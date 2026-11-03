from pathlib import Path
import hashlib, json
root = Path(__file__).parent
manifest = json.loads((root / "manifest.json").read_text())
for name, expected in manifest["files"].items():
    actual = hashlib.sha256((root / name).read_bytes()).hexdigest()
    if actual != expected: raise SystemExit(f"hash mismatch: {name}")
log = (root / "gametest.log").read_text(errors="replace")
if "All 47 required tests passed :)" not in log: raise SystemExit("terminal native pass record missing")
print("Verified 2 hashes and the 47-test native pass record; packaged replay remains required.")
