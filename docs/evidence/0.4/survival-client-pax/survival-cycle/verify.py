#!/usr/bin/env python3
"""Check the retained survival PAX cycle evidence, including the archived save."""

import hashlib
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path


HERE = Path(__file__).resolve().parent
REPO = HERE.parents[4]
WORLD = "FD403PaxSurvivalRetry"
EXPECTED = {
    "client.log.zst": "42360662409e09f6de13bbbe68529b76910368d871133615ee1b65c07f9e6c62",
    "world.tar.zst": "44c6f5c0666d4100cfdae4413109b38a47402242956609ea41b6810356428166",
    "foundation-save.json": "ca381e749205af8350f76dfc87f55103bf2442d11a75be1d06701175f3f01d3f",
}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def archived_data(name):
    return subprocess.run(
        ["tar", "-I", "zstd", "-xOf", str(HERE / "world.tar.zst"), f"{WORLD}/data/{name}"],
        check=True,
        capture_output=True,
    ).stdout


def decode(data):
    with tempfile.NamedTemporaryFile(suffix=".dat") as saved:
        saved.write(data)
        saved.flush()
        result = subprocess.run(
            [sys.executable, str(REPO / "scripts/inspect-foundation-save.py"), saved.name],
            check=True,
            capture_output=True,
            text=True,
        )
    return json.loads(result.stdout)


def main():
    for line in (HERE / "artifact.sha256").read_text().splitlines():
        expected, name = line.split("  ", 1)
        assert digest((HERE / name).read_bytes()) == expected, name
    for name, expected in EXPECTED.items():
        assert digest((HERE / name).read_bytes()) == expected, name

    foundation_raw = archived_data("fourthdimension_foundation.dat")
    assert digest(foundation_raw) == "5ec63a2a3b2fd3fdbe4a216c0f4df7248cb4918688140798c254ac24762f77c5"
    foundation = json.loads((HERE / "foundation-save.json").read_text())
    assert foundation == decode(foundation_raw)

    route = json.loads((HERE / "route-geometry.json").read_text())
    path_raw = archived_data("simulatedcoasters_coaster_paths.dat")
    assert digest(path_raw) == route["raw_path_sha256"]
    paths = decode(path_raw)["data"]["Dimensions"]
    edges = []
    for dimension in paths:
        for edge in dimension["Graph"]["Edges"]:
            bezier = edge.get("Bezier", {})
            positions = [entry["Pos"] for entry in bezier.get("Positions", [])]
            if positions and all(pos[0] == 850 for pos in positions):
                edges.append(
                    {"positions": positions, "axes": [entry["V"] for entry in bezier["Axes"]]}
                )
    assert len(edges) == 5 and edges == route["edges"]
    station = next(
        edge for edge in edges
        if edge["positions"] == [[850, -55, 175], [850, -55, 225]]
    )
    assert all(axis[1] == 0 for axis in station["axes"])

    data = foundation["data"]
    rails = data["rails"][-5:]
    assert len(rails) == 5
    assert all(rail["variant"] == "pax_shuttle_v1" and rail["valid"] == 1 for rail in rails)
    cars = [
        car for car in data["vehicles"]
        if car["boundFrom"] == 233646221623241 and car["boundTo"] == 233646221828041
    ]
    assert len(cars) == 4 and len({tuple(car["chassis"]) for car in cars}) == 4
    for car in cars:
        assert car["family"] == "pax_shuttle" and car["variant"] == "pax_shuttle_v1"
        assert car["boarding_ready"] == 1 and car["stopping"] == 1
        assert car["restraints"] == 0 and car["fault"] == ""
        assert len(car["carriers"]) == 2
        assert all(carrier["capture"] == 1 and not carrier["occupants"] for carrier in car["carriers"])

    log = subprocess.run(
        ["zstd", "-dc", str(HERE / "client.log.zst")],
        check=True,
        capture_output=True,
        text=True,
    ).stdout
    links = re.findall(r"\[cart-link\] linked carts .* at distance 6\.000", log)
    restored = re.findall(r"\[cart-link\] restored link .* \(distance 6\.0\)", log)
    assert len(links) == 3 and len(restored) >= 6
    queries = [line for line in log.splitlines() if "844, -55, 183 has the following block data:" in line]
    assert queries and "shuttle_cycles: 1" in queries[-1]
    final = queries[-1]
    assert 'shuttle_phase: "BOARDING"' in final and 'shuttle_fault: ""' in final
    assert "Speed: 16.0f" in final and final.count("[I;") >= 4
    maximum = float(re.search(r"shuttle_maximum: ([0-9.]+)d", final).group(1))
    station_distance = float(re.search(r"shuttle_station: ([0-9.]+)d", final).group(1))
    assert maximum > station_distance + 25
    assert "Found no elements matching RootVehicle" in log
    assert "BUILD SUCCESSFUL in 26m 52s" in log
    print("PAX survival cycle evidence verified")


if __name__ == "__main__":
    main()
