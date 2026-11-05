#!/usr/bin/env python3
"""Verify the bounded FD402D linked-train fresh-process evidence."""

import gzip
import hashlib
from pathlib import Path
import tarfile
import zipfile


ROOT = Path(__file__).resolve().parent
SELECTED = (ROOT / "../../../qualification/2026-11-05-ec2fcda/qualification.jar").resolve()
OVERLAY = ROOT / "focused-fixture-overlay.jar"
CHANGED_ENTRIES = {
    "dev/fourthdimension/runtime/CarrierValidationFixture.class",
    "dev/fourthdimension/runtime/CarrierValidationFixture$1.class",
}


def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def compressed_text(path: Path) -> str:
    with gzip.open(path, "rt", encoding="utf-8") as source:
        return source.read()


for line in text(ROOT / "SHA256SUMS").splitlines():
    expected, name = line.split("  ", 1)
    actual = hashlib.sha256((ROOT / name).read_bytes()).hexdigest()
    assert actual == expected, (name, expected, actual)

assert hashlib.sha256(SELECTED.read_bytes()).hexdigest() == (
    "656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7"
)
assert hashlib.sha256(OVERLAY.read_bytes()).hexdigest() == (
    "0e31b09efb485b57a28586d71e96662c490cf3fe0fdd2215a30f56e97837538c"
)
with zipfile.ZipFile(SELECTED) as selected_zip, zipfile.ZipFile(OVERLAY) as overlay_zip:
    selected_names = set(selected_zip.namelist())
    overlay_names = set(overlay_zip.namelist())
    assert selected_names == overlay_names
    changed = {
        name
        for name in selected_names
        if selected_zip.read(name) != overlay_zip.read(name)
    }
assert changed == CHANGED_ENTRIES, changed

cases = {
    "arrow": {
        "world": "FD402DArrowTrain",
        "design": "ARROW",
        "cars": [
            "919b56b0-946b-4ac9-8992-7ed066b1da47",
            "dde70cfa-e5a9-416f-9c18-5771fa7bb560",
        ],
        "bearing": "ede06dcf-1961-4bd5-b461-6a1aab88b272",
        "motion": "motionChassis={ARROW=2}",
    },
    "pax": {
        "world": "FD402DPaxTrain",
        "design": "PAX",
        "cars": [
            "bb2d5d12-9f59-4d1b-b99c-2f1c8412ac18",
            "e0a82fe6-a68a-4b83-bd19-aaf510d0c896",
            "8d3927da-9dd3-4abc-b5c4-0f3450a9116e",
            "4ce06d52-2d10-482d-aed5-cbe2db7b35f7",
        ],
        "bearing": "1be3f87f-dacc-4644-a2e7-01b0a32704da",
        "motion": "motionChassis={PAX=4}",
    },
}

for family, case in cases.items():
    directory = ROOT / family
    pause = compressed_text(directory / "pause-client.log.gz")
    resume = compressed_text(directory / "resume-client.log.gz")

    for marker in (
        "paused occupied recovery for fresh-process resume",
        "FD_CARRIER_VALIDATION passed",
        "riderMounted=true",
        "recoveryLinkSeen=true",
        "recoveryTrainSeen=true",
        "Stopping!",
    ):
        assert marker in pause, (family, marker)
    for marker in (
        "resumed persisted occupied recovery",
        "linked=true retained=true braked=true",
        "started occupied recovery",
        "braked occupied recovery",
        "completed fresh-process resumed occupied recovery cycle",
        "detached=true restraintsOpen=true ridersUnloaded=1",
        "FD_CARRIER_VALIDATION passed",
        "riderMounted=false",
        "recoveryLinkSeen=true",
        "recoveryMotionSeen=true",
        "recoveryCycleSeen=true",
        "recoveryTrainSeen=true",
        "recoveryTrainMotionSeen=true",
        case["motion"],
        "Stopping!",
    ):
        assert marker in resume, (family, marker)
    for identifier in (*case["cars"], case["bearing"]):
        assert identifier in resume, (family, identifier)
    for log in (pause, resume):
        assert "OutOfMemoryError" not in log
        assert "IllegalStateException" not in log

    for phase in ("pause", "resume"):
        vm_args = text(directory / f"{phase}-vm-args.txt")
        program_args = text(directory / f"{phase}-program-args.txt")
        classpath = text(directory / f"{phase}-classpath.txt")
        assert "-Dfourthdimension.carrierFixtureRecoveryTrain=true" in vm_args
        assert f"-Dfourthdimension.carrierFixtureRecoveryDesign={case['design']}" in vm_args
        assert case["world"] in program_args
        assert "fd402d-selected-fixture.jar" in classpath

    archived_data = {}
    for stage in ("pause", "final"):
        archive_path = directory / f"{stage}-world.tar.gz"
        with tarfile.open(archive_path, "r:gz") as archive:
            foundation_name = f"{case['world']}/data/fourthdimension_foundation.dat"
            player_name = (
                f"{case['world']}/playerdata/"
                "380df991-f603-344c-a090-369bad2a924a.dat"
            )
            foundation = gzip.decompress(archive.extractfile(foundation_name).read())
            player = gzip.decompress(archive.extractfile(player_name).read())
        archived_data[stage] = (foundation, player)

    pause_foundation, pause_player = archived_data["pause"]
    final_foundation, final_player = archived_data["final"]
    assert b"recovery_cart" in pause_foundation
    assert b"RootVehicle" in pause_player
    assert b"recovery_cart" not in final_foundation
    assert b"RootVehicle" not in final_player

print("PASS: FD402D selected-production linked-train fresh-process evidence")
