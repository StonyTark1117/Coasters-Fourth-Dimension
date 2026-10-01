#!/usr/bin/env python3
"""Check recorded dev.13 idle availability evidence; never runs a client or certifies FD902."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1] / 'docs/evidence/0.9'


def read(name):
    return json.loads((root / name).read_text())


def keyed(data):
    return {tuple(v['chassis']): v for v in data['vehicles']}


before, control, edge, final = [read(name)['data'] for name in [
    'idle-pristine-save.json', 'idle-control-save.json',
    'idle-fixed-edge-save.json', 'idle-fixed-final-save.json',
]]
all_vehicles = [keyed(data) for data in [before, control, edge, final]]
assert all(data['schema'] == 5 and len(data['rails']) == 46 for data in [before, control, edge, final])
assert before['rails'] == control['rails'] == edge['rails'] == final['rails']
assert all(len(vehicles) == 13 and vehicles.keys() == all_vehicles[0].keys() for vehicles in all_vehicles)
targets = {id for id, v in all_vehicles[0].items() if v['family'] == 'pax_shuttle' and not v['fault']}
inherited = {id for id, v in all_vehicles[0].items() if v['family'] == 'pax_shuttle' and v['fault']}
assert len(targets) == len(inherited) == 4
for vehicles in all_vehicles:
    assert sum(len(v['carriers']) for v in vehicles.values()) == 30
    for id, vehicle in vehicles.items():
        original = all_vehicles[0][id]
        for field in ['chassis', 'family', 'variant', 'boundFrom', 'boundTo']:
            assert vehicle[field] == original[field]
        assert [c['body'] for c in vehicle['carriers']] == [c['body'] for c in original['carriers']]
        for current, initial in zip(vehicle['carriers'], original['carriers']):
            assert {k: v for k, v in current.items() if k not in ['angle', 'omega']} == {
                k: v for k, v in initial.items() if k not in ['angle', 'omega']}
        if id in inherited:
            assert vehicle['fault'] and vehicle['restraints']
for id in targets:
    failed = all_vehicles[1][id]
    assert failed['fault'] == 'Shuttle lead car unavailable' and failed['restraints']
    for vehicles in [all_vehicles[0], all_vehicles[2], all_vehicles[3]]:
        vehicle = vehicles[id]
        assert not vehicle['fault'] and not vehicle['restraints'] and vehicle['stopping'] and vehicle['boarding_ready']
        assert {k: v for k, v in vehicle.items() if k != 'carriers'} == {
            k: v for k, v in all_vehicles[0][id].items() if k != 'carriers'}

control_log = (root / 'idle-control-reopen-events.txt').read_text()
assert 'tick=41359' in control_log and 'phase=BOARDING interrupted=BOARDING leadLoaded=false carsLoaded=0' in control_log
assert 'phase=FAULT interrupted=BOARDING leadLoaded=false carsLoaded=0' in control_log
assert 'fault=Shuttle lead car unavailable' in control_log
fixed_log = (root / 'idle-fixed-first-events.txt').read_text()
assert 'tick=40531' in fixed_log and 'phase=BOARDING interrupted=BOARDING leadLoaded=false carsLoaded=0' in fixed_log
assert 'winchAvailable=true fault=' in fixed_log and 'BUILD SUCCESSFUL' in fixed_log
negative = (root / 'idle-control-native-events.txt').read_text()
assert 'observedPhase=FAULT fault=Shuttle lead car unavailable leadLoaded=false leadHeld=true restraints=[true, true, true, true]' in negative
assert '1 required tests failed' in negative and 'BUILD FAILED' in negative
for name in ['idle-native-standalone-events.txt', 'idle-native-combined-events.txt']:
    logs = (root / name).read_text()
    assert 'observedPhase=BOARDING fault= leadLoaded=false leadHeld=true restraints=[false, false, false, false]' in logs
    assert 'gapPreserved=true missingLeadDispatchRejected=true' in logs
    assert 'restoredCars=4 restoredCarriers=8 links=4 missingSupportDispatchRejected=true' in logs
    assert 'All 29 required tests passed' in logs and 'BUILD SUCCESSFUL' in logs
cold = (root / 'idle-fixed-cold-events.txt').read_text()
assert 'at (128.5, 90.0, -26.5)' in cold
assert cold.count('shuttle_fault: ""') >= 2 and cold.count('shuttle_phase: "BOARDING"') >= 2
assert 'Found no elements matching RootVehicle' in cold and 'Found no elements matching NoGravity' in cold
assert 'FD_SHUTTLE_LIFECYCLE' not in cold and 'BUILD SUCCESSFUL' in cold
flags = (root / 'idle-cold-validation-flags.txt').read_text()
for flag in ['validation', 'ponderValidation', 'schedulerValidation', 'shuttleValidation']:
    assert f'-Dfourthdimension.{flag}=false' in flags
assert read('idle-unit.json') == {'tests': 25, 'failures': 0, 'errors': 0, 'skipped': 0}
player = read('idle-final-player.json')
assert 'RootVehicle' not in player and not player.get('NoGravity', 0)
assert player['Pos'] == [343.5, 85.0, -26.5]
print('Verified historical dev.13 idle-winch control/fix and default cold reopen; see the caught-shuttle checkpoint for subsequent inherited-fault recovery.')
