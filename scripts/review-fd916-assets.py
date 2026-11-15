#!/usr/bin/env python3
"""Finite authored asset inventory and deterministic model/reference checks; no visual pass claim."""
import collections, hashlib, json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/fourthdimension'
OUT = ROOT / 'docs/evidence/0.9/visual-quality/asset-inventory.json'

def uv_for(face, a, b):
    x,y,z=a; X,Y,Z=b
    return {'down':[x,16-Z,X,16-z], 'up':[x,z,X,Z],
            'north':[16-X,16-Y,16-x,16-y], 'south':[x,16-Y,X,16-y],
            'west':[z,16-Y,Z,16-y], 'east':[16-Z,16-Y,16-z,16-y]}[face]

def group(name):
    for prefix, label in [('powered_inverted','Powered Inverted Yaw'),('new_generation_flying','New-generation Flying'),
       ('fixed_inverted','Fixed Inverted'),('s2_spinning','S2 Spinning'),('spinning','S1 Spinning'),('spin_bearing','S1 Spinning'),
       ('suspended','Suspended'),('roller_ball','Roller Ball'),('pipeline','Pipeline'),('dive','Dive'),('flying','Conventional Flying'),
       ('axis','Axis'),('wing_seat','Wing'),('wing_fixed','Wing'),('fixed_wing','Wing'),('wing_car','Wing'),
       ('arrow','Arrow'),('legacy','Legacy ZacSpin'),('zacspin_legacy','Legacy ZacSpin'),('free_spin','Free Spin'),
       ('sns_free_spin','Free Spin'),('revised','Revised ZacSpin'),('zacspin_revised','Revised ZacSpin'),
       ('pax','PAX'),('shuttle','PAX')]:
        if name.startswith(prefix): return label
    return 'Shared tools/equipment/geometry'

files=[]; issues=[]
for p in sorted(ASSETS.rglob('*')):
    if not p.is_file() or '/ponder/' in str(p): continue
    rel=p.relative_to(ASSETS).as_posix()
    if rel.startswith('lang/'):continue
    raw=p.read_bytes(); record={'path':rel,'group':group(p.stem),'sha256':hashlib.sha256(raw).hexdigest()}
    if p.suffix=='.json':
        d=json.loads(raw)
        for i,e in enumerate(d.get('elements', [])):
            if not all(-16<=a<b<=32 for a,b in zip(e['from'],e['to'])):issues.append({'path':rel,'element':i,'kind':'element_bounds'})
            for f,v in e.get('faces',{}).items():
                uv=v.get('uv',uv_for(f,e['from'],e['to']))
                if any(n<0 or n>16 for n in uv):issues.append({'path':rel,'element':i,'face':f,'kind':'uv_outside_sprite','implicit':'uv' not in v,'uv':uv})
        refs=[]
        def walk(o):
            if isinstance(o,dict):
                for k,v in o.items():
                    if k in ('model','parent') and isinstance(v,str) and v.startswith('fourthdimension:'):refs.append('models/'+v.split(':',1)[1]+'.json')
                    walk(v)
            elif isinstance(o,list):
                for v in o:walk(v)
        walk(d)
        for t in d.get('textures',{}).values():
            if t.startswith('fourthdimension:'):refs.append('textures/'+t.split(':',1)[1]+'.png')
        record['localReferences']=sorted(set(refs))
        for ref in refs:
            if not (ASSETS/ref).is_file():issues.append({'path':rel,'kind':'missing_local_reference','reference':ref})
    files.append(record)
result={'scope':'Authored models, blockstates and textures; procedural rendering reviewed separately. Static flags require criterion review, not automatic family requalification.',
        'counts':dict(collections.Counter(x['group'] for x in files)), 'files':files,'flags':issues}
OUT.write_text(json.dumps(result,indent=2)+'\n')
print(f'{len(files)} files; {len(issues)} static flags in {len(set(x["path"] for x in issues))} files')
print(json.dumps(dict(collections.Counter(x['kind'] for x in issues))))
print('\n'.join(sorted(set(x['path'] for x in issues))))
