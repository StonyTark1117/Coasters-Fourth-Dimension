#!/usr/bin/env python3
"""Verify bounded legacy/Free Spin combined-pack evidence, including failed first layout."""
import json
import math
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
def saved(name):
    return json.loads((root / (name + '.json')).read_text())['data']
def vehicle(data, family):
    return next(v for v in data['vehicles'] if v['family'] == family)
def packets(name):
    rows = []
    for line in (root / name).read_text().splitlines():
        if 'FD_SNAPSHOT ' in line:
            r = dict(re.findall(r'(\w+)=([^ ]+)', line.split('FD_SNAPSHOT ', 1)[1]))
            rows.append(tuple(r[k] for k in ('player', 'dimension', 'tick', 'sha256', 'railsSha256', 'rails', 'vehicles')))
    return rows

before, frozen, resumed = (saved(n) for n in ('occupied-before', 'occupied-restarted-frozen', 'occupied-resumed'))
assert before == frozen
assert before['schema'] == 5 and len(before['rails']) == 16
assert before['rails'] == resumed['rails']
for family in ('zacspin_legacy', 'sns_free_spin'):
    a, b = vehicle(before, family), vehicle(resumed, family)
    assert a['restraints'] and a['boarding_ready'] and not a['fault']
    assert sum(len(c['occupants']) for c in a['carriers']) == 1
    assert {k:v for k,v in a.items() if k != 'carriers'} == {k:v for k,v in b.items() if k != 'carriers'}
    for c, d in zip(a['carriers'], b['carriers']):
        assert {k:v for k,v in c.items() if k not in ('angle','omega')} == {k:v for k,v in d.items() if k not in ('angle','omega')}
    running = vehicle(saved('running'), family)
    assert not running['stopping'] and running['restraints'] and not running['fault']
    exited = vehicle(saved('exited'), family)
    assert exited['boarding_ready'] and not exited['restraints'] and not exited['fault']
    assert all(not c['occupants'] and not c['rider_seats'] for c in exited['carriers'])

# Preserve the failure; rescue/configuration changes do not count as an original-layout pass.
stalled = vehicle(saved('stalled'), 'sns_free_spin')
assert not stalled['stopping'] and not stalled['boarding_ready'] and not stalled['fault']
assert 'Kind: "TRIM", Value: 8' in (root/'stall.txt').read_text()
assert 'Speed: ' in (root/'stall.txt').read_text()
assert vehicle(saved('stalled'), 'zacspin_legacy')['boarding_ready']
for name, expected in [('display-open','1 / 8'), ('display-open','Restraints closed: No'),
                       ('display-open','All carriers must be captured'), ('display-ready','Ready to dispatch'),
                       ('display-retained','Release maintenance retention'), ('fs-display-occupied','1 / 8')]:
    assert expected in (root/(name+'.txt')).read_text()
assert (root/'display-exited.txt').read_text().count('0 / 8') == 2

server = set(packets('server1-events.txt') + packets('server2-events.txt'))
report = {'scope': 'Two local clients; combined pack; legacy and Free Spin occupied cold restart. Original Free Spin circuit stalled.',
          'cold_restart_exact': True, 'clients': {}}
received = {}
for name, file in [('Dev','dev1-events.txt'),('Observer','observer1-events.txt')]:
    rows = packets(file)
    assert len(rows) > 100 and all(r in server for r in rows)
    received[name] = {r[1:] for r in rows}
    camera = []
    for line in (root/file).read_text().splitlines():
        if 'FD_IMMERSION' not in line:
            continue
        r = dict(re.findall(r'(\w+)=([^ ]+)', line.split(' camera=',1)[0]))
        assert r['metric'] == 'normalized-v1'
        for key in ('rawError','carrierUp','cameraUp','pitch','travel'):
            r[key] = float(r[key]); assert math.isfinite(r[key])
        camera.append(r)
    mounted = [r for r in camera if r['riding']=='true']
    assert len(mounted) > 100 and len({r['body'] for r in mounted}) == 1
    assert all(r['body'] != 'none' and 0 <= r['rawError'] < 1e-6 for r in mounted)
    exits = [b for a,b in zip(camera,camera[1:]) if a['riding']=='true' and b['riding']=='false']
    assert exits
    last = exits[-1]
    assert last['body']=='none' and last['travel']==0
    assert abs(last['cameraUp'] - math.cos(math.radians(last['pitch']))) < .001
    report['clients'][name] = {'matched_packets':len(rows),'mounted_samples':len(mounted),
        'carrier':mounted[0]['body'],'minimum_carrier_up':min(r['carrierUp'] for r in mounted),
        'maximum_raw_error_radians':max(r['rawError'] for r in mounted),'normal_exit_restored':True}
report['identical_both_client_snapshots'] = len(received['Dev'] & received['Observer'])
assert report['identical_both_client_snapshots'] > 100

# The layout-only candidate completed both circuits and an occupied station restart.
fixed = saved('fixed-returned')
assert saved('fixed-cold-before') == saved('fixed-cold-frozen')
for family in ('zacspin_legacy','sns_free_spin'):
    v = vehicle(fixed,family)
    assert v['stopping'] and v['boarding_ready'] and v['restraints'] and not v['fault']
    assert sum(len(c['occupants']) for c in v['carriers']) == 1
    assert not vehicle(saved('fixed-running'),family)['stopping']
    e = vehicle(saved('fixed-exited'),family)
    assert e['boarding_ready'] and not e['restraints'] and not e['fault']
    assert all(not c['occupants'] and not c['rider_seats'] for c in e['carriers'])
assert (root/'fixed-return-status.txt').read_text().count('Ready to dispatch') == 2
assert (root/'fixed-resumed-ready.txt').read_text().count('Ready to dispatch') == 2
assert 'All 24 required tests passed' in (root/'build2.txt').read_text()

def last_camera(name):
    line = (root/name).read_text().splitlines()[-1]
    fields = dict(re.findall(r'(\w+)=([^ ]+)',line.split(' camera=',1)[0]))
    q = [float(x) for x in re.findall(r'[+-]?\d\.\d+E[+-]\d+',line.split('camera=',1)[1])]
    return fields, q
for name in ('dev','observer'):
    a,q = last_camera(name+'-cold-before.txt')
    b,r = last_camera(name+'-cold-after.txt')
    assert a['body'] == b['body'] != 'none'
    dot=abs(sum(x*y for x,y in zip(q,r)))/math.sqrt(sum(x*x for x in q)*sum(x*x for x in r))
    angle=math.degrees(2*math.acos(min(1,dot)))
    assert angle < .25
    report['clients']['Dev' if name=='dev' else 'Observer']['cold_view_delta_degrees']=angle
report['layout_fixed_two_client_circuit_and_cold_restart']=True
assert 'All 24 required tests passed' in (root/'build4.txt').read_text()
assert (root/'build4.txt').read_text().count('controller=COAST') == 3
assert 'Held returning car disappeared' in (root/'build3.txt').read_text()
report['combined_native_held_presence_regression_passed'] = True
assert saved('final-cold-before') == saved('final-cold-frozen')
for family in ('zacspin_legacy','sns_free_spin'):
    v = vehicle(saved('final-returned'),family)
    assert v['stopping'] and v['boarding_ready'] and v['restraints'] and not v['fault']
    assert sum(len(c['occupants']) for c in v['carriers']) == 1
    assert not vehicle(saved('final-running'),family)['stopping']
    a, b = vehicle(saved('final-cold-before'),family), vehicle(saved('final-resumed'),family)
    assert {k:v for k,v in a.items() if k!='carriers'} == {k:v for k,v in b.items() if k!='carriers'}
    for x,y in zip(a['carriers'],b['carriers']):
        assert {k:v for k,v in x.items() if k not in ('angle','omega')} == {k:v for k,v in y.items() if k not in ('angle','omega')}
    e = vehicle(saved('final-exited'),family)
    assert e['boarding_ready'] and not e['restraints'] and not e['fault']
    assert all(not c['occupants'] and not c['rider_seats'] for c in e['carriers'])
for name in ('final-return','final-resumed-readouts'):
    text = (root/(name+'.txt')).read_text()
    assert text.count('Ready to dispatch') == 2
    assert text.count('CoastersEngineeredStep: 2') == 2
for name in ('engineered-only','base'):
    text=(root/(name+'.txt')).read_text()
    assert 'All 24 required tests passed' in text
    assert text.count('_SKIPPED:') == (2 if name=='engineered-only' else 3)
assert (root/'engineered-only.txt').read_text().count('controller=COAST') == 3
final_server = set(packets('server5-events.txt')+packets('server6-events.txt'))
report['final_artifact'] = {}
for name,file in [('Dev','dev3-events.txt'),('Observer','observer3-events.txt')]:
    rows=packets(file)
    assert len(rows)>50 and all(r in final_server for r in rows)
    a,q=last_camera('final-'+name.lower()+'-cold-before.txt')
    b,r=last_camera('final-'+name.lower()+'-cold-after.txt')
    assert a['body']==b['body']!='none'
    dot=abs(sum(x*y for x,y in zip(q,r)))/math.sqrt(sum(x*x for x in q)*sum(x*x for x in r))
    angle=math.degrees(2*math.acos(min(1,dot)))
    assert angle<.25
    report['final_artifact'][name]={'matched_packets':len(rows),'cold_view_delta_degrees':angle}
for name in ('server5','server6','dev3','observer3'):
    text=(root/(name+'-mods.txt')).read_text()
    for version in ('Track Styles 1.2.0','Engineered 1.1.1','Immersion 0.2.3','Magnetized 1.1.0','Additions 0.2.0'):
        assert version in text,(name,version)
report['final_cold_restart_exact']=True
report['final_controllers_still_coast_after_cold_restart']=True
print(json.dumps(report,indent=2))
