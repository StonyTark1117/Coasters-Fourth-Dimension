#!/usr/bin/env python3
"""Check recorded dev.14 boarding recovery/holding evidence; never certifies all FD902 cases."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'docs/evidence/0.9'


def read(name):
    return json.loads((root / name).read_text())


def keyed(data):
    return {tuple(v['chassis']): v for v in data['vehicles']}


names = ['caught-before-recovery-save.json', 'caught-recovered-save.json',
         'caught-control-cold-save.json', 'caught-fixed-edge-save.json', 'caught-fixed-final-save.json']
before, recovered, control, edge, final = [read(name)['data'] for name in names]
all_data = [before, recovered, control, edge, final]
vehicles = [keyed(data) for data in all_data]
b, r, c, e, f = vehicles
assert all(data['schema'] == 5 and data['rails'] == before['rails'] for data in all_data)
assert len(before['rails']) == 46
assert all(len(v) == 13 and v.keys() == b.keys() for v in vehicles)
assert all(sum(len(v['carriers']) for v in data['vehicles']) == 30 for data in all_data)
targets = {id for id, v in b.items() if v['fault']}
assert len(targets) == 4 and all(b[id]['family'] == 'pax_shuttle' for id in targets)
for current in vehicles:
    for id, v in current.items():
        for key in ['chassis', 'family', 'variant', 'boundFrom', 'boundTo']:
            assert v[key] == b[id][key]
        assert [carrier['body'] for carrier in v['carriers']] == [carrier['body'] for carrier in b[id]['carriers']]
        for carrier, initial in zip(v['carriers'], b[id]['carriers']):
            assert {k: x for k, x in carrier.items() if k not in ['angle', 'omega']} == {
                k: x for k, x in initial.items() if k not in ['angle', 'omega']}
for id in targets:
    assert b[id]['restraints'] and b[id]['fault'] == 'Shuttle winch chunk unavailable or winch removed'
    assert not r[id]['restraints'] and not r[id]['fault'] and r[id]['revision'] == b[id]['revision'] + 2
    assert c[id]['restraints'] and c[id]['fault'] == 'Shuttle lead car unavailable'
for current in [r, e, f]:
    assert all(not v['fault'] and not v['restraints'] and v['boarding_ready'] for v in current.values())
    for id, v in current.items():
        assert {k: x for k, x in v.items() if k != 'carriers'} == {
            k: x for k, x in r[id].items() if k != 'carriers'}

winch_names = ['caught-recovered-winch.json', 'caught-control-cold-winch.json',
               'caught-fixed-edge-winch.json', 'caught-fixed-final-winch.json']
rw, cw, ew, fw = [read(name) for name in winch_names]
assert {tuple(id) for id in rw['shuttle_cars']} == targets
assert rw['shuttle_carriage'] == [-133561419, -927185268, -2060102316, -96450345]
assert rw['shuttle_cycles'] == 1 and rw['shuttle_maximum'] == 0
assert cw['shuttle_phase'] == 'FAULT' and cw['shuttle_fault'] == 'Shuttle lead car unavailable'
for w in [rw, ew, fw]:
    assert w['Speed'] == 128 and w['shuttle_phase'] == 'BOARDING' and not w['shuttle_fault']
    assert {k: x for k, x in w.items() if k.startswith('shuttle_')} == {
        k: x for k, x in rw.items() if k.startswith('shuttle_')}

recovery_log = (root / 'caught-recovery-client-events.txt').read_text()
assert 'phase=CAPTURING' in recovery_log and 'phase=BOARDING' in recovery_log
assert 'carriage=f80a03b5-c8bc-468c-8535-5554fa4048d7 carriageLoaded=true' in recovery_log
assert 'BUILD SUCCESSFUL' in recovery_log
control_log = (root / 'caught-control-cold-events.txt').read_text()
assert 'tick=59850' in control_log and 'phase=BOARDING interrupted=BOARDING leadLoaded=false carsLoaded=0' in control_log
assert 'phase=FAULT interrupted=BOARDING leadLoaded=false carsLoaded=0' in control_log
assert 'tick=59851' in control_log and 'leadLoaded=true carsLoaded=4' in control_log
assert 'BUILD SUCCESSFUL' in control_log
negative = (root / 'caught-control-native-events.txt').read_text()
assert 'observedPhase=FAULT fault=Shuttle lead car unavailable leadLoaded=false carriageLoaded=false leadHeld=true carriageHeld=true restraints=[true, true, true, true]' in negative
assert '1 required tests failed' in negative and 'BUILD FAILED' in negative
for name in ['caught-native-standalone-events.txt', 'caught-native-combined-events.txt']:
    log = (root / name).read_text()
    assert 'observedPhase=BOARDING fault= leadLoaded=false carriageLoaded=false leadHeld=true carriageHeld=true restraints=[false, false, false, false]' in log
    assert 'restoredCars=4 restoredCarriers=8 links=4 sameCarriage=true missingLeadDispatchRejected=true missingSupportDispatchRejected=true' in log
    assert 'permanentCarriageRemovalFaulted=true destroyedCarriageRecoveryRejected=true replacementAllocated=false' in log
    assert 'All 30 required tests passed' in log and 'BUILD SUCCESSFUL' in log
fixed = (root / 'caught-fixed-first-events.txt').read_text()
assert 'tick=60719' in fixed and 'phase=BOARDING interrupted=BOARDING leadLoaded=false carsLoaded=0' in fixed
assert 'carriageLoaded=false winchAvailable=true fault=' in fixed and 'BUILD SUCCESSFUL' in fixed
cold = (root / 'caught-fixed-cold-events.txt').read_text()
assert 'at (-121.5, 90.0, -26.5)' in cold
assert cold.count('shuttle_fault: ""') >= 2 and cold.count('shuttle_phase: "BOARDING"') >= 2
assert 'FD_SHUTTLE_LIFECYCLE' not in cold and 'BUILD SUCCESSFUL' in cold
assert 'Found no elements matching RootVehicle' in cold and 'Found no elements matching NoGravity' in cold
flags = (root / 'caught-cold-validation-flags.txt').read_text()
for flag in ['validation', 'ponderValidation', 'schedulerValidation', 'shuttleValidation']:
    assert f'-Dfourthdimension.{flag}=false' in flags
assert read('caught-unit.json') == {'tests': 25, 'failures': 0, 'errors': 0, 'skipped': 0}
player = read('caught-final-player.json')
assert player['Pos'] == [93.5, 85.0, -26.5]
assert 'RootVehicle' not in player and not player.get('NoGravity', 0)
print('Verified recorded PAX operator recovery, caught-train control/fix, native holding and default cold reopen; broader FD902 remains open.')
