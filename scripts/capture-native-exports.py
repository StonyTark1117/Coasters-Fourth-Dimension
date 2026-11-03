#!/usr/bin/env python3
"""Preserve five native exports after a profile's process exits; never launch a JVM.

Capture status is independent of gameplay acceptance. Run metadata is supplied by
the owner; its exit code is not independently observed by this offline helper.
"""
import argparse
import datetime as dt
import gzip
import hashlib
import json
from pathlib import Path
import re
import sys

EXPORTS = (
    "bank-native-frames.json",
    "recovery-bank-native-frames.json",
    "recovery-bank-train-native-frames.json",
    "recovery-bank-mesh-trace.jsonl",
    "recovery-bank-train-mesh-trace.jsonl",
)


def timestamp(value):
    value = dt.datetime.fromisoformat(value)
    if value.tzinfo is None:
        raise ValueError("Run timestamps must include a timezone")
    return value.timestamp()


def signature(stat):
    return (stat.st_dev, stat.st_ino, stat.st_size, stat.st_mtime_ns, stat.st_ctime_ns)


def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(chunk)
    return result.hexdigest()


def capture(run_dir, metadata_path, output):
    metadata_bytes = metadata_path.read_bytes()
    metadata = json.loads(metadata_bytes)
    start, end = timestamp(metadata["start_utc"]), timestamp(metadata["end_utc"])
    if end < start:
        raise ValueError("Run ended before it started")
    if end > dt.datetime.now(dt.timezone.utc).timestamp():
        raise ValueError("Run end is in the future; wait for process completion")
    if not re.fullmatch(r"[0-9a-f]{40}", metadata["source"]):
        raise ValueError("Full source commit is required")
    if metadata["profile"] not in ("base", "styles", "engineered", "combined"):
        raise ValueError("Unknown profile")
    if type(metadata["exit_code"]) is not int:
        raise ValueError("Actual completed-process exit code must be recorded")
    run_dir, output = run_dir.resolve(), output.resolve()
    if output == run_dir or run_dir in output.parents:
        raise ValueError("Evidence output must be outside the reusable runtime directory")
    if not run_dir.is_dir():
        raise ValueError("Runtime directory does not exist")
    output.mkdir(parents=True, exist_ok=False)
    record = {
        "source": metadata["source"], "profile": metadata["profile"],
        "run_start_utc": metadata["start_utc"], "run_end_utc": metadata["end_utc"],
        "reported_process_exit_code": metadata["exit_code"],
        "metadata_path": str(metadata_path.resolve()),
        "metadata_sha256": hashlib.sha256(metadata_bytes).hexdigest(),
        "capture_started_utc": dt.datetime.now(dt.timezone.utc).isoformat(),
        "tool_sha256": digest(Path(__file__)),
        "scope": "Post-exit byte preservation only. Caller owns process-exit and source attribution; no test, clearance, or release acceptance inferred.",
        "files": {},
    }
    for name in EXPORTS:
        source = run_dir / name
        item = {"source_path": str(source), "issues": []}
        record["files"][name] = item
        try:
            if source.is_symlink():
                raise ValueError("Export is a symlink")
            if not source.is_file():
                item["issues"].append("missing")
                continue
            before = source.stat()
            item.update(source_bytes=before.st_size, source_mtime_ns=before.st_mtime_ns)
            if not start <= before.st_mtime_ns / 1e9 <= end:
                item["issues"].append("mtime_outside_run_interval")
            raw_hash, size = hashlib.sha256(), 0
            destination = output / (name + ".gz")
            # Empty header filename and fixed mtime make identical payloads reproducible.
            with source.open("rb") as src, destination.open("xb") as dst:
                with gzip.GzipFile(filename="", fileobj=dst, mode="wb", mtime=0) as packed:
                    for chunk in iter(lambda: src.read(1024 * 1024), b""):
                        size += len(chunk)
                        raw_hash.update(chunk)
                        packed.write(chunk)
            item.update(payload=destination.name, bytes=size, sha256=raw_hash.hexdigest(),
                        gzip_sha256=digest(destination))
            after = source.stat()
            if signature(before) != signature(after) or size != before.st_size:
                item["issues"].append("source_changed_during_capture")
            if size == 0:
                item["issues"].append("empty")
            # Verify the stored copy, not just the stream used to create it.
            copied_hash, copied_size = hashlib.sha256(), 0
            with gzip.open(destination, "rb") as copied:
                for chunk in iter(lambda: copied.read(1024 * 1024), b""):
                    copied_hash.update(chunk)
                    copied_size += len(chunk)
            if copied_hash.hexdigest() != item["sha256"] or copied_size != size:
                item["issues"].append("copy_verification_failed")
        except (OSError, ValueError, EOFError) as error:
            item["issues"].append(f"capture_error: {error}")
    record["capture_complete"] = all(not item["issues"] for item in record["files"].values())
    record["candidate_for_separate_acceptance_review"] = record["capture_complete"] and metadata["exit_code"] == 0
    record["capture_ended_utc"] = dt.datetime.now(dt.timezone.utc).isoformat()
    (output / "manifest.json").write_text(json.dumps(record, indent=2) + "\n")
    return record


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--run-dir", required=True, type=Path)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    try:
        record = capture(args.run_dir, args.metadata, args.output)
    except (OSError, ValueError, KeyError, TypeError) as error:
        print(f"Capture refused: {error}", file=sys.stderr)
        return 2
    print(json.dumps({"manifest": str(args.output / "manifest.json"),
                      "capture_complete": record["capture_complete"],
                      "reported_process_exit_code": record["reported_process_exit_code"],
                      "issues": {n: v["issues"] for n, v in record["files"].items() if v["issues"]}}, indent=2))
    return 0 if record["capture_complete"] else 1


if __name__ == "__main__":
    sys.exit(main())
