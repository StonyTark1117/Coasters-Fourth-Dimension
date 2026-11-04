#!/usr/bin/env python3
"""Verify the frozen FD401 candidate and its scoped artifact comparisons."""

from __future__ import annotations

import hashlib
import json
import subprocess
import sys
from pathlib import Path
from zipfile import ZipFile


HERE = Path(__file__).resolve().parent
ROOT = Path(
    subprocess.check_output(["git", "rev-parse", "--show-toplevel"], cwd=HERE, text=True).strip()
)
CANDIDATE = HERE / "coasters-fourth-dimension-0.9.0-dev.27.jar"
CONFIG = json.loads((HERE / "candidate.json").read_text())


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def entries(path: Path) -> dict[str, str]:
    with ZipFile(path) as archive:
        names = [item.filename for item in archive.infolist() if not item.is_dir()]
        assert len(names) == len(set(names)), f"duplicate JAR entry in {path}"
        return {name: hashlib.sha256(archive.read(name)).hexdigest() for name in names}


def entry_set_digest(rows: list[tuple[str, str]]) -> str:
    payload = "".join(f"{name}\0{digest}\n" for name, digest in sorted(rows)).encode()
    return hashlib.sha256(payload).hexdigest()


def git(*arguments: str) -> str:
    return subprocess.check_output(["git", *arguments], cwd=ROOT, text=True).strip()


candidate = CONFIG["candidate"]
assert git("rev-parse", f"{candidate['sourceCommit']}^{{commit}}") == candidate["sourceCommit"]
assert CANDIDATE.stat().st_size == candidate["artifactSize"]
assert sha256(CANDIDATE) == candidate["artifactSha256"]

classpath_path = HERE / CONFIG["classpath"]["record"]
assert sha256(classpath_path) == CONFIG["classpath"]["recordSha256"]
classpath = json.loads(classpath_path.read_text())
assert len(classpath) == CONFIG["classpath"]["entryCount"]
classpath_by_name = {row["name"]: row for row in classpath}
assert len(classpath_by_name) == len(classpath)
assert classpath_by_name[CANDIDATE.name]["sha256"] == candidate["artifactSha256"]
assert classpath_by_name[CANDIDATE.name]["size"] == candidate["artifactSize"]

dependency_hashes = {
    "create-coasters-simulated-1601871-8655742.jar": "f80b4c3cdf9fa6188804967147761b0373e150a6eadbdc53626e5ac9700b2049",
    "coasters-simulated-track-styles-1642719-9020601.jar": "42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38",
    "coasters-engineered-1643631-8863559.jar": "eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d",
    "createaddition-439890-8188476.jar": "41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9",
    "create-1.21.1-6.0.10-280.jar": "192c7b0be13f523dcec9a7a2393bbc2614c3c9bd3edb1f7720a97bcd4f0b87a7",
    "create-1.21.1-6.0.10-280-slim.jar": "f0652bee27460f2d26a748f537cb5d687981441378d3a526d17ffaab5a1072bb",
    "sable-neoforge-1.21.1-2.0.3.jar": "da6c3b66238586603d1dcaa2afb012d36815fbce0a2d5938fbb2936701d42279",
    "sable-sable_rapier-1.21.1-2.0.3.jar": "aabd5ed08349821989cd1806122918cbec3c9dfafe4584e0ab012fa015b7749d",
    "sable-companion-common-1.21.1-1.6.0.jar": "873633e35046e3761b277ff8a1ecad0d55d9a3014fa81a0b084c9aecba1f3bed",
    "flywheel-neoforge-1.21.1-1.0.6.jar": "31dda15c205eb596d3b3449ef03f6af7363a6cd35b3da4bfe916b304f9e5337e",
    "ponder-neoforge-1.0.82+mc1.21.1.jar": "0cf4611ad853042b689ac386184c5bbe02950efcffddb49e5f604e82baddb0dc",
    "Registrate-MC1.21-1.3.0+67.jar": "510f4041c41739f1d8ea8850ab8364d3e3a5fada8529beed5d9f479e2523db52",
    "veil-neoforge-1.21.1-4.1.4.jar": "9c2899086a96339245b5abde82879011753871f25443df9581881f29596a7180",
    "neoforge-21.1.248.jar": "974c295d4390599b20486f80d38a14c934cf773c07bb0dbacf92dc8eb04c6dc7",
}
for name, expected in dependency_hashes.items():
    assert classpath_by_name[name]["sha256"] == expected, f"dependency changed: {name}"

candidate_entries = entries(CANDIDATE)
generated = {"META-INF/MANIFEST.MF", "META-INF/LICENSE-fourthdimension.txt"}
class_rows = [(name, digest) for name, digest in candidate_entries.items() if name.endswith(".class")]
generated_rows = [(name, digest) for name, digest in candidate_entries.items() if name in generated]
resource_rows = [
    (name, digest)
    for name, digest in candidate_entries.items()
    if not name.endswith(".class") and name not in generated
]
parity = CONFIG["jarBuildParity"]
assert len(class_rows) == parity["classFiles"]
assert len(resource_rows) == parity["resourceFiles"]
assert len(generated_rows) == parity["generatedFiles"]
assert len(candidate_entries) == parity["totalFiles"]
assert entry_set_digest(class_rows) == parity["classEntrySetSha256"]
assert entry_set_digest(resource_rows) == parity["resourceEntrySetSha256"]
assert entry_set_digest(generated_rows) == parity["generatedEntrySetSha256"]
all_payload = "".join(
    f"{kind}\0{name}\0{digest}\n"
    for kind, rows in sorted(
        {"class": class_rows, "generated": generated_rows, "resource": resource_rows}.items()
    )
    for name, digest in sorted(rows)
).encode()
assert hashlib.sha256(all_payload).hexdigest() == parity["allEntrySetSha256"]

# The final candidate is exactly the artifact used by the a23ae2d coupled/UI records.
comparisons = {
    "a23ae2d": (
        ROOT
        / "docs/evidence/0.9/fd401-styles-coupled-fullturn-a23ae2d"
        / CANDIDATE.name,
        [],
    ),
    "9ab54cf": (
        ROOT / "docs/evidence/0.9/fd401-styles-banked-fullturn-9ab54cf" / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
        ],
    ),
    "b2403d3": (
        ROOT / "docs/evidence/0.9/fd401-styles-midsection-reach-b2403d3" / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
        ],
    ),
    "2ba168d": (
        ROOT
        / "docs/evidence/0.9/fd401-controller-native-availability-2ba168d"
        / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/AppearanceGameTests.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
        ],
    ),
    "663aa12": (
        ROOT
        / "docs/evidence/0.9/fd401-engineered-pax-callback-663aa12"
        / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/AppearanceGameTests.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
            "dev/fourthdimension/gametest/RideTelemetryGameTests.class",
        ],
    ),
    "2e12e9d": (
        ROOT
        / "docs/evidence/0.9/fd401-candidate-pax-realignment-2e12e9d"
        / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/AppearanceGameTests.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
            "dev/fourthdimension/gametest/EngineeredInterlockGameTests.class",
            "dev/fourthdimension/gametest/RideTelemetryGameTests.class",
            "dev/fourthdimension/integration/EngineeredInterlock.class",
            "dev/fourthdimension/mixin/EngineeredMotorGuardMixin.class",
        ],
    ),
    "81ee2e6": (
        ROOT / "docs/evidence/0.9/fd401-candidate-81ee2e6" / CANDIDATE.name,
        [
            "dev/fourthdimension/gametest/AppearanceGameTests.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Frame.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests$Section.class",
            "dev/fourthdimension/gametest/BankedArrowGameTests.class",
            "dev/fourthdimension/gametest/EngineeredInterlockGameTests.class",
            "dev/fourthdimension/gametest/RideTelemetryGameTests.class",
            "dev/fourthdimension/integration/EngineeredInterlock.class",
            "dev/fourthdimension/integration/RideTelemetry.class",
            "dev/fourthdimension/mixin/EngineeredMotorGuardMixin.class",
            "dev/fourthdimension/vehicle/VehicleOperations$1Candidate.class",
            "dev/fourthdimension/vehicle/VehicleOperations.class",
        ],
    ),
}
for label, (path, expected_changes) in comparisons.items():
    older = entries(path)
    actual_changes = sorted(
        name
        for name in candidate_entries.keys() | older.keys()
        if candidate_entries.get(name) != older.get(name)
    )
    assert actual_changes == sorted(expected_changes), f"unexpected {label} artifact delta"

# No packaged input changed after the exact a23ae2d artifact build.
packaged_inputs = [
    "build.gradle",
    "settings.gradle",
    "gradle.properties",
    "gradle",
    "src/main",
    "src/generated",
    "src/test",
]
result = subprocess.run(
    ["git", "diff", "--quiet", "a23ae2d", candidate["sourceCommit"], "--", *packaged_inputs],
    cwd=ROOT,
)
assert result.returncode == 0, "packaged inputs differ between a23ae2d and candidate source"

print(
    "FD401 candidate verified:",
    candidate["sourceCommit"][:7],
    candidate["artifactSha256"],
    f"{len(classpath)} classpath files",
    f"{len(candidate_entries)} JAR files",
)
