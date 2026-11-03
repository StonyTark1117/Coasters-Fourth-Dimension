#!/usr/bin/env python3
"""Verify this bounded native profile matrix, not the complete release gate."""
from pathlib import Path
import argparse,gzip,hashlib,json,subprocess,sys,zipfile
parser=argparse.ArgumentParser(); parser.add_argument('--world-root',type=Path); parser.add_argument('--jar',type=Path); args=parser.parse_args()
root=Path(__file__).resolve().parent
manifest=json.loads((root/'manifest.json').read_text())
for name,expected in manifest.items():
 p=root/name
 assert p.is_file() and hashlib.sha256(p.read_bytes()).hexdigest()==expected,f'Invalid payload: {name}'
records=[]
for name in ['base','styles','engineered','combined']:
 subprocess.run([sys.executable,str(root/'check-profile.py'),str(root/name)],check=True)
 records.append(json.loads((root/name/'metadata.json').read_text()))
first=records[0]
for m in records:
 assert m['source']=='abc4a9a9dd2a1df4957ef5714a58eae6e150895b'
 for key in ['source_files','compiled_files','built_jars']: assert m[key]==first[key],f'{key}: {m["profile"]}'
 assert list(m['built_jars'].values())==['74ae47b70f2e95bcd8152552f255cabba6b55ed942a1f9856fa41fef39ca2467']
 resolved=json.loads((root/m['profile']/'resolved-runtime-classpath.json').read_text())
 assert 'before GameTest startup' in resolved['capture']
 deps={d['sha256'] for d in resolved['files']}
 for profiles_with,sha in [(('styles','combined'),'42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38'),(('engineered','combined'),'eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d')]:
  assert (sha in deps)==(m['profile'] in profiles_with),f'Addon artifact: {m["profile"]}'
 world_manifest=json.loads((root/m['profile']/'stopped-world-manifest.json').read_text())
 assert 'data/fourthdimension_foundation.dat' in world_manifest and 'level.dat' in world_manifest
 if args.world_root:
  for rel,expected in world_manifest.items():
   path=args.world_root/m['profile']/'stopped-world'/rel
   assert path.is_file() and hashlib.sha256(path.read_bytes()).hexdigest()==expected,f'World mismatch: {m["profile"]}/{rel}'
 log=gzip.decompress((root/m['profile']/'console.log.gz').read_bytes()).decode()
 assert "Running test batch 'fourthdimension_family_eligibility:0' (1 tests)" in log
 assert 'Vehicle capacity reached' not in log
 summary=json.loads((root/'summary.json').read_text())[m['profile']]
 assert summary['required_tests']==49 and summary['minutes']==float(m['complete_summaries'][0][1].split()[0])
jar=json.loads((root/'jar-class-equivalence.json').read_text())
assert jar['compiled_classes_compared']==216 and not jar['mismatches'] and jar['sha256']=='74ae47b70f2e95bcd8152552f255cabba6b55ed942a1f9856fa41fef39ca2467'
if args.jar:
 assert hashlib.sha256(args.jar.read_bytes()).hexdigest()==jar['sha256']
 with zipfile.ZipFile(args.jar) as archive:
  for name,expected in first['compiled_files'].items():
   if name.startswith('classes/java/main/') and name.endswith('.class'):
    assert hashlib.sha256(archive.read(name.removeprefix('classes/java/main/'))).hexdigest()==expected,name
inventory=json.loads((root/'test-inventory.json').read_text())
assert inventory['source']=='abc4a9a9dd2a1df4957ef5714a58eae6e150895b' and inventory['count']==49
assert any(t['test']=='fiveFamilyFactoryLockedSnapAndBindingMatrix' and 'fourthdimension_family_eligibility' in t['annotation'] for t in inventory['tests'])
print(f'PASS bounded native matrix: four profiles,49 required tests each,{len(manifest)} payload hashes,one exact source/JAR. Full FD901/0.9 gates remain separate.')
