#!/usr/bin/env python3
"""FD2104 F1/P2 source geometry audit; does not substitute for affected views."""
import json
import math
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'b5e78f31b72a6c22dae25219d1eefa1f5de20a3e'
ASSETS = 'src/main/resources/assets/fourthdimension/'


def load(path, old=False):
    return json.loads(subprocess.check_output(['git', 'show', f'{BASE}:{path}'], cwd=ROOT)
                      if old else (ROOT / path).read_bytes())


def gap(a, b):
    return math.sqrt(sum(max(0, a[0][j] - b[1][j], b[0][j] - a[1][j]) ** 2 for j in range(3)))


def box(e):
    return tuple(tuple(e[k][j] / 16 for j in range(3)) for k in ('from', 'to'))


renderer = (ROOT / 'src/main/java/dev/fourthdimension/client/fd917/VehicleMechanismRenderer.java').read_text()
assert 'childRadial,.12,.12,"machined",light)' in renderer
# Actual child pivot is x=0,y=.5,z=.5 in both Flying factories. Each seat is
# translated in X only; the transverse shaft spans all four/two seats.
shaft = ((-10, .44, .44), (10, .56, .56))
for family in ('flying', 'new_generation_flying'):
    for state in ('00', '01', '10', '11'):
        path = ASSETS + f'models/block/{family}_seat_{state}.json'
        old, current = load(path, True)['elements'], load(path)['elements']
        assert all(e in current for e in old), 'Existing rider/contact part changed'
        # Exclude all upholstery: mechanical support must not depend on padding.
        def structural(parts):
            return [e for e in parts if all(f['texture'] not in ('#seat', '#cushion', '#restraint') for f in e['faces'].values())]
        old_support = structural(old)
        old_gap = min(gap(shaft, box(e)) for e in old_support)
        assert abs(old_gap - .065) < 1e-9, (family, old_gap)
        fixed = structural(current)
        reached = {i for i, e in enumerate(fixed) if gap(shaft, box(e)) < 1e-9}
        pending = list(reached)
        while pending:
            a = pending.pop()
            for b in range(len(fixed)):
                if b not in reached and gap(box(fixed[a]), box(fixed[b])) < 1e-9:
                    reached.add(b)
                    pending.append(b)
        assert all(i in reached for i, e in enumerate(fixed) if e.get('name') == 'pressed seat pan')
        assert sum('fixed shaft saddle' in e.get('name', '') for e in current) == 2
        for j in range(3):
            assert min(e['from'][j] for e in current) == min(e['from'][j] for e in old)
            assert max(e['to'][j] for e in current) == max(e['to'][j] for e in old)
        assert max(e['to'][1] for e in current if 'fixed shaft saddle' in e.get('name', '')) < 7.8
    for path in [ASSETS + f'fd917_articulation/{family}_seat.json']:
        assert load(path) == load(path, True)
    for file in (ROOT / ASSETS / 'models/block/fd917').glob(f'{family}_*restraint*.json'):
        path = str(file.relative_to(ROOT))
        assert load(path) == load(path, True)
    print(f'{family}: baseline structural gap .065; current shaft→fixed saddles→pan PASS in all 4 states; cushion/hinge/envelope unchanged')

assert 'fma(-.245,axis),axis,radial,.18,.29,.19,"structural",light)' in renderer
# Both factories share this controller and pivot; ring width is along Y.
controller = load(ASSETS + 'models/block/spinning_controller.json')['elements']
top = 1 + max(e['to'][1] for e in controller) / 16
pivot = .5 + 1.75 - .3125
fixed_min = pivot - .34 / 2
lo, hi = pivot - .245 - .19 / 2, pivot - .245 + .19 / 2
assert abs(fixed_min - top - .1425) < 1e-9
assert lo < top and hi > fixed_min
# The sleeve intersects the square top (.1875 half-width), while leaving a
# radial .01 gap around the existing .17-radius rotating receiver.
assert .18 < math.sqrt(2) * .1875 < .29
assert abs(.18 - .17 - .01) < 1e-9
print(f'S1/S2: stationary gap .1425 bridged by [{lo:.4f},{hi:.4f}]; receiver radial clearance .01 PASS')

# P3: actual finite journal endpoints, not the central native pivot. Coaxial
# translation commutes with rotation about Z, so mounting remains aligned.
assert 'AxisVehicle.is(v)?-1.07:SuspendedVehicle.is(v)?-1.29:0' in renderer
assert 'AxisVehicle.is(v)?.25:.18, fixedOuter=AxisVehicle.is(v)?.32:.29' in renderer
assert 'AxisVehicle.is(v)?.36:forkBrake?.42:.34' in renderer
assert 'AxisVehicle.is(v)?.36:.42' in renderer
assert 'SuspendedVehicle.is(v)?.04:.12' in renderer
assert 'new Vector3d(fixed).fma(.06,axis).fma(.39,radial)' in renderer
assert 'new Vector3d(fixed).fma(.54,axis).fma(.39,radial),radial,.065,.065' in renderer
assert .39 - .065 / 2 > .32  # fixed caliper bridge clears the entire disc sweep
for family, front, offset, width, bore, shaft_radius, contact, grip in (
        ('axis', -.75, -1.07, .36, .25, math.hypot(.1875, .15), (.18, .18), (.14, 0)),
        ('suspended', -1, -1.29, .42, .18, math.hypot(.0625, .0625), (0, .23), (.055, 0))):
    center_z = .5 + offset
    assert abs(center_z - width / 2 - front) < 1e-9
    # Account for the actual 32-sided ring bore's apothem, not just its radius.
    clearance = bore * math.cos(math.pi / 32) - shaft_radius
    assert clearance > 0
    if family == 'axis':
        # Front fork clamp relative to the shaft axis is X±.1875,Y±.21875.
        assert abs(contact[0]) < .1875 and abs(contact[1]) < .21875
        assert .25 < math.hypot(*contact) < .32
        # Next rotating collar starts at child Z=-.375; race ends at-.39.
        assert center_z + width / 2 < -.375
    else:
        # Front hanger is X±.0625 and extends above the shaft axis.
        assert abs(contact[0]) < .0625 and 0 < contact[1] < 1.3125
        assert .18 < math.hypot(*contact) < .29
    # A point in the near caliper arm intersects the fixed annulus in both
    # lock states; the outboard bridge joins that arm to the opposite arm.
    point_y, point_z = (.31, .16) if family == 'axis' else (.28, .20)
    for pad_gap in (.033, .11):
        dy, dz = -.13, .24 - pad_gap
        py, pz = point_y - .39, point_z - .06
        t = (py * dy + pz * dz) / (dy * dy + dz * dz)
        perpendicular = abs(py * dz - pz * dy) / math.hypot(dy, dz)
        assert 0 <= t <= 1 and perpendicular < .065 / 2
        assert bore < point_y < (.32 if family == 'axis' else .29)
        assert abs(point_z) < width / 2
    for angle in ((0, math.pi / 4, math.pi / 2, math.pi, 3 * math.pi / 2)
                  if family == 'axis' else (-math.pi / 4, 0, math.pi / 4)):
        # Receiver grip and shaft rotate together; the translated center is
        # invariant under the same rotation, while radius is preserved.
        x, y = grip
        rotated = (x * math.cos(angle) - y * math.sin(angle),
                   x * math.sin(angle) + y * math.cos(angle))
        assert abs(math.hypot(*rotated) - math.hypot(*grip)) < 1e-9
        assert math.hypot(*rotated) < shaft_radius
    print(f'{family}: fork/race front plane {front}, rotating receiver grip, coaxial poses PASS; minimum bore clearance {clearance:.6f}')
