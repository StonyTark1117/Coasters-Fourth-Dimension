#!/usr/bin/env python3
"""FD2102's two finite body choices, derived without writing accepted bases."""
from copy import deepcopy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / 'src/main/resources/assets/fourthdimension/models'
OUT = MODELS / 'block/fd917'
COLORS = ('white orange magenta light_blue yellow lime pink gray light_gray cyan '
          'purple blue brown green red black').split()


def load(name):
    return json.loads((MODELS / (name + '.json')).read_text())


def material(element):
    return next(iter(element['faces'].values()))['texture']


def box(template, lo, hi, texture=None):
    result = deepcopy(template)
    result['from'], result['to'] = list(lo), list(hi)
    if texture:
        for face in result['faces'].values():
            face['texture'] = texture
    return result


def wing(base):
    result = deepcopy(base)
    e = result['elements']
    # Narrow, shallow steps; padding, pan, shoe, uprights and hinges stay exact.
    for index, lo, hi in (
        (4, (3,4,14.5), (13,18,15.25)),
        (5, (4,18,14.5), (12,21,15.25)),
        (6, (5,21,13), (11,24,15.25)),
        (7, (5.5,24,13), (10.5,25.5,15.25)),
        (10, (0,5,10), (2,8,12)),
        (12, (14,5,10), (16,8,12))):
        assert material(e[index]) == '#paint'
        e[index] = box(e[index], lo, hi)
    # Explicit rigid hinge load path, exposed beside the narrower fairing.
    e.extend((box(e[9], (1,12,13), (2,20,14.75)),
              box(e[11], (14,12,13), (15,20,14.75)),
              box(e[9], (1,20,14), (15,21,14.75))))
    return result


def inverted(base):
    result = deepcopy(base)
    e = result['elements']
    assert all(material(e[i]) == '#paint' for i in range(2,6))
    # Lower crossmember seats on the retained central metal support; side rails
    # carry the unchanged hinge blocks and leave genuine openings behind pads.
    rails = [((-7,12,14),(7,15,16)),
             ((-7,14,14),(-5,30,16)), ((5,14,14),(7,30,16)),
             ((-5,26,14),(5,28,16)), ((-5,28,14),(5,30,16)),
             ((-4,30,14),(4,32,16))]
    result['elements'] = e[:2] + [box(e[2], lo, hi) for lo,hi in rails] + e[6:]
    return result


def transform(elements, scale, offset):
    result = deepcopy(elements)
    for e in result:
        assert 'rotation' not in e, 'Miniature transform expects axis-aligned base'
        for key in ('from','to'):
            e[key] = [round(x*s+d, 9) for x,s,d in zip(e[key],scale,offset)]
    return result


def wing_item(base, original, alternate):
    result = deepcopy(base)
    # Preserve the accepted chassis, beam, dynamic restraints and presentation.
    for x in (-8,-4,20,24):
        old = transform(original['elements'],(.25,)*3,(x,7,8))
        for element in old:
            # Existing generator has harmless float tails in two contact plates.
            match = next((i for i,e in enumerate(result['elements'])
                          if material(e)==material(element) and
                          all(abs(a-b)<1e-7 for k in ('from','to')
                              for a,b in zip(e[k],element[k]))), None)
            assert match is not None, 'Accepted Wing miniature layout changed'
            result['elements'].pop(match)
        result['elements'].extend(transform(alternate['elements'],(.25,)*3,(x,7,8)))
    return result


def inverted_item(base, inner, outer):
    result = deepcopy(base)
    # Keep the accepted overhead carrier and four-seat presentation. Affine
    # miniatures use the actual alternate fixed geometry + closed restraints,
    # so frame openings and all support joins survive the inventory reduction.
    result['elements'] = deepcopy(base['elements'][:5])
    result['elements'].extend((
        box(base['elements'][0],(0,5,12.6),(16,6.6,13.5)),
        box(base['elements'][0],(0,23,9),(2,24,14)),
        box(base['elements'][0],(14,23,9),(16,24,14))))
    restraint = load('block/fd917/fixed_inverted_restraint')['elements']
    for index,x in enumerate((1.5,5.5,9.5,13.5)):
        seat = outer if index == 3 else inner
        result['elements'].extend(transform(seat['elements']+restraint,
                                           (.1875,.4,.25),(x,2.2,5.5625)))
    return result


def outputs():
    result = {}
    for color in COLORS:
        base = load('block/wing_seat_'+color+'_open')
        assert base == load('block/wing_seat_'+color+'_closed')
        result['body_wing_compact_'+color] = wing(base)
    for suffix in ('','_outer'):
        base = load('block/fixed_inverted_seat_open'+suffix)
        assert base == load('block/fixed_inverted_seat_closed'+suffix)
        result['body_fixed_inverted_open_frame'+suffix] = inverted(base)
    wi,fi = load('item/wing_car'),load('item/fixed_inverted_car')
    result['body_wing_base_item'] = wi
    result['body_fixed_inverted_base_item'] = fi
    result['body_wing_compact_item'] = wing_item(wi,load('block/wing_seat_cyan_open'),
                                               result['body_wing_compact_cyan'])
    result['body_fixed_inverted_open_frame_item'] = inverted_item(fi,
        result['body_fixed_inverted_open_frame'],result['body_fixed_inverted_open_frame_outer'])
    return result


if __name__ == '__main__':
    OUT.mkdir(parents=True,exist_ok=True)
    models = outputs()
    for name,model in models.items():
        (OUT/(name+'.json')).write_text(json.dumps(model,indent=2)+'\n')
    print(f'Generated {len(models)} FD2102 body models; no accepted base writes')
