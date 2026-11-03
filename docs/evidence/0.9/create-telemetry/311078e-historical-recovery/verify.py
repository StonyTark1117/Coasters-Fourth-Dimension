#!/usr/bin/env python3
"""Verify only the recorded historical repair/unload invariants."""
import gzip
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent
read = lambda name: json.loads((root / name).read_text())
artifact = read('artifact.json')
assert artifact['sourceRevision'] == '311078e8229e54514b1e3e0cbb4837bf87066ce5'
assert artifact['jarSha256'] == 'dc60cbdefe35cf2737573e57b1509f790cf82342bea6a81764a49066b2b72fd6'
original = read('input-world.json')
assert original['sourceFoundationSha256'] == original['copyFoundationSha256'] == '7b30ddbf3abf72f1002a3935992d6331d1b89ee2beed15f7ccc100d9098ca821'
records = [json.loads(line) for line in (root / 'observations.jsonl').read_text().splitlines()]
assert [r['case'] for r in records] == [
    'historical-loaded', 'historical-after-capture', 'historical-after-realign',
    'historical-normal-unloaded', 'historical-station-open-restraints',
    'historical-normal-unload-confirmed']
chassis = [543374797, -1922610306, -1252424642, 897751176]
carriers = [[-27598355,1600734704,-1969085482,-1603896950],
            [24911851,19222397,-1282343699,-381322804]]
rider = [940439953,-167562164,-1601161573,-1389718966]
fault = 'End of compatible track; capture and inspect the route'
vehicles = []
for i, record in enumerate(records):
    hits = [v for v in record['savedVehicles'] if v['chassis'] == chassis]
    assert len(hits) == 1
    v = hits[0]; vehicles.append(v)
    assert [c['body'] for c in v['carriers']] == carriers
    assert record['savedRails'] == records[0]['savedRails']
    assert (v['family'],v['variant'],v['boundFrom'],v['boundTo']) == (
        'arrow_controlled','arrow_four_rail_v1',8796093177939,8796093251667)
    assert v['fault'] == fault
    assert v['alignment_required'] == (1 if i < 2 else 0)
    assert v['boarding_ready'] == (0 if i < 2 else 1)
    assert v['restraints'] == (1 if i < 4 else 0)
    assert 'ride_fault' in record['repairDisplayLink']
    assert fault in record['repairSign']
    seats = [s for c in v['carriers'] for s in c['rider_seats']]
    occupants = [p for c in v['carriers'] for p in c['occupants']]
    if i < 5:
        assert seats == [{'seat':274877911039,'player':rider}]
        assert occupants == [rider]
        assert record['availability'].endswith('assignments=1')
        assert 'RootVehicle' in record['players']['Dev']
    else:
        assert not seats and not occupants
        assert record['availability'].endswith('assignments=0')
        assert 'RootVehicle' not in record['players']['Dev']

stopped = read('stopped-foundation.json')['data']
assert stopped['rails'] == records[0]['savedRails']
v = next(v for v in stopped['vehicles'] if v['chassis'] == chassis)
assert [c['body'] for c in v['carriers']] == carriers
assert not v['alignment_required'] and not v['restraints']
assert v['fault'] == fault
assert all(not c['occupants'] and not c['rider_seats'] for c in v['carriers'])
shutdown = read('shutdown-state.json')
for role in ('server','dev','observer'):
    assert shutdown[role]['exitCode'] == 0
    log = gzip.decompress((root / (role+'.log.gz')).read_bytes()).decode()
    assert 'BUILD SUCCESSFUL' in log
    assert ('All dimensions are saved' if role == 'server' else 'Stopping!') in log
assert shutdown['xvfb']['zeroChildWindowsBeforeStop']
manifest = read('manifest.json')
assert set(manifest) == {p.name for p in root.iterdir() if p.is_file() and p.name != 'manifest.json'}
for name, expected in manifest.items():
    assert hashlib.sha256((root / name).read_bytes()).hexdigest() == expected, name
print(f'PASS: {len(records)} observations, historical identity/profile preservation, normal unload, {len(manifest)} file hashes; endpoint warning remains.')
