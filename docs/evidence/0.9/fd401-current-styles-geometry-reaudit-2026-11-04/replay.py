#!/usr/bin/env python3
"""Reproduce or verify the bounded FD401B retained-input geometry re-audit."""

import contextlib
import gzip
import hashlib
import importlib.util
import json
import shutil
import sys
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
BUNDLE = Path(__file__).resolve().parent
INPUTS = ROOT / "docs/evidence/0.9/fd402-isolated-base-ff7c831/native-exports"
SINGLE = "recovery-bank"
TRAIN = "recovery-bank-train"


def digest(path):
    hasher = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def report_sources():
    return json.loads((BUNDLE / "single-arrow_4d.json").read_text())["sourceHashes"]


def check_saved_inputs():
    config = json.loads((BUNDLE / "config.json").read_text())
    swept = json.loads((BUNDLE / "swept-clearance.json").read_text())
    for filename, relative in config["jars"].items():
        path = Path(relative)
        if digest(path) != config["dependencyHashes"][filename]:
            raise SystemExit(f"Dependency hash mismatch: {path}")
    expected_archives = {
        "single": (f"{SINGLE}-mesh-trace.jsonl.gz", f"{SINGLE}-native-frames.json.gz"),
        "train": (f"{TRAIN}-mesh-trace.jsonl.gz", f"{TRAIN}-native-frames.json.gz"),
    }
    for label, names in expected_archives.items():
        hashes = swept["inputHashes"][label]
        for kind, filename in zip(("traceArchive", "framesArchive"), names):
            if digest(INPUTS / filename) != hashes[kind]:
                raise SystemExit(f"Retained {label} {kind} hash mismatch")
        trace, frames = (gzip.decompress((INPUTS / filename).read_bytes()) for filename in names)
        for kind, payload in zip(("tracePayload", "framesPayload"), (trace, frames)):
            if hashlib.sha256(payload).hexdigest() != hashes[kind]:
                raise SystemExit(f"Retained {label} {kind} payload hash mismatch")
    expected_sources = report_sources()
    for relative, expected in expected_sources.items():
        if digest(ROOT / relative) != expected:
            raise SystemExit(f"Repository source changed: {relative}")
    if not swept["passes"] or any(not all(route["passes"] for route in json.loads(path.read_text())["routes"])
                                   for path in (BUNDLE / "single-arrow-model-clearance.json",
                                                BUNDLE / "train-arrow-model-clearance.json")):
        raise SystemExit("Saved geometry report does not pass")
    expected_files = [line.split("  ", 1)[1] for line in
                      (BUNDLE / "SHA256SUMS").read_text().splitlines()]
    for name in expected_files:
        if digest(BUNDLE / name) != next(line.split()[0] for line in
                                        (BUNDLE / "SHA256SUMS").read_text().splitlines()
                                        if line.endswith("  " + name)):
            raise SystemExit(f"Bundle hash mismatch: {name}")
    print("Saved bundle, current sources, dependencies, and retained inputs verify.")


def run_main(module, argv, output):
    sys.argv = [str(argv[0]), *(str(item) for item in argv[1:])]
    with output.open("w") as stream, contextlib.redirect_stdout(stream):
        module.main()


def replay():
    expected_sources = report_sources()
    config = json.loads((BUNDLE / "config.json").read_text())
    for filename, expected in config["toolHashes"].items():
        if digest(ROOT / "scripts" / filename) != expected:
            raise SystemExit(f"Audit tool changed: {filename}")

    mesh = load("fd401_mesh_reaudit", ROOT / "scripts/audit-recovery-mesh-trace.py")
    mesh.SOURCE_PINS = {name: expected_sources[name] for name in mesh.SOURCE_PINS}
    styles = load("fd401_styles_reaudit", ROOT / "scripts/audit-recovery-styles.py")
    styles.recovery.SOURCE_PINS = {name: expected_sources[name] for name in styles.recovery.SOURCE_PINS}
    styles.SOURCE_PINS = {name: expected_sources[name] for name in styles.SOURCE_PINS}

    base, minecraft, provider = (Path(config["jars"][key]) for key in ("base", "minecraft", "styles"))
    single_trace_archive = INPUTS / f"{SINGLE}-mesh-trace.jsonl.gz"
    single_frames_archive = INPUTS / f"{SINGLE}-native-frames.json.gz"
    train_trace_archive = INPUTS / f"{TRAIN}-mesh-trace.jsonl.gz"
    train_frames_archive = INPUTS / f"{TRAIN}-native-frames.json.gz"
    with tempfile.TemporaryDirectory(prefix="fd401-geometry-replay-") as temporary:
        temp = Path(temporary)
        single_trace = temp / "single-trace.jsonl"
        single_frames = temp / "single-frames.json"
        train_trace = temp / "train-trace.jsonl"
        train_frames = temp / "train-frames.json"
        single_arrow_frames = temp / "single-arrow-frames.json"
        train_arrow_frames = temp / "train-arrow-frames.json"
        single_trace.write_bytes(gzip.decompress(single_trace_archive.read_bytes()))
        single_frames.write_bytes(gzip.decompress(single_frames_archive.read_bytes()))
        train_trace.write_bytes(gzip.decompress(train_trace_archive.read_bytes()))
        train_frames.write_bytes(gzip.decompress(train_frames_archive.read_bytes()))
        single_arrow_frames.write_text(json.dumps(
            [route for route in json.loads(single_frames.read_text()) if route["family"] == "ARROW"]))
        train_arrow_frames.write_text(json.dumps(
            [route for route in json.loads(train_frames.read_text()) if route["family"] == "ARROW"]))

        run_main(mesh, [ROOT / "scripts/audit-recovery-mesh-trace.py", base, minecraft,
                        single_arrow_frames, single_trace], BUNDLE / "single-arrow-model-clearance.json")
        run_main(mesh, [ROOT / "scripts/audit-recovery-mesh-trace.py", base, minecraft,
                        train_arrow_frames, train_trace], BUNDLE / "train-arrow-model-clearance.json")

        cases = [
            ("single", single_frames, single_trace, "--style", "arrow_4d", "single-arrow_4d.json"),
            ("single", single_frames, single_trace, "--style", "arrow_4d_dbl_spine", "single-arrow_4d_dbl_spine.json"),
            ("train", train_frames, train_trace, "--style", "arrow_4d", "train-arrow_4d.json"),
            ("train", train_frames, train_trace, "--style", "arrow_4d_dbl_spine", "train-arrow_4d_dbl_spine.json"),
            ("train", train_frames, train_trace, "--style-plan", BUNDLE / "train-transition-plan.json", "train-transition.json"),
        ]
        for _, frames, trace, flag, choice, output_name in cases:
            run_main(styles, [ROOT / "scripts/audit-recovery-styles.py", base, minecraft, provider,
                              frames, trace, flag, choice], BUNDLE / output_name)

    swept = load("fd401_swept_reaudit", ROOT / "scripts/audit-recovery-swept-styles.py")
    swept.REPORTS = BUNDLE
    swept.INPUTS = INPUTS
    run_main(swept, [ROOT / "scripts/audit-recovery-swept-styles.py"], BUNDLE / "swept-clearance.json")
    refresh_sums()
    check_saved_inputs()


def refresh_sums():
    entries = []
    for path in sorted(BUNDLE.iterdir()):
        if path.is_file() and path.name != "SHA256SUMS":
            entries.append(f"{digest(path)}  {path.name}")
    (BUNDLE / "SHA256SUMS").write_text("\n".join(entries) + "\n")


if __name__ == "__main__":
    if "--verify-only" in sys.argv:
        check_saved_inputs()
    else:
        replay()
