#!/usr/bin/env python3
"""FD917 shared editable pixel materials and density-preserving cuboid export.

Importing never writes resources. The JSON pixel maps are authoritative art;
this exporter only resolves their palettes and writes lossless RGBA PNGs.
"""
import argparse
import copy
import itertools
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'assets-source/fd917/materials.json'
TEXTURES = ROOT / 'src/main/resources/assets/fourthdimension/textures/block/fd917'
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
COLORS = ('white','orange','magenta','light_blue','yellow','lime','pink','gray',
          'light_gray','cyan','purple','blue','brown','green','red','black')


def texture(name):
    return 'fourthdimension:block/fd917/' + name


def box(lo, hi, material, name=None):
    """Model units: 16/block. Optional names keep parts editable in model tools."""
    part = {'from': list(lo), 'to': list(hi),
            'faces': {face: {'texture': '#' + material.lstrip('#')} for face in FACES}}
    if name:
        part['name'] = name
    return part


def _segments(lo, hi):
    values = [lo]
    while values[-1] + 16 < hi:
        values.append(values[-1] + 16)
    values.append(hi)
    return list(zip(values, values[1:]))


def density_elements(parts):
    """Tile long faces at 16 px/block; omit newly internal subdivision faces.

    This preserves the outside mesh and rotation origin. Each tile samples a
    bounded sprite rectangle rather than squeezing one sprite across a beam.
    Explicit authored UVs on <=16-unit faces are retained.
    """
    result = []
    for original in parts:
        lo, hi = original['from'], original['to']
        assert all(-16 <= a < b <= 32 for a, b in zip(lo, hi)), original
        split = any(b-a > 16 for a, b in zip(lo, hi))
        for spans in itertools.product(*(_segments(a,b) for a,b in zip(lo,hi))):
            a = [v[0] for v in spans]; b = [v[1] for v in spans]
            x,y,z = [v-u for u,v in zip(a,b)]
            axes = {'north':(2,False),'south':(2,True),'west':(0,False),
                    'east':(0,True),'down':(1,False),'up':(1,True)}
            dimensions = {'north':(x,y),'south':(x,y),'east':(z,y),
                          'west':(z,y),'up':(x,z),'down':(x,z)}
            part = {k:copy.deepcopy(v) for k,v in original.items() if k not in ('from','to','faces')}
            part.update({'from':a,'to':b,'faces':{}})
            for face, value in original['faces'].items():
                axis, positive = axes[face]
                if (b[axis] != hi[axis] if positive else a[axis] != lo[axis]):
                    continue
                f = copy.deepcopy(value)
                if split or 'uv' not in f:
                    f['uv'] = [0,0,*dimensions[face]]
                assert all(0 <= v <= 16 for v in f['uv']), (face,f)
                part['faces'][face] = f
            if part['faces']:
                result.append(part)
    return result


def materials(color='red', padding=None):
    """Shared vocabulary, not shared silhouettes. Particle matches family paint."""
    return {'metal':texture('structural'), 'body':texture('paint_'+color),
            'paint':texture('paint_'+color), 'accent':texture('paint_'+color),
            'cushion':texture('upholstery_'+(padding or color)),
            'seat':texture('upholstery_'+(padding or color)),
            'restraint':texture('restraint'), 'harness':texture('restraint'),
            'contact':texture('machined'), 'bolt':texture('fastener'),
            'rubber':texture('rubber'), 'latch':texture('paint_orange'),
            'particle':texture('paint_'+color)}


def model(parts, color='red', padding=None, overrides=None):
    palette = materials(color,padding)
    palette.update(overrides or {})
    return {'parent':'minecraft:block/block', 'ambientocclusion':True,
            'textures':palette, 'elements':density_elements(parts)}


def item_display(scale=.65):
    return {'gui':{'rotation':[25,225,0],'translation':[0,-1,0],'scale':[scale]*3},
            'ground':{'translation':[0,2,0],'scale':[.30]*3},
            'fixed':{'rotation':[0,180,0],'scale':[.5]*3},
            'firstperson_righthand':{'rotation':[0,135,0],'translation':[0,1,0],'scale':[.38]*3},
            'firstperson_lefthand':{'rotation':[0,225,0],'translation':[0,1,0],'scale':[.38]*3},
            'thirdperson_righthand':{'rotation':[75,45,0],'translation':[0,2,0],'scale':[.35]*3},
            'thirdperson_lefthand':{'rotation':[75,225,0],'translation':[0,2,0],'scale':[.35]*3}}


def export_textures():
    from PIL import Image
    data = json.loads(SOURCE.read_text())
    TEXTURES.mkdir(parents=True,exist_ok=True)
    paths = []
    for name, entry in data['materials'].items():
        pattern = data['patterns'][entry['pattern']]
        assert len(pattern)==16 and all(len(row)==16 for row in pattern), name
        palette = {k:tuple(bytes.fromhex(v))+ (255,) for k,v in entry['palette'].items()}
        im = Image.new('RGBA',(16,16)); im.putdata([palette[c] for row in pattern for c in row])
        p = TEXTURES/(name+'.png'); im.save(p,optimize=False); paths.append(p)
    return paths


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--export',action='store_true',required=True)
    parser.parse_args()
    print('Exported',len(export_textures()),'authored FD917 material tiles')
