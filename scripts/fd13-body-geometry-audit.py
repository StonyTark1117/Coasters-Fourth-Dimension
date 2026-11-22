#!/usr/bin/env python3
"""Finite exported-body geometry checks; no runtime or whole-family claims."""
from collections import deque
from copy import deepcopy
import importlib.util
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('body_generator', ROOT/'scripts/generate-body-variants.py')
g = importlib.util.module_from_spec(spec)
spec.loader.exec_module(g)


def touch(a,b):
    overlap = [min(a['to'][k],b['to'][k])-max(a['from'][k],b['from'][k]) for k in range(3)]
    # Actual volume or a shared face with positive area; corner/edge contact is insufficient.
    return min(overlap)>=-1e-8 and sum(v>1e-8 for v in overlap)>=2


def reach(elements,start=0):
    seen,pending = {start},deque([start])
    while pending:
        a=pending.popleft()
        for b in range(len(elements)):
            if b not in seen and touch(elements[a],elements[b]):
                seen.add(b); pending.append(b)
    return seen


def structural(elements):
    return [e for e in elements if g.material(e) not in ('#cushion','#seat','#restraint','#harness','#rubber','#latch')]


def bounds(elements):
    return ([min(e['from'][k] for e in elements) for k in range(3)],
            [max(e['to'][k] for e in elements) for k in range(3)])


def bounded(base,alternate):
    lo,hi=bounds(base['elements']); a,b=bounds(alternate['elements'])
    assert all(a[k]>=lo[k]-1e-8 and b[k]<=hi[k]+1e-8 for k in range(3)),(a,b,lo,hi)


def volume(elements,material):
    return sum((e['to'][0]-e['from'][0])*(e['to'][1]-e['from'][1])*(e['to'][2]-e['from'][2])
               for e in elements if g.material(e)==material)


def local_path(base,alternate):
    old,new=base['elements'],alternate['elements']
    assert all(e in new for e in old if g.material(e)!='#paint'), 'Changed required interface'
    rigid=structural(new); connected=reach(rigid)
    assert len(connected)==len(rigid), 'Disconnected rigid shell/hinge support'
    for pad in (e for e in new if g.material(e) in ('#seat','#cushion')):
        assert any(touch(pad,rigid[i]) for i in connected),'Padding lacks frame contact'
    bounded(base,alternate)
    # Geometric distinction, independent of recoloring or added hidden connectors.
    ratio=volume(new,'#paint')/volume(old,'#paint')
    assert ratio<.70, ('Insufficient shell reduction',ratio)
    return ratio


def shifted(model,pos):
    while 'elements' not in model:
        model=g.load(model['parent'].split(':',1)[1])
    return g.transform(structural(model['elements']),(1,1,1),tuple(x*16 for x in pos))


models={name:json.loads((g.OUT/(name+'.json')).read_text()) for name in g.outputs()}
assert models==g.outputs(), 'Generated export differs from repeatable generator'
for color in g.COLORS:
    base=g.load('block/wing_seat_'+color+'_open')
    alternate=models['body_wing_compact_'+color]
    assert alternate['textures']==base['textures']
    assert alternate['textures']['cushion'].endswith('upholstery_'+color)
    ratio=local_path(base,alternate)
print(f'Wing: 16 dyes; all pads, shoe, pan, uprights, hinge/contact parts exact; rigid face-contact graph connected; paint volume {ratio:.3f} of base')

# Native Wing assembly: controller, beam cells, four replacement seats.
nodes=shifted(g.load('block/wing_fixed_controller'),(0,1,0)); targets=[]
for x in (-4,-3,-2,-1,1,2,3,4):
    seat=abs(x)>=3
    if seat:targets.append(len(nodes))
    nodes+=shifted(models['body_wing_compact_cyan'] if seat else g.load('block/fixed_wing_beam'),(x,1,0))
assert all(i in reach(nodes) for i in targets), 'Wing assembled controller-to-seat break'
print('Wing native four-seat assembly: controller → beams → retained shoes → each pan/hinge path PASS (padding excluded)')

for suffix in ('','_outer'):
    ratio=local_path(g.load('block/fixed_inverted_seat_open'+suffix),models['body_fixed_inverted_open_frame'+suffix])
    print(f'Fixed Inverted{suffix}: pads and all metal/support/hinge parts exact; rigid graph connected; paint volume {ratio:.3f} of base')

nodes=shifted(g.load('block/fixed_inverted_controller'),(0,-1,0)); targets=[]
frame=((-2,-1,0),(-1,-1,0),(1,-1,0),(2,-1,0),(-2,-2,0),(2,-2,0),(-2,-3,0),(2,-3,0),(-2,-4,0))
for i,pos in enumerate(frame):nodes+=shifted(g.load('block/fixed_inverted_frame_'+str(i)),pos)
for x in (-1,0,1,2):
    targets.append(len(nodes))
    nodes+=shifted(models['body_fixed_inverted_open_frame'+('_outer' if x==2 else '')],(x,-4,0))
assert all(i in reach(nodes) for i in targets),'Fixed Inverted gantry-to-seat break'
print('Fixed Inverted native four-seat assembly: controller → gantry/spars → retained underpan rails → every shell/hinge PASS (padding excluded)')

# Real open-frame apertures: no painted element occupies this rear-shell point.
for point in ((0,20,15),(-3,22,15),(3,22,15)):
    def covers(e):return all(e['from'][k]<point[k]<e['to'][k] for k in range(3))
    assert any(covers(e) for e in g.load('block/fixed_inverted_seat_open')['elements'] if g.material(e)=='#paint')
    assert not any(covers(e) for e in models['body_fixed_inverted_open_frame']['elements'] if g.material(e)=='#paint')
print('Open-frame rear shell has three measured apertures formerly occupied by paint; not a palette-only change')

for family,variant in (('wing','compact'),('fixed_inverted','open_frame')):
    base=g.load('item/'+family+'_car')
    assert models['body_'+family+'_base_item']==base
    mini=models['body_'+family+'_'+variant+'_item']
    assert mini['display']==base['display'] and mini['textures']==base['textures']
    bounded(base,mini)
    assert mini['elements']!=base['elements']
    if family=='fixed_inverted':
        rigid=structural(mini['elements'])
        assert len(reach(rigid))==len(rigid), 'Miniature gantry/seat/hinge frame is disconnected'
print('Four picker miniatures: exact base copies, alternate shell geometry, original display/material mapping and bounded extents PASS')
print('PASS: 22 repeatable alternate/picker models; fixed geometry only. Dynamic restraint resources and accepted base files are never written.')
