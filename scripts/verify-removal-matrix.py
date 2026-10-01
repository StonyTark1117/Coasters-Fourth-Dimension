#!/usr/bin/env python3
"""Verify the bounded, unoccupied schema-5 dedicated-server removal matrix."""
import collections
import copy
import json
import math
import re
import sys
import uuid
from pathlib import Path

root = Path(sys.argv[1])
baseline = json.loads((root / 'baseline.json').read_text())['data']
expected_families = {'arrow_controlled': 1, 'zacspin_legacy': 1, 'sns_free_spin': 1,
                     'zacspin_revised': 1, 'pax_shuttle': 4}

def stable(data):
    result = copy.deepcopy(data)
    for vehicle in result['vehicles']:
        for carrier in vehicle['carriers']:
            # Only these live physics measurements may settle differently after reload.
            carrier.pop('angle')
            carrier.pop('omega')
    return result

assert baseline['schema'] == 5 and len(baseline['rails']) == 35
assert collections.Counter(v['family'] for v in baseline['vehicles']) == expected_families
assert sum(len(v['carriers']) for v in baseline['vehicles']) == 20
assert sum(bool(r['magnets']) for r in baseline['rails']) == 1
assert sum(r['appearance_style'].endswith('arrow_4d_dbl_spine') for r in baseline['rails']) == 1
exact_versions = {'engineered': 'Engineered 1.1.1', 'styles': 'Track Styles 1.2.0',
                  'additions': 'Additions 0.2.0', 'magnetized': 'Magnetized 1.1.0',
                  'immersion': 'Immersion 0.2.3'}
pax_ids = {str(uuid.UUID(int=sum((word & 0xffffffff) << (96 - 32 * i)
                               for i, word in enumerate(v['chassis']))))
           for v in baseline['vehicles'] if v['family'] == 'pax_shuttle'}
report = {'scope': 'Eight parked, unoccupied vehicles across all five families', 'runs': {}}
versions = {'engineered': 'coastersengineered', 'styles': 'coasterssimulatedextratypes',
            'additions': 'coasterfins', 'magnetized': 'coastersmagnetized',
            'immersion': 'coasters_immersion'}
for case in ['control'] + ['without-' + m for m in versions] + ['base', 'reinstalled']:
    data = json.loads((root / (case + '.json')).read_text())['data']
    assert stable(data) == stable(baseline), (case, 'saved identity/mechanics changed')
    for v in data['vehicles']:
        assert v['fault'] == '' and v['stopping'] == 1 and v['boarding_ready'] == 1
        assert v['restraints'] == 0
        for c in v['carriers']:
            assert c['capture'] == 1 and c['occupants'] == []
            assert math.isfinite(c['angle']) and math.isfinite(c['omega'])
    log = (root / (case + '.txt')).read_text()
    assert 'Done (' in log and 'BUILD SUCCESSFUL' in log
    pairs = {tuple(sorted((a, b))) for a, b in re.findall(r'restored link ([\w-]+) <-> ([\w-]+) \(distance 6.0\)', log)}
    assert len(pairs) == 3 and {v for p in pairs for v in p} == pax_ids, (case, pairs)
    commands = (root / (case + '-commands.txt')).read_text()
    assert 'Stepping 200 tick(s)' in commands and 'shuttle_phase: "BOARDING"' in commands
    absent = {'base', 'reinstalled', 'without-additions'}
    stations = commands.count('id: "coasterfins:station"')
    fins = commands.count('id: "coasterfins:coaster_fin"')
    assert stations == fins == (0 if case in absent else 5), (case, stations, fins)
    motor = 'id: "coastersengineered:linear_motor_controller"' in commands
    assert motor == (case not in {'base', 'reinstalled', 'without-engineered'})
    mods = (root / (case + '-mods.txt')).read_text()
    for name, modid in versions.items():
        present = case != 'base' and case != 'without-' + name
        assert ('(' + modid + ')' in mods) == present
        if present: assert exact_versions[name] in mods
    report['runs'][case] = {'rails': 35, 'vehicles': 8, 'carriers': 20,
                           'restored_pax_links': len(pairs), 'addon_stations': stations,
                           'addon_fins': fins, 'engineered_controller': motor}
air = set(re.findall(r'\[Rcon\] FD_REMOVED_BLOCK_AIR (\w+) (\w+)',
                     (root / 'reinstalled.txt').read_text()))
assert air == {(family, item) for family in expected_families for item in ('station', 'fin')} | {('arrow_controlled', 'engineered_controller')}
report['reinstalled_missing_blocks_confirmed_air'] = len(air)
print(json.dumps(report, indent=2))
