#!/usr/bin/env python3
"""Assembled Pipeline floor/pan/shell check; geometry evidence, not ride proof."""
import json
import math
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'b5e78f31b72a6c22dae25219d1eefa1f5de20a3e'
ASSETS = 'src/main/resources/assets/fourthdimension/'


def read(path, baseline=False):
    return json.loads(subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT)
                      if baseline else (ROOT / path).read_bytes())


def model(name, pos, baseline=False):
    data = read(ASSETS + 'models/block/' + name + '.json', baseline)
    return [(name + ':' + str(i),
             tuple(pos[j] + e['from'][j] / 16 for j in range(3)),
             tuple(pos[j] + e['to'][j] / 16 for j in range(3)))
            for i, e in enumerate(data['elements'])]


def distance(a, b):
    return math.sqrt(sum(max(0, a[1][j] - b[2][j], b[1][j] - a[2][j]) ** 2 for j in range(3)))


def connected(boxes, start=0):
    seen = {start}
    pending = [start]
    while pending:
        a = pending.pop()
        for b in range(len(boxes)):
            if b not in seen and distance(boxes[a], boxes[b]) < 1e-9:
                seen.add(b)
                pending.append(b)
    return seen


# Exactly PipelineVehicle.frame() iteration order and native coordinates.
frames = ([(x, -3, z) for z in range(-2, 3) for x in range(-1, 2)]
          + [(x, 0, z) for z in (-2, 2) for x in range(-1, 2)]
          + [(x, y, z) for z in (-2, 2) for x in (-1, 1) for y in (-2, -1)]
          + [(0, -1, 0)])
report = []
for closed in ('open', 'closed'):
    for baseline in (True, False):
        structure = model('pipeline_controller', (0, -2, 0), baseline)
        for i, pos in enumerate(frames):
            structure += model('pipeline_frame_' + str(i), pos, baseline)
        for x, z in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            name = f'pipeline_seat_{int(x > 0)}_{int(z > 0)}_{closed}'
            seat = model(name, (x, -2, z), baseline)
            gap = min(distance(a, b) for a in seat for b in structure)
            reach = connected(structure + seat)
            all_seat_reached = all(i in reach for i in range(len(structure), len(structure) + len(seat)))
            if baseline:
                assert abs(gap - 1 / 16) < 1e-9 and not all_seat_reached
            else:
                assert gap == 0 and all_seat_reached, (name, gap, all_seat_reached)
            report.append(dict(baseline=baseline, seat=name, gap=gap, controller_to_all_seat_parts=all_seat_reached))
        if not baseline:
            # Every existing contact/hinge shell part is identical except the pan underside.
            for x, z in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
                path = ASSETS + f'models/block/pipeline_seat_{int(x > 0)}_{int(z > 0)}_{closed}.json'
                old, new = read(path, True), read(path)
                for part in old['elements']:
                    if part.get('name') != 'side rail cabin pan':
                        assert part in new['elements'], (path, part.get('name'))

# Geometry addition stays within the preexisting assembled cabin envelope.
def envelope(baseline):
    boxes = model('pipeline_controller', (0, -2, 0), baseline)
    for i, pos in enumerate(frames):
        boxes += model('pipeline_frame_' + str(i), pos, baseline)
    for x, z in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        boxes += model(f'pipeline_seat_{int(x > 0)}_{int(z > 0)}_open', (x, -2, z), baseline)
    return [[f(b[k][j] for b in boxes) for j in range(3)] for k, f in ((1, min), (2, max))]

assert envelope(True) == envelope(False)
for path in [ASSETS + 'fd917_articulation/pipeline_seat.json',
             'src/main/java/dev/fourthdimension/vehicle/PipelineVehicle.java']:
    old = subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT)
    assert old == (ROOT / path).read_bytes(), path
for path in (ROOT / ASSETS / 'models/block/fd917').glob('pipeline_restraint*.json'):
    assert read(str(path.relative_to(ROOT)), True) == json.loads(path.read_text())
print(json.dumps({'baseline': BASE, 'results': report, 'envelope': envelope(False),
                  'unchanged': ['native assembly', 'rider/cushion/hinge parts', 'restraint meshes and articulation'],
                  'result': 'PASS'}, indent=2))
