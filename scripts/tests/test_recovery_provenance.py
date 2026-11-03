"""A clearance report must not attribute results to files changed during its run."""
import importlib.util
import contextlib
import io
import json
import sys
import zipfile
from unittest import mock
import os
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location(
    'recovery_provenance', Path(__file__).resolve().parents[1]/'audit-recovery-mesh-trace.py')
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


class ProvenanceGuard(unittest.TestCase):
    def test_unchanged_input_keeps_original_identity(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder)/'frames.json'
            path.write_bytes(b'original')
            paths = {'frames': path}
            initial = audit.provenance_snapshot(paths)
            audit.require_unchanged_provenance(paths, initial)
            self.assertEqual(initial['frames'], audit.digest(path))

    def test_same_size_replacement_with_restored_mtime_is_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder)/'renderer.java'
            path.write_bytes(b'original')
            stat = path.stat()
            paths = {'source:renderer': path}
            initial = audit.provenance_snapshot(paths)
            path.write_bytes(b'modified')
            os.utime(path, ns=(stat.st_atime_ns, stat.st_mtime_ns))
            with self.assertRaisesRegex(ValueError, 'source:renderer'):
                audit.require_unchanged_provenance(paths, initial)

    def test_removed_input_cannot_receive_a_report(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder)/'trace.jsonl'
            path.write_bytes(b'trace')
            paths = {'trace': path}
            initial = audit.provenance_snapshot(paths)
            path.unlink()
            with self.assertRaises(FileNotFoundError):
                audit.require_unchanged_provenance(paths, initial)

    def test_cli_refuses_to_print_a_clearance_report_after_source_change(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            source = root/'renderer.java'
            source.write_text('original')
            base, minecraft = root/'base.jar', root/'minecraft.jar'
            for path in (base, minecraft):
                with zipfile.ZipFile(path, 'w'):
                    pass
            frames, trace = root/'frames.json', root/'trace.jsonl'
            frames.write_text(json.dumps([{'fixture': 'minimal'}]))
            trace.write_text('')
            def mutate(*args):
                source.write_text('modified')
                return {'passes': True}
            output, errors = io.StringIO(), io.StringIO()
            with mock.patch.object(audit, 'ROOT', root), \
                 mock.patch.object(audit, 'SOURCE_PINS', {'renderer.java': audit.digest(source)}), \
                 mock.patch.object(audit, 'BASE_PIN', audit.digest(base)), \
                 mock.patch.object(audit, 'MINECRAFT_PIN', audit.digest(minecraft)), \
                 mock.patch.object(audit, 'Resources'), \
                 mock.patch.object(audit, 'audit', side_effect=mutate), \
                 mock.patch.object(sys, 'argv', ['audit', str(base), str(minecraft), str(frames), str(trace)]), \
                 contextlib.redirect_stdout(output), contextlib.redirect_stderr(errors):
                with self.assertRaises(SystemExit) as stopped:
                    audit.main()
            self.assertEqual(stopped.exception.code, 2)
            self.assertEqual(output.getvalue(), '')
            self.assertIn('Audit provenance changed during analysis: source:renderer.java', errors.getvalue())
