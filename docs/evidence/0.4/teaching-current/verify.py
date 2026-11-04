#!/usr/bin/env python3
"""Check the saved current-artifact teaching evidence."""

from hashlib import sha256
from pathlib import Path
import subprocess
from struct import unpack


ROOT = Path(__file__).resolve().parent
SCENES = {
    "matching", "construction", "arrow", "legacy", "free_spin",
    "revised", "magnets", "boarding", "pax",
}


def png_size(path: Path) -> tuple[int, int]:
    header = path.read_bytes()[:24]
    assert header[:8] == b"\x89PNG\r\n\x1a\n", path
    return unpack(">II", header[16:24])


def main() -> None:
    for line in (ROOT / "evidence.sha256").read_text().splitlines():
        digest, relative = line.split("  ", 1)
        path = ROOT / relative
        assert path.is_file(), relative
        assert sha256(path.read_bytes()).hexdigest() == digest, relative

    for number in range(1, 16):
        assert png_size(ROOT / "tooltips" / f"{number:02d}.png") == (854, 480), number
    for scene in SCENES:
        assert png_size(ROOT / "ponder" / f"{scene}-full.png") == (854, 480), scene
        width, height = png_size(ROOT / "ponder" / f"{scene}-captions.png")
        assert width == 816 and height >= 690, scene

    log = subprocess.check_output(["zstd", "-dc", str(ROOT / "client.log.zst")], text=True)
    assert log.count("FD_PONDER item=") == 53
    assert "FD_PONDER passed items=15 registrations=53 uniqueScenes=9" in log
    assert "ThreadedAnvilChunkStorage: All dimensions are saved" in log
    assert "[minecraft/Minecraft]: Stopping!" in log
    assert "BUILD SUCCESSFUL in 8m 51s" in log
    assert "OutOfMemoryError" not in log
    print("PASS: 15 tooltips, 9 rendered scenes/caption sheets, 53 playbacks, normal shutdown, hashes")


if __name__ == "__main__":
    main()
