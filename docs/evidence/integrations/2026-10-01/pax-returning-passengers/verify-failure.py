#!/usr/bin/env python3
"""Validate the retained failure; success means the failure is documented, not fixed."""
from pathlib import Path
import json

root = Path(__file__).parent
runs = json.loads((root / 'runs.json').read_text())

def report(name, label):
    rows = [json.loads(l.split('FD_PAX_ENERGY_RESTART ', 1)[1])
            for l in (root / (name + '-checks.txt')).read_text().splitlines()
            if 'FD_PAX_ENERGY_RESTART {' in l]
    return next(r for r in rows if r['label'] == label)

before, after = report('before', 'before'), report('after', 'restored')
assert before['phase'] == 'RETURNING' and before['telemetry']['occupied'] == 16
assert after['phase'] == 'FAULT' and after['interrupted'] == 'RETURNING'
assert after['telemetry']['occupied'] == 2 and after['telemetry']['captured']
assert before['energy'] == after['energy'] == 12345
assert before['carriage'] == after['carriage'] and after['carriageLoaded']
assert all(c['stopping'] and c['restraints'] and c['consist'] == 4 for c in after['cars'])
assert all(b['capture'] and b['loaded'] for c in after['cars'] for b in c['carriers'])
a = json.loads((root / 'before-entities.json').read_text())
b = json.loads((root / 'after-entities.json').read_text())
assert len(a) == len(b) == len({r['id'] for r in a}) == len({r['id'] for r in b}) == 16
assert {r['id'] for r in a} == {r['id'] for r in b}
assert all(r['health'] == 10 and r['parent_type'] == 'create:seat' for r in a)
assert all(r['health'] == 10 for r in b)
assert sum(r['parent_type'] == 'create:seat' for r in b) == 2
assert sum(r['parent_type'] is None for r in b) == 14
for name, count in [('before', 16), ('after', 2)]:
    data = json.loads((root / (name + '-save.json')).read_text())['data']
    assert sum(len(c['occupants']) for v in data['vehicles'] for c in v['carriers']) == count
    assert runs[name]['exit_status'] == 0
assert runs['before']['operation_exit_status'] == 0 and runs['after']['operation_exit_status'] == 1
assert runs['before']['server_pid'] != runs['after']['server_pid']
commands = (root / 'after-commands.txt').read_text()
assert 'pax_energy_restart resume' not in commands and 'pax_energy_restart unload' not in commands
settings = json.loads((root / 'settings-restored.json').read_text())
assert all(r['original_sha256'] == r['restored_sha256'] for r in settings.values())
print('Confirmed original failed run: 16 original pigs alive, 14 detached after returning cold restart; settings restored.')
