"""Observe actual OUTBOUND station contact before arming one original-curve removal."""
import importlib.util
import json
import math
import time
from pathlib import Path

ROOT = Path('/mnt/NVME4/fourthdimension-runs/fd10-mechanisms-20261115')
spec = importlib.util.spec_from_file_location('private', Path(__file__).resolve().parents[4] / 'scripts/fd-private-client.py')
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
r = m.PrivateRun(ROOT / 'run.json', '01a1dd1d-c6a6-7be0-9cb5-2b033ac550da')
r.state('server')
path = ROOT / 'evidence/station-loss-station-adjacent-approach.jsonl'
assert not path.exists(), 'Preserve the first approach; do not repeat blindly'
station = {'BlockPos{x=0, y=105, z=65}', 'BlockPos{x=0, y=105, z=97}'}
baseline = json.loads((Path(__file__).parent / 'native-baseline.json').read_text())
slots = {c['id']: c['position'] for c in baseline['cars']}
deadline = time.monotonic() + 90
with path.open('w') as log:
    while time.monotonic() < deadline:
        answer = m.rcon(r.c, 'fd902 snapshot')
        assert answer.startswith('FD902_STATE '), answer
        state = json.loads(answer.removeprefix('FD902_STATE '))
        log.write(json.dumps(state) + '\n')
        log.flush()
        assert state['phase'] != 'FAULT', state['fault']
        # Physical support realignment is explicitly station-adjacent (eight-block
        # guard). Keep a block of margin and require the whole native consist on
        # that exact station edge, not merely its first entering car.
        if state['phase'] == 'OUTBOUND' and all(
                {c['boundFrom'], c['boundTo']} == station
                and math.dist(c['position'], slots[c['id']]) < 7
                for c in state['cars']):
            answer = m.rcon(r.c, 'fd902 loss_at OUTBOUND')
            assert 'FD902_ARM' in answer, answer
            print(json.dumps({'armedTick': state['tick'], 'phase': state['phase'],
                              'riders': len(state['riders']), 'answer': answer}))
            break
        time.sleep(.25)
    else:
        raise RuntimeError('No actual occupied OUTBOUND station contact within90seconds')
