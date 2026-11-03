"""Evidence-integrity regressions only; no Minecraft or JVM process is launched."""
import datetime as dt
import gzip
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest import mock

spec = importlib.util.spec_from_file_location("capture", Path(__file__).resolve().parents[1] / "capture-native-exports.py")
capture = importlib.util.module_from_spec(spec)
spec.loader.exec_module(capture)


class CaptureTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.run = self.root / "runtime"
        self.run.mkdir()
        now = dt.datetime.now(dt.timezone.utc)
        self.metadata = self.root / "metadata.json"
        self.meta = {"source": "a" * 40, "profile": "styles", "exit_code": 0,
                     "start_utc": (now - dt.timedelta(seconds=60)).isoformat(),
                     "end_utc": now.isoformat()}
        self.metadata.write_text(json.dumps(self.meta))
        self.payloads = {}
        for name in capture.EXPORTS:
            content = (name + "\n").encode() * 100
            self.payloads[name] = content
            path = self.run / name
            path.write_bytes(content)
            os.utime(path, (now.timestamp() - 1, now.timestamp() - 1))
        self.output = self.root / "evidence"

    def collect(self):
        return capture.capture(self.run, self.metadata, self.output)

    def test_complete_archive_roundtrip_and_reproducibility(self):
        first = self.collect()
        self.assertTrue(first["capture_complete"])
        self.assertTrue(first["candidate_for_separate_acceptance_review"])
        second = capture.capture(self.run, self.metadata, self.root / "second")
        for name, expected in self.payloads.items():
            item = first["files"][name]
            self.assertEqual(gzip.decompress((self.output / item["payload"]).read_bytes()), expected)
            self.assertEqual(item["sha256"], hashlib.sha256(expected).hexdigest())
            self.assertEqual(item["gzip_sha256"], second["files"][name]["gzip_sha256"])

    def test_missing_and_stale_exports_preserve_failed_record(self):
        (self.run / capture.EXPORTS[0]).unlink()
        os.utime(self.run / capture.EXPORTS[1], (0, 0))
        result = self.collect()
        self.assertFalse(result["capture_complete"])
        self.assertFalse(result["candidate_for_separate_acceptance_review"])
        self.assertEqual(result["files"][capture.EXPORTS[0]]["issues"], ["missing"])
        self.assertIn("mtime_outside_run_interval", result["files"][capture.EXPORTS[1]]["issues"])
        self.assertTrue((self.output / "manifest.json").exists())

    def test_actual_source_mutation_during_copy_is_rejected(self):
        original = gzip.GzipFile
        source = self.run / capture.EXPORTS[0]
        changed = []

        class MutatingWriter(original):
            def write(self, data):
                if not changed:
                    changed.append(True)
                    with source.open("ab") as stream:
                        stream.write(b"concurrent writer\n")
                return super().write(data)

        with mock.patch.object(capture.gzip, "GzipFile", MutatingWriter):
            result = self.collect()
        self.assertFalse(result["capture_complete"])
        self.assertIn("source_changed_during_capture", result["files"][capture.EXPORTS[0]]["issues"])

    def test_failed_process_is_never_candidate_evidence(self):
        self.meta["exit_code"] = 143
        self.metadata.write_text(json.dumps(self.meta))
        result = self.collect()
        self.assertTrue(result["capture_complete"])
        self.assertFalse(result["candidate_for_separate_acceptance_review"])
        self.assertEqual(result["reported_process_exit_code"], 143)

    def test_empty_and_symlink_are_rejected(self):
        (self.run / capture.EXPORTS[0]).write_bytes(b"")
        linked = self.run / capture.EXPORTS[1]
        linked.unlink()
        linked.symlink_to(self.run / capture.EXPORTS[2])
        result = self.collect()
        self.assertFalse(result["capture_complete"])
        self.assertIn("empty", result["files"][capture.EXPORTS[0]]["issues"])
        self.assertTrue(any("symlink" in x for x in result["files"][capture.EXPORTS[1]]["issues"]))

    def test_refuses_overwrite_and_runtime_destination(self):
        self.collect()
        before = (self.output / "manifest.json").read_bytes()
        with self.assertRaises(FileExistsError):
            self.collect()
        self.assertEqual(before, (self.output / "manifest.json").read_bytes())
        with self.assertRaises(ValueError):
            capture.capture(self.run, self.metadata, self.run / "unsafe")

    def test_incomplete_metadata_creates_no_archive(self):
        del self.meta["exit_code"]
        self.metadata.write_text(json.dumps(self.meta))
        with self.assertRaises(KeyError):
            self.collect()
        self.assertFalse(self.output.exists())


if __name__ == "__main__":
    unittest.main()
