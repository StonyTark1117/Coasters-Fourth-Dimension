#!/usr/bin/env python3
"""Verify the archived four-profile checkpoint, not the full release gate."""
from pathlib import Path
import hashlib,json,subprocess,sys
root=Path(__file__).resolve().parent
manifest=json.loads((root/'manifest.json').read_text())
for name,expected in manifest.items():
 p=root/name
 assert p.is_file(),f'Missing payload: {name}'
 assert hashlib.sha256(p.read_bytes()).hexdigest()==expected,f'Hash mismatch: {name}'
profiles=['base','styles','engineered']
records=[]
for name in profiles:
 if name!='engineered': subprocess.run([sys.executable,str(root/'check-profile.py'),str(root/name)],check=True)
 records.append(json.loads((root/name/'metadata.json').read_text()))
first=records[0]
for m in records:
 assert m['source']=='11e1a934da0f8c6cdafde818d290eb0fd17a1ffb'
 for key in ['source_files','compiled_files','built_jars']:
  assert m[key]==first[key],f'{key} changed in {m["profile"]}'
 assert list(m['built_jars'].values())==['b95327c8fbfdba02214669c4947ce578631e07088aeb6101a0d2d1649a3b88c9']
 resolved=json.loads((root/m['profile']/'resolved-launch-classpath.json').read_text())
 deps={d['sha256'] for d in resolved['files']}
 for profiles_with,sha in [(('styles','combined'),'42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38'),(('engineered','combined'),'eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d')]:
  assert (sha in deps)==(m['profile'] in profiles_with),f'Optional artifact hash mismatch: {m["profile"]}'
relation=json.loads((root/'release-equivalence.json').read_text())
assert relation['tested_source']==first['source'] and relation['executable_diff']==''
assert set(relation['changed_paths'])=={'ROADMAP.md','docs/evidence/0.9/README.md'}
jar=json.loads((root/'jar-class-equivalence.json').read_text())
assert jar['compiled_classes_compared']==216 and not jar['mismatches']
assert jar['sha256']==next(iter(first['built_jars'].values()))
import gzip
failed=records[-1]
log=gzip.decompress((root/'engineered/console.log.gz').read_bytes()).decode()
assert failed['exit_code']==1 and failed['required_test_summaries']==[]
assert 'fivefamilyfactorylockedsnapandbindingmatrix failed at' in log and 'Vehicle capacity reached' in log
assert 'FD_ENGINEERED_FE installed=1.1.1 nativeCapability=true unloaded=true' in log
assert 'non-zero exit value 143' in log and 'BUILD FAILED' in log
assert 'All dimensions are saved' not in log
termination=json.loads((root/'engineered/termination.json').read_text())
assert termination['jvm_exit']==143 and termination['normal_shutdown'] is False
assert json.loads((root/'summary.json').read_text())['combined']['status'].startswith('not run')
assert not (root/'combined').exists()
print(f'PASS mixed-result record: {len(manifest)} hashes; base/Styles pass49, Engineered capacity failure preserved, combined not run. NOT a green matrix or release gate.')
