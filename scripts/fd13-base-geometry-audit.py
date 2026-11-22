#!/usr/bin/env python3
"""Affected assembled Wing load path and bounded base-refinement regressions.

This checks exported geometry, not rendered pixels or native physics. Baseline
is the integrated 1.2/FD2101 commit; it deliberately does not audit 17 styles.
"""
import json
import subprocess
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = Path('src/main/resources/assets/fourthdimension')
BASE = 'b5e78f31b72a6c22dae25219d1eefa1f5de20a3e'


def load(path, baseline=False):
    relative = ASSETS / path
    return json.loads(subprocess.check_output(['git', 'show', f'{BASE}:{relative}'], cwd=ROOT)
                      if baseline else (ROOT / relative).read_text())


def boxes(name, position, baseline=False):
    model = load(f'models/block/{name}.json', baseline)
    if 'elements' not in model:
        return boxes(model['parent'].split('block/')[1], position, baseline)
    return [(tuple(p + 16*d for p, d in zip(e['from'], position)),
             tuple(p + 16*d for p, d in zip(e['to'], position))) for e in model['elements']]


def connected(nodes):
    reached = {0}
    pending = deque(reached)
    while pending:
        i = pending.popleft()
        for j, b in enumerate(nodes):
            if j not in reached and all(nodes[i][0][k] <= b[1][k] and b[0][k] <= nodes[i][1][k]
                                        for k in range(3)):
                reached.add(j)
                pending.append(j)
    return reached


def wing(baseline=False):
    # Exact cells in WingVehicle.create; seats overwrite beam cells.
    source = (ROOT / 'src/main/java/dev/fourthdimension/vehicle/WingVehicle.java').read_text()
    for x in (-4, -3, 3, 4):
        assert f'new BlockPos({x}, 1, 0)' in source
    nodes = boxes('wing_fixed_controller', (0, 1, 0), baseline)
    pans = []
    for x in (-4, -3, -2, -1, 1, 2, 3, 4):
        seat = abs(x) >= 3
        if seat:
            pans.append(len(nodes))
        nodes += boxes('wing_seat_cyan_open' if seat else 'fixed_wing_beam', (x, 1, 0), baseline)
    reached = connected(nodes)
    return [i in reached for i in pans]


assert all(wing(True)), 'Baseline arms already provide a load path; this is refinement'
assert all(wing()), 'Wing controller/beam/shoe path does not reach all four pans'
print('Wing: baseline arm path and refined mounting-shoe path both reach all four pans')

# Unchanged first two elements define the rider-contact cushion positions.
# Added shoe sits below them; tapered shell removes volume behind the rider.
for name in ('wing_seat_cyan_open', 'fixed_inverted_seat_open', 'fixed_inverted_seat_open_outer'):
    old = load(f'models/block/{name}.json', True)['elements']
    new = load(f'models/block/{name}.json')['elements']
    for i in range(2):
        assert (new[i]['from'], new[i]['to']) == (old[i]['from'], old[i]['to']), name
    for k in range(3):
        assert min(e['from'][k] for e in new) >= min(e['from'][k] for e in old), name
        assert max(e['to'][k] for e in new) <= max(e['to'][k] for e in old), name
    print(f'{name}: cushion positions and outer bounds preserved')

for family in ('wing', 'fixed_inverted'):
    for path in (f'fd917_articulation/{family}_seat.json', f'models/block/fd917/{family}_restraint.json'):
        assert load(path) == load(path, True), path
    print(f'{family}: restraint mesh, pivot and open angle unchanged')

# Every Wing dye still changes upholstery; the refined shell is neutral paint.
from fd917_materials import COLORS
for color in COLORS:
    for state in ('open', 'closed'):
        model = load(f'models/block/wing_seat_{color}_{state}.json')
        assert model['textures']['cushion'].endswith('upholstery_' + color)
        assert model['textures']['paint'].endswith('paint_gray')
print('All 16 Wing dyes retain their upholstery mapping')
