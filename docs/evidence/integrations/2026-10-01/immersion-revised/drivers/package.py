from pathlib import Path
import gzip,json,hashlib,shutil,zipfile
b=Path(__file__).resolve().parent;root=b.parents[1];out=root/'docs/evidence/integrations/2026-10-01/immersion-revised';out.mkdir(exist_ok=True)
def copy(src,target):
 target.parent.mkdir(parents=True,exist_ok=True)
 if src.suffix=='.log':target=target.with_suffix('.log.gz');target.write_bytes(gzip.compress(src.read_bytes(),mtime=0))
 elif src.suffix in ['.txt','.py','.sh','.json','.toml','.exit','.pid','.xml']:
  target.write_text('\n'.join(x.rstrip() for x in src.read_text().splitlines()).rstrip()+'\n')
 else:shutil.copyfile(src,target)
for case in ['immersion','combined']:
 d=b/case
 assert (d/'server.exit').read_text().strip()=='0'
 for src in d.iterdir():
  if src.is_file():copy(src,out/case/src.name)
 (out/case/'commands.raw.txt.gz').write_bytes(gzip.compress((d/'commands.txt').read_bytes(),mtime=0))
 for name in ['data/fourthdimension_foundation.dat','level.dat']:
  copy(d/'world'/name,out/case/'world'/name)
 for src in (d/'world/playerdata').glob('*.dat'):copy(src,out/case/'world/playerdata'/src.name)
for src in b.glob('*.py'):copy(src,out/'drivers'/src.name)
for name in ['build.log','native-base.log','native-base.exit','cleanup.json','sable-zoom.txt','visual-review.json']:
 copy(b/name,out/name)
for src in b.glob('original*'):copy(src,out/'settings-before'/src.name)
for src in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):copy(src,out/'unit-tests'/src.name)
jar=Path('/home/braydon/.cache/fourthdimension-48535694/immersion-revised.jar')
classes={};sources={}
with zipfile.ZipFile(jar) as archive:
 for name in ['IntegrationCommands','immersion/ImmersionRideFixture','immersion/ImmersionCameraDiagnostics']:
  source=Path('src/main/java/dev/fourthdimension/integration')/(name+'.java')
  sources[str(source)]=hashlib.sha256((root/source).read_bytes()).hexdigest()
  copy(root/source,out/'sources'/source.name)
  path='dev/fourthdimension/integration/'+name+'.class';raw=archive.read(path)
  assert raw==(Path('/tmp/fourthdimension-48535694/build/classes/java/main')/path).read_bytes(),path
  classes[path]=hashlib.sha256(raw).hexdigest()
metadata={'artifact':str(jar),'artifact_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'source_hashes':sources,'packaged_and_current_class_hashes':classes}
(out/'artifact.json').write_text(json.dumps(metadata,indent=2)+'\n')
files={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name not in ['README.md','verification.json','manifest.json']}
(out/'manifest.json').write_text(json.dumps({'files':files},indent=2)+'\n');print(len(files),'artifacts')
