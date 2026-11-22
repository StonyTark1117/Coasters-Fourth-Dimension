#!/usr/bin/env python3
"""Check the transferred 1.2 operator-path evidence links and boundaries."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / "docs/evidence/1.2/art/operator-path-audit.md"


def main() -> None:
    text = EVIDENCE.read_text(encoding="utf-8")
    required = (
        "OperatorScreen",
        "OperatorPanel.sample",
        "EditorNetwork",
        "ExampleLayouts",
        "Mechanisms owns occupied admission",
    )
    missing = [token for token in required if token not in text]
    if missing:
        raise SystemExit("missing audit markers: " + ", ".join(missing))
    print(f"PASS: {EVIDENCE} contains {len(required)} operator-path markers")


if __name__ == "__main__":
    main()
