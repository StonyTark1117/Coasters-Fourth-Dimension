"""Verify the bounded retained-input audit, without starting Minecraft."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent
read = lambda name: json.loads((root / name).read_text())
manifest = read("manifest.json")
for name, digest in manifest.items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == digest, name
provenance = read("provenance.json")
bank = read("bank-control-clearance.json")
wheel = read("straight-wheel-clearance.json")
assert len(read("results.json")) == 2
assert all(r["exit_code"] == 0 for r in read("results.json"))
assert bank["nativeFramesSha256"] == provenance["retained_frame_sha256"]
assert bank["nativeFramesSha256"] == manifest["bank-native-frames.json"]
assert bank["providerSha256"] == wheel["providerSha256"] == provenance["inputs"]["coasters-simulated-track-styles"]["sha256"]
assert wheel["baseSha256"] == provenance["inputs"]["create-coasters-simulated"]["sha256"]
assert bank["placementSourceSha256"] == wheel["placementSourceSha256"]
assert bank["placementSourceSha256"] == provenance["audited_production_sources"]["src/main/java/dev/fourthdimension/physics/ArrowDecorationPlacement.java"]
assert wheel["trackDesignSourceSha256"] == provenance["audited_production_sources"]["src/main/java/dev/fourthdimension/track/TrackDesign.java"]
assert bank["sections"] == 8 and bank["controlSegments"] == 1244
assert bank["turnInterval"] == [-4, 2] and wheel["wheelEnvelopes"] == 20
assert len(bank["models"]) == len(wheel["models"]) == 4
assert len(wheel["nativeSupports"]) == 8
for model in bank["models"] + wheel["models"] + wheel["nativeSupports"]:
    assert model["requiredGap"] == 0.01 and model["passes"]
    assert model["minimumConservativeGap"] >= model["requiredGap"]
legacy_ties = [m for m in wheel["legacyReferenceModels"] if m["resource"].endswith("_tie.obj")]
assert len(legacy_ties) == 2
assert all(not m["passes"] and m["minimumConservativeGap"] == 0 for m in legacy_ties)
assert "excludes riders, chassis, wheels" in bank["scope"]
assert "excludes banking" in wheel["scope"]
print(f"PASS {len(manifest)} payload hashes, pinned inputs, unchanged margins, bounded reports and legacy negative controls")
print("Scope: retained banked control-rail geometry and level-straight wheel envelopes only; no new runtime or full FD401 certification")
