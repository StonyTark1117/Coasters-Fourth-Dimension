#!/usr/bin/env python3
"""Check affected car seats' exported static load paths in model units."""
import json
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/fourthdimension/models/block"
FRAME = [(-2, -1, 0), (-1, -1, 0), (1, -1, 0), (2, -1, 0),
         (-2, -2, 0), (2, -2, 0), (-2, -3, 0), (2, -3, 0), (-2, -4, 0)]
SEATS = [(-1, -4, 0), (0, -4, 0), (1, -4, 0), (2, -4, 0)]


def elements(name, pos, south=False):
    data = json.loads((MODELS / f"{name}.json").read_text())
    offset = [16 * value for value in pos]
    result = []
    for element in data["elements"]:
        lo, hi = element["from"][:], element["to"][:]
        if south:
            # The rear row rotates around the block center; the axle block
            # remains unrotated in the child body.
            lo[0], hi[0] = 16 - hi[0], 16 - lo[0]
            lo[2], hi[2] = 16 - hi[2], 16 - lo[2]
        result.append((tuple(a + b for a, b in zip(lo, offset)),
                       tuple(a + b for a, b in zip(hi, offset))))
    return result


def joined(a, b):
    return all(a[0][axis] <= b[1][axis] and b[0][axis] <= a[1][axis]
               for axis in range(3))


def overlaps(a, b):
    return all(min(a[1][axis], b[1][axis]) - max(a[0][axis], b[0][axis]) > 0
               for axis in range(3))


def contains(box, point):
    return all(box[0][axis] < point[axis] < box[1][axis]
               for axis in range(3))


# The visible parent bearing used to be connected by a horizontal stub at
# pivot height, below the stationary crossbeam. Check that the rendered yoke
# embeds in the actual row flange on both sides, including Revised's lower row.
renderer = (ROOT / "src/main/java/dev/fourthdimension/client/fd917/OriginalCarrierSupportRenderer.java").read_text()
assert "carrier.side-sign*1.75,1.78" in renderer
assert "mesh.beam(strutMount,bearingShoulder" in renderer
assert "mesh.beam(bearingShoulder,bearing" in renderer
for family, row in (("legacy", ("wing_strut", 2, 0)),
                    ("free_spin", ("wing_strut", 2, 3)),
                    ("revised", ("revised_strut", 1, 3)),
                    ("pax", ("wing_strut", 2, 0))):
    model, y, z = row
    for sign in (-1, 1):
        # Renderer at() adds .5 to each plot-center coordinate.
        mount = (16 * (0.5 + sign * 1.25), 16 * (0.5 + 1.78), 16 * (z + 0.5))
        cells = elements(model, (1 if sign > 0 else -1, y, z))
        assert any(contains(box, mount) for box in cells), (family, sign, "bearing yoke misses row flange")
print("original-family parent bearings: both yoke mounts embedded in real row flanges")


for state in ("open", "closed"):
    nodes = elements("fixed_inverted_controller", (0, -1, 0))
    for index, pos in enumerate(FRAME):
        nodes.extend(elements(f"fixed_inverted_frame_{index}", pos))
    cushion_indices = []
    for seat_index, pos in enumerate(SEATS):
        cushion_indices.append(len(nodes))
        suffix = "_outer" if seat_index == 3 else ""
        nodes.extend(elements(f"fixed_inverted_seat_{state}{suffix}", pos))

    reached = {0}
    pending = deque([0])
    while pending:
        current = pending.popleft()
        for candidate, box in enumerate(nodes):
            if candidate not in reached and joined(nodes[current], box):
                reached.add(candidate)
                pending.append(candidate)
    assert all(index in reached for index in cushion_indices), (state, cushion_indices, reached)
    print(f"{state}: controller/frame to all four seat pans connected ({len(reached)} elements)")

    # Arrow's axle is in the block above each child seat. Its old bucket and
    # underpan never met that axle, even though the dynamic crank reached it.
    seat_nodes = elements(f"arrow_seat_{state}", (0, -1, 0))
    axle_nodes = elements("carrier_axle", (0, 0, 0))
    nodes = seat_nodes + axle_nodes
    reached = set(range(len(seat_nodes), len(nodes)))
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for candidate, box in enumerate(nodes):
            if candidate not in reached and joined(nodes[current], box):
                reached.add(candidate)
                pending.append(candidate)
    assert 0 in reached, (state, "Arrow axle does not reach bucket cushion")
    arrow_model = json.loads((MODELS / f"arrow_seat_{state}.json").read_text())
    rear_posts = [part for part in arrow_model["elements"]
                  if part.get("name") == "exposed rear load post"]
    assert {part["from"][0] for part in rear_posts} == {0, 14.5}, state
    assert all(part["to"][2] > 16 for part in rear_posts), state
    assert any(part.get("name") == "rear underseat crossmember"
               and part["to"][2] > 16 for part in arrow_model["elements"]), state
    print(f"{state}: Arrow axle to bucket cushion connected ({len(reached)} elements)")

# All five original families share the stationary crossbeam above a cart-mounted
# controller. Its old upper flange ended four model units below the strut.
controllers = ("arrow_controlled_controller", "zacspin_legacy_controller",
               "sns_free_spin_controller", "zacspin_revised_controller",
               "pax_shuttle_controller")
for name in controllers:
    controller = json.loads((MODELS / f"{name}.json").read_text())
    names = {part.get("name") for part in controller["elements"]}
    assert {"left chassis to crossbeam stanchion",
            "right chassis to crossbeam stanchion",
            "crossbeam support saddle"}.issubset(names), name
    nodes = elements(name, (0, 1, 0))
    strut_start = len(nodes)
    nodes.extend(elements("paired_spine_cross" if name in (
        "sns_free_spin_controller", "zacspin_revised_controller") else "wing_strut", (0, 2, 0)))
    reached = {0}
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for candidate, box in enumerate(nodes):
            if candidate not in reached and joined(nodes[current], box):
                reached.add(candidate)
                pending.append(candidate)
    assert strut_start in reached, f"{name} does not carry the center crossbeam"
    print(f"{name}: cart/controller to center crossbeam connected")

# VehicleFactory now assembles one continuous lower-profile center spine. Only
# row intersections have an upper transverse flange; intermediate cells cannot
# form raised center ribs. Saved cars migrate the same seven old block states.
for family, controller in (("free_spin", "sns_free_spin_controller"),
                           ("revised", "zacspin_revised_controller")):
    revised = family == "revised"
    nodes = elements(controller, (0, 1, 0))
    cells = {(x, 2, 0): "wing_strut" for x in range(-2, 3)}
    cells.update({(0, 2, z): "paired_spine_cross" if z == 0 or not revised and abs(z) == 3
                  else "paired_spine" for z in range(-3, 4)})
    for z in (-3, 3):
        for x in range(-2, 3):
            if x == 0 and not revised:
                continue
            cells[(x, 1 if revised else 2, z)] = "revised_strut" if revised else "wing_strut"
    cell_nodes = {}
    for pos, name in cells.items():
        cell_nodes[pos] = set(range(len(nodes), len(nodes) + len(elements(name, pos))))
        nodes.extend(elements(name, pos))
    assert all(any(joined(nodes[i], nodes[j])
                   for i in cell_nodes[(0, 2, z)] for j in cell_nodes[(0, 2, z+1)])
               for z in range(-3, 3)), (family, "broken center spine")
    spine_model = json.loads((MODELS / "paired_spine.json").read_text())
    assert all(part["to"][1] <= 5.5 for part in spine_model["elements"]), (
        family, "raised center rib")
    if revised:
        assert all(any(overlaps(nodes[i], nodes[j])
                       for i in cell_nodes[(0, 2, z)] for j in cell_nodes[(0, 1, z)])
                   for z in (-3, 3)), "Revised lower row only face-touches the spine"
    reached = {0}
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for candidate, box in enumerate(nodes):
            if candidate not in reached and joined(nodes[current], box):
                reached.add(candidate)
                pending.append(candidate)
    assert all(indices <= reached for indices in cell_nodes.values()), (
        family, "stationary support disconnected")
    print(f"{family}: controller through continuous low center spine to both rows connected")

for family, axle in (("free_spin", "free_spin_axle"),
                     ("revised", "revised_axle"), ("pax", "free_spin_axle"),
                     ("legacy", "legacy_axle")):
    for state in ("open", "closed"):
        paired = family in ("free_spin", "revised")
        for sign in (-1, 1):
            for longitudinal in ((-3, 3) if paired else (0,)):
                axle_nodes = elements(axle, (0, 0, 0)) + elements(axle, (sign, 0, 0))
                nodes = axle_nodes[:]
                cushion_indices = []
                z_positions = (-1, 1) if family == "legacy" else (0,)
                for seat_x in (0, sign):
                    for seat_z in z_positions:
                        south = longitudinal > 0 if paired else seat_z > 0
                        cushion_indices.append(len(nodes))
                        nodes.extend(elements(f"{family}_seat_{state}",
                                              (seat_x, -1, seat_z), south))
                reached = set(range(len(axle_nodes)))
                pending = deque(reached)
                while pending:
                    current = pending.popleft()
                    for candidate, box in enumerate(nodes):
                        if candidate not in reached and joined(nodes[current], box):
                            reached.add(candidate)
                            pending.append(candidate)
                assert all(index in reached for index in cushion_indices), (
                    family, state, sign, longitudinal, "axle misses a seat pan")
        if family in ("free_spin", "pax"):
            model = json.loads((MODELS / f"{family}_seat_{state}.json").read_text())
            assert {part["from"][0] for part in model["elements"]
                    if part.get("name") == "exposed rear load post"} == {0, 14.5}, (family, state)
        if family == "revised":
            parts = json.loads((MODELS / f"{family}_seat_{state}.json").read_text())["elements"]
            webs = [part for part in parts if part.get("name") == "cradle to bearing web"]
            assert {part["from"][0] for part in webs} == {0, 14}, (family, state)
            assert all(part["from"][1] <= 9 and part["to"][1] >= 22.5
                       and part["from"][2] <= 11 and part["to"][2] >= 16
                       for part in webs), (family, state, "visible cradle-to-spine gap")
            assert any(part.get("name") == "rear cradle crossmember"
                       and part["from"][0] == 0 and part["to"][0] == 16
                       and part["from"][1] <= 9 and part["to"][1] >= 11
                       for part in parts), (family, state, "missing pair crossmember")
        print(f"{family} {state}: axle to every occupied seat pan connected across rows and sides")

assert json.loads((ROOT / "src/main/resources/assets/fourthdimension/fd917_articulation/fixed_inverted_seat.json").read_text())["parts"][0]["pivot"] == [0, 29, 15]
print("Fixed Inverted restraint pivot unchanged: [0, 29, 15]")
