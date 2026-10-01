from pathlib import Path
from zipfile import ZipFile
import gzip,hashlib,json,re,shutil,subprocess
b=Path('build/acquisition');out=Path('docs/evidence/integrations/2026-10-01/acquisition');out.mkdir(parents=True,exist_ok=True)
assert (b/'native.exit').read_text().strip()=='0'
def gz(p,dest):dest.write_bytes(gzip.compress(p.read_bytes(),mtime=0))
for p in b.iterdir():
 if not p.is_file() or p.name=='server.properties.original' or p.name=='rcon.lock' or p.name.endswith('.pid'):continue
 if p.suffix in ['.log','.txt']:gz(p,out/(p.name+'.gz'))
 elif p.suffix in ['.json','.png','.dat','.exit','.py','.sh']:shutil.copyfile(p,out/p.name)
for folder in ['fake-player-attempt','private-handler-attempt']:
 dest=out/folder;dest.mkdir(exist_ok=True)
 for p in (b/folder).iterdir():
  if p.suffix=='.log':gz(p,dest/(p.name+'.gz'))
  elif p.suffix in ['.json','.exit','.py']:shutil.copyfile(p,dest/p.name)
# Keep the rejected visual attempt with its actual death screen, not a passing review label.
dest=out/'slime-interruption';dest.mkdir(exist_ok=True)
for name in ['tooltip-review-0.png','tooltip-review-1.png']:shutil.copyfile(b/'slime-interruption'/name,dest/name)
unit=out/'unit-tests';unit.mkdir(exist_ok=True)
for p in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):shutil.copyfile(p,unit/p.name)
sources=['README.md','ROADMAP.md','docs/INTEGRATIONS.md','scripts/generate-recipe-unlocks.py','scripts/verify-acquisition.py','src/main/java/dev/fourthdimension/client/ItemDescriptions.java','src/main/java/dev/fourthdimension/gametest/AcquisitionGameTests.java','src/main/resources/assets/fourthdimension/lang/en_us.json']
sources += [str(p) for p in sorted(Path('src/main/resources/data/fourthdimension/advancement/recipes').glob('*.json'))]
m={'parent_commit':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),'artifacts':{k:v for k,v in json.loads((b/'artifact-diff.json').read_text()).items() if k in ['visual','native']},'sources':{p:hashlib.sha256(Path(p).read_bytes()).hexdigest() for p in sources}}
for a in m['artifacts'].values():assert hashlib.sha256(Path(a['path']).read_bytes()).hexdigest()==a['sha256']
m['loaded_mod_versions']={profile:{mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',(b/('native-'+profile+'.log')).read_text(),re.M)} for profile in ['base','combined']}
m['files']={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name not in ['README.md','manifest.json','verification.json']}
(out/'manifest.json').write_text(json.dumps(m,indent=2)+'\n')
