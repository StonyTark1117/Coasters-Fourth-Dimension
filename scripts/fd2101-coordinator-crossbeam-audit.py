#!/usr/bin/env python3
"""Source-derived stationary assembly views; not a substitute for in-game proof.

Reconstructs VehicleFactory's original paired-row block placements and current
JSON elements. Coordinates are relative to the parent plot block corner. The
procedural support renderer and moving seats are deliberately excluded so they
cannot hide disconnected baked beam cells. No production assets are written.
"""
import hashlib
import json
import sys
from collections import deque
from pathlib import Path

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/main/resources/assets/fourthdimension'
OUT = ROOT / 'docs/evidence/1.3/fd2101'

def assembly(revised):
    cells = {(0, 1, 0): 'zacspin_revised_controller' if revised else 'sns_free_spin_controller'}
    cells.update({(x, 2, 0): 'wing_strut' for x in range(-2, 3)})
    if revised:
        cells.update({(x, 1, z): 'revised_strut' for z in (-3, 3) for x in range(-2, 3)})
    cells.update({(0, 2, z): 'wing_strut' for z in range(-3, 4)})
    if not revised:
        cells.update({(x, 2, z): 'wing_strut' for z in (-3, 3) for x in range(-2, 3)})
    boxes = []
    for pos, model in cells.items():
        for part in json.loads((BASE / f'models/block/{model}.json').read_text())['elements']:
            assert 'rotation' not in part, 'Update audit for rotated elements'
            boxes.append({'cell': pos, 'model': model, 'name': part.get('name'),
                          'lo': [pos[i] + part['from'][i] / 16 for i in range(3)],
                          'hi': [pos[i] + part['to'][i] / 16 for i in range(3)]})
    return boxes

def touches(a, b):
    return all(a['lo'][i] <= b['hi'][i] + 1e-9 and b['lo'][i] <= a['hi'][i] + 1e-9 for i in range(3))

def proposed_repair(revised):
    """Bounds matching the companion renderer patch, converted from center coordinates."""
    result = []
    def add(name, lo, hi):
        result.append({'cell': (0, 2, 0), 'model': 'PROPOSED renderer member', 'name': name,
                       'lo': [v + .5 for v in lo], 'hi': [v + .5 for v in hi]})
    gaps = [(i+.25, i+.75) for i in range(-3, 3)] + [(-3.5, -3.25), (3.25, 3.5)]
    for z0, z1 in gaps:
        add('lower flange gap strip', [-.25, 1.75, z0], [.25, 1.84375, z1])
        add('upper flange gap strip', [-.25, 2.15625, z0], [.25, 2.25, z1])
    add('continuous Z web', [-.0625, 1.84375, -3.5], [.0625, 2.15625, 3.5])
    if revised:
        for z in (-3, 3):
            add('lower row riser', [-.25, 1.375, z-.25], [.25, 2, z+.25])
    return result

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for name in ('wing_strut', 'revised_strut'):
        state = json.loads((BASE / f'blockstates/{name}.json').read_text())
        assert state['variants'][''].get('x', 0) == state['variants'][''].get('y', 0) == 0
    fig, axes = plt.subplots(2, 3, figsize=(15, 8), layout='constrained')
    records = {}
    proposed = '--proposed-repair' in sys.argv
    suffix = '-proposed' if proposed else ''
    for row, revised in enumerate((False, True)):
        family = 'Revised' if revised else 'Free Spin'
        boxes = assembly(revised)
        if proposed:
            boxes += proposed_repair(revised)
        reached = {0}
        pending = deque(reached)
        while pending:
            i = pending.popleft()
            for j, candidate in enumerate(boxes):
                if j not in reached and touches(boxes[i], candidate):
                    reached.add(j)
                    pending.append(j)
        disconnected = sorted({tuple(b['cell']) for i, b in enumerate(boxes) if i not in reached})
        if proposed:
            assert not disconnected, (family, 'proposed stationary repair is disconnected', disconnected)
        records[family] = {'baked_disconnected_cells': disconnected,
                           'adjacent_spine_cell_z_gap_blocks': 0.5,
                           'boxes': boxes}
        for col, (a, b, title) in enumerate(((0, 2, 'TOP: X / Z'), (2, 1, 'SIDE: Z / Y'), (0, 1, 'FRONT: X / Y'))):
            ax = axes[row, col]
            for i, box in enumerate(boxes):
                lo, hi = box['lo'], box['hi']
                color = '#137e6d' if i in reached else '#d54c32'
                ax.add_patch(Rectangle((lo[a], lo[b]), hi[a]-lo[a], hi[b]-lo[b],
                                       facecolor=color, edgecolor='#172e40', alpha=.35, linewidth=.5))
            ax.set_xlim((-3.8, 4.8) if a == 2 else (-3, 4))
            ax.set_ylim((-3.8, 4.8) if b == 2 else (0.5, 3.1))
            ax.set_aspect('equal')
            ax.grid(alpha=.2)
            ax.set_title(f'{family} — {title}')
            ax.set_xlabel('XYZ'[a] + ' (blocks)')
            ax.set_ylabel('XYZ'[b] + ' (blocks)')
    fig.suptitle(('PROPOSED repair + baked geometry' if proposed else 'BAKED stationary geometry only') +
                 ' • green: connected to controller • red: disconnected\n'
                 'Seats, track and perspective excluded; projection overlap alone does not prove 3D contact.')
    fig.savefig(OUT / f'coordinator-crossbeam-views{suffix}.svg')
    fig.savefig(OUT / f'coordinator-crossbeam-views{suffix}.png', dpi=150)
    inputs = ['src/main/java/dev/fourthdimension/vehicle/VehicleFactory.java',
              'src/main/java/dev/fourthdimension/client/fd917/OriginalCarrierSupportRenderer.java']
    inputs += [str(p.relative_to(ROOT)) for p in sorted((BASE / 'models/block').glob('*strut.json'))]
    inputs += [str((BASE / f'models/block/{name}.json').relative_to(ROOT))
               for name in ('zacspin_revised_controller', 'sns_free_spin_controller')]
    inputs += [str((BASE / f'blockstates/{name}.json').relative_to(ROOT))
               for name in ('wing_strut', 'revised_strut')]
    inputs += ['docs/evidence/1.3/fd2101/coordinator-crossbeam-repair.patch']
    report = {'scope': 'baked geometry; reconstructed factory placements; contact includes face-touch',
              'proposed_renderer_patch_included': proposed,
              'input_sha256': {p: hashlib.sha256((ROOT / p).read_bytes()).hexdigest() for p in inputs},
              'families': records}
    (OUT / f'coordinator-crossbeam-audit{suffix}.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps({k: {a:b for a,b in v.items() if a != 'boxes'} for k,v in records.items()}, indent=2))

if __name__ == '__main__':
    main()
