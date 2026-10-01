from pathlib import Path
import gzip,hashlib,json,re,shutil,subprocess,zipfile
b=Path('build/server-benchmark');out=Path('docs/evidence/integrations/2026-10-01/server-benchmark');out.mkdir(parents=True,exist_ok=True)
assert (b/'run.exit').read_text().strip()=='0'
assert (b/'profile.exit').read_text().strip()=='0'
def gz(src,dest):dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(gzip.compress(src.read_bytes(),mtime=0))
for p in b.glob('*.log'):gz(p,out/(p.name+'.gz'))
for p in b.glob('*-commands.txt'):gz(p,out/(p.name+'.gz'))
for p in b.glob('*.json'):
 if p.name in ['hardware.json','status.json','profile-status.json','settings-restored.json']:shutil.copyfile(p,out/p.name)
 elif p.name not in ['manifest.json','jfr-preview.json','jvm-information.json']:gz(p,out/(p.name+'.gz'))
for p in b.glob('*.jsonl'):gz(p,out/(p.name+'.gz'))
for pattern in ['*.exit','*-foundation.dat','*.jfr']:
 for p in b.glob(pattern):shutil.copyfile(p,out/p.name)
for name in ['run.py','monitor.py','profile.py','collect.py','continue.py','inspect-power.py','monitor-profile.py']:
 shutil.copyfile(b/name,out/name)
for name in ['build-initial.log','build.log','seed.log','seed-commands.txt','run.log','settings-restored.json','run.exit','status.json']:
 p=b/'attempt-1'/name
 if p.exists():gz(p,out/'attempt-1'/(name+'.gz'))
for p in (b/'attempt-2').iterdir():
 if p.is_file():gz(p,out/'attempt-2'/(p.name+'.gz'))
unit=out/'unit-tests';unit.mkdir(exist_ok=True)
for p in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):shutil.copyfile(p,unit/p.name)
jar=Path('/home/braydon/.cache/fourthdimension-48535694/server-benchmark-v2.jar')
sources=['src/main/java/dev/fourthdimension/integration/PerformanceFixture.java','src/main/java/dev/fourthdimension/FourthDimension.java','src/main/java/dev/fourthdimension/runtime/CarrierRuntime.java','scripts/verify-server-benchmark.py','scripts/summarize-benchmark-jfr.py','scripts/BenchmarkJfrSummary.java','build.gradle','gradle.properties']
m={'parent_commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'artifact':{'path':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},'sources':{s:hashlib.sha256(Path(s).read_bytes()).hexdigest() for s in sources},'files':{}}
m['loaded_mod_versions']={profile:{mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',(b/(profile+'-parked.log')).read_text(),re.M)} for profile in ['base','combined']}
with zipfile.ZipFile(jar) as z:
 m['artifact']['classes']={n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if 'PerformanceFixture' in n or n.endswith('/FourthDimension.class') or n.endswith('/CarrierRuntime.class')}
 for n,h in m['artifact']['classes'].items():assert hashlib.sha256((Path('/tmp/fourthdimension-48535694/build/classes/java/main')/n).read_bytes()).hexdigest()==h
m['files']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name not in ['README.md','manifest.json','verification.json']}
(out/'manifest.json').write_text(json.dumps(m,indent=2)+'\n')
