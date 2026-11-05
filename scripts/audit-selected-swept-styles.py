#!/usr/bin/env python3
"""Audit a selected-JAR Arrow trace between native tick snapshots.

Consumes a passing current-trace substep report; the existing swept-geometry
implementation supplies the conservative motion and local-exception checks.
"""

import argparse
import gzip
import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SELECTED_JAR = "656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7"


def digest(path):
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def payload_digest(path):
    h = hashlib.sha256()
    with gzip.open(path, "rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("selected_jar", "base", "minecraft", "styles", "frames", "trace", "substeps_report"):
        parser.add_argument(name, type=Path)
    args = parser.parse_args()
    spec = importlib.util.spec_from_file_location(
        "swept", ROOT / "scripts/audit-recovery-swept-styles.py")
    swept = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(swept)

    report = json.loads(args.substeps_report.read_text())
    require(report["passes"], "Substep report did not pass")
    require(digest(args.selected_jar) == SELECTED_JAR, "Wrong selected JAR")
    for name in ("base", "minecraft", "styles", "frames"):
        require(digest(getattr(args, name)) == report["inputHashes"][name],
                "Substep input changed: " + name)
    require(payload_digest(args.trace) == report["inputHashes"]["tick_trace"],
            "Compressed tick-trace payload changed")
    for name, expected in report["sourceHashes"].items():
        require(digest(ROOT / name) == expected, "Audited source changed: " + name)
    require(digest(swept.SABLE) == swept.SABLE_PIN, "Sable interpolation JAR changed")
    require(digest(swept.SABLE_COMPANION) == swept.SABLE_COMPANION_PIN,
            "Sable companion JAR changed")
    routes = json.loads(args.frames.read_text())
    arrow = [route for route in routes if route["family"] == "ARROW"]
    require(len(arrow) == 1 and arrow[0]["carCount"] == 2,
            "Expected one coupled Arrow route")
    sections = arrow[0]["sections"]
    require(len(sections) == 5, "Expected five Arrow sections")
    names = ("arrow_4d", "arrow_4d_dbl_spine", "transition")
    plans = {
        "arrow_4d": {section["rail"]: "arrow_4d" for section in sections},
        "arrow_4d_dbl_spine": {section["rail"]: "arrow_4d_dbl_spine" for section in sections},
        "transition": {section["rail"]: style for section, style in zip(
            sections, ("original", "arrow_4d", "arrow_4d_dbl_spine", "original", "arrow_4d"))},
    }
    with zipfile.ZipFile(args.base) as base, zipfile.ZipFile(args.minecraft) as minecraft:
        resources = swept.mesh.Resources(base, minecraft)
        wheel_radius = max(swept.radius(points) for points, _ in resources.wheel_envelopes())
        results = {}
        for name in names:
            sampled = report["results"][name]["sampled"]
            require(sampled["passes"] and sampled["requiredGap"] == .02,
                    "Sampled plan did not meet required gap: " + name)
            plan_report = {"result": sampled, "prospectiveStylePlan": plans[name]}
            scanned = swept.audit_trace(args.trace, plan_report, resources, wheel_radius)
            exceptions = scanned.pop("exceptions")
            local = swept.check_exceptions(
                args.trace, args.frames, plan_report, exceptions, resources, args.styles)
            require(scanned["maximumMotion"]["body"] <
                    scanned["globalSampledGap"] - .02 or local["count"] > 0,
                    "Unresolved body sweep: " + name)
            require(local["minimumSweptLowerBound"] is None or
                    local["minimumSweptLowerBound"] >= .02,
                    "Local swept gap below requirement: " + name)
            results[name] = {**scanned, "localChecks": local}
    require(digest(args.selected_jar) == SELECTED_JAR and
            digest(args.frames) == report["inputHashes"]["frames"] and
            payload_digest(args.trace) == report["inputHashes"]["tick_trace"],
            "Inputs changed during audit")
    print(json.dumps({
        "passes": True,
        "scope": "Selected-JAR coupled Arrow native tick poses, default Styles models, "
                 "and conservative Sable between-snapshot interpolation; excludes client "
                 "extrapolation, equipment/cosmetics, arbitrary models and observed rendering.",
        "selectedJarSha256": SELECTED_JAR,
        "inputHashes": report["inputHashes"],
        "substepsReportSha256": digest(args.substeps_report),
        "sableSha256": swept.SABLE_PIN,
        "sableCompanionSha256": swept.SABLE_COMPANION_PIN,
        "sweptImplementationSha256": digest(ROOT / "scripts/audit-recovery-swept-styles.py"),
        "results": results,
    }, indent=2))


if __name__ == "__main__":
    main()
