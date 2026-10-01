from pathlib import Path
import gzip,hashlib,json,re,shutil,subprocess,zipfile
b=Path('build/projection-reuse');out=Path('docs/evidence/integrations/2026-10-01/projection-reuse');out.mkdir(parents=True,exist_ok=True)
assert (b/'regressions.exit').read_text().strip()=='0' and (b/'benchmark.exit').read_text().strip()=='0'
def gz(p,dest):dest.write_bytes(gzip.compress(p.read_bytes(),mtime=0))
for p in b.glob('*.log'):gz(p,out/(p.name+'.gz'))
for p in b.glob('*-commands.txt'):gz(p,out/(p.name+'.gz'))
for p in b.glob('*.json'):
 if p.name in ['status.json','regressions-status.json','artifact-diff.json','settings-restored.json','cleanup.json','source-seed.json']:shutil.copyfile(p,out/p.name)
 else:gz(p,out/(p.name+'.gz'))
for p in b.glob('*.jsonl'):gz(p,out/(p.name+'.gz'))
for pattern in ['*.exit','*-foundation.dat','*.py','comparison.png']:
 for p in b.glob(pattern):
  shutil.copyfile(p,out/p.name)
  if p.name=='benchmark.py':
   gz(p,out/'benchmark-executed.py.gz')
   (out/p.name).write_text('\n'.join(line.rstrip() for line in p.read_text().splitlines())+'\n')
gz(b/'comparison.svg',out/'comparison.svg.gz')
failed=out/'failed-attempt';failed.mkdir(exist_ok=True)
for p in (b/'failed-attempt').iterdir():
 if p.suffix in ['.log','.txt'] or p.name.endswith(('-parked.json','-operating.json')):gz(p,failed/(p.name+'.gz'))
 else:shutil.copyfile(p,failed/p.name)
unit=out/'unit-tests';unit.mkdir(exist_ok=True)
for p in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):shutil.copyfile(p,unit/p.name)
sources=['src/main/java/dev/fourthdimension/runtime/CarrierRuntime.java','src/main/java/dev/fourthdimension/runtime/VehicleTrackSample.java','src/main/java/dev/fourthdimension/vehicle/BoundaryGuard.java','scripts/verify-projection-reuse.py','build.gradle','gradle.properties']
m={'parent_commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'artifacts':json.loads((b/'status.json').read_text())['artifacts'],'sources':{p:hashlib.sha256(Path(p).read_bytes()).hexdigest() for p in sources},'files':{}}
for version,artifact in m['artifacts'].items():
 p=Path(artifact['path']);assert hashlib.sha256(p.read_bytes()).hexdigest()==artifact['sha256']
 with zipfile.ZipFile(p) as z:
  artifact['classes']={n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if n.endswith(('/CarrierRuntime.class','/VehicleTrackSample.class','/BoundaryGuard.class'))}
  if version=='new':
   for n,h in artifact['classes'].items():assert hashlib.sha256((Path('/tmp/fourthdimension-48535694/build/classes/java/main')/n).read_bytes()).hexdigest()==h
m['loaded_mod_versions']={profile:{mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',(b/('native-'+profile+'.log')).read_text(),re.M)} for profile in ['base','engineered','styles','additions','magnetized','immersion','combined']}
m['files']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name not in ['manifest.json','README.md','verification.json']}
(out/'manifest.json').write_text(json.dumps(m,indent=2)+'\n')
