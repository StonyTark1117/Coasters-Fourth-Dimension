from pathlib import Path
import gzip,json,hashlib,shutil,zipfile
b=Path(__file__).resolve().parent;root=b.parents[1];out=root/'docs/evidence/integrations/2026-10-01/camera-connections';out.mkdir(exist_ok=True)
def copy(src,target):
 target.parent.mkdir(parents=True,exist_ok=True)
 if src.suffix=='.log':target=target.with_suffix('.log.gz');target.write_bytes(gzip.compress(src.read_bytes(),mtime=0))
 elif src.suffix in ['.txt','.py','.sh','.json','.toml','.exit','.pid','.xml']:
  target.write_text('\n'.join(x.rstrip() for x in src.read_text().splitlines()).rstrip()+'\n')
 else:shutil.copyfile(src,target)
for case in ['before','after','base','combined']:
 d=b/case
 assert (d/'server.exit').read_text().strip()=='0'
 for src in d.iterdir():
  if src.is_file():copy(src,out/case/src.name)
 (out/case/'commands.raw.txt.gz').write_bytes(gzip.compress((d/'commands.txt').read_bytes(),mtime=0))
 for src in (d/'spawn-slime-attempt').glob('*'):
  if src.is_file():copy(src,out/case/'spawn-slime-attempt'/src.name)
 for name in ['data/fourthdimension_foundation.dat','level.dat']:
  copy(d/'world'/name,out/case/'world'/name)
 for src in (d/'world/playerdata').glob('*.dat'):copy(src,out/case/'world/playerdata'/src.name)
for src in b.glob('*.py'):copy(src,out/'drivers'/src.name)
for name in ['before-build.log','fixed-build.log','cleanup.json','visual-review.json']:
 copy(b/name,out/name)
for src in b.glob('original*'):copy(src,out/'settings-before'/src.name)
for src in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):copy(src,out/'unit-tests'/src.name)
metadata={}
for candidate in ['before','fixed']:
 jar=Path('/home/braydon/.cache/fourthdimension-48535694/camera-connections-'+candidate+'.jar')
 with zipfile.ZipFile(jar) as archive:
  classes={name:hashlib.sha256(archive.read(name)).hexdigest() for name in archive.namelist() if name.startswith('dev/fourthdimension/') and name.endswith('.class')}
  metadata[candidate]={'artifact':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'classes':classes,'mixins':json.loads(archive.read('fourthdimension.mixins.json'))}
  if candidate=='fixed':
   assert archive.read('fourthdimension.mixins.json')==(root/'src/main/resources/fourthdimension.mixins.json').read_bytes()
   for name in classes:assert hashlib.sha256((Path('/tmp/fourthdimension-48535694/build/classes/java/main')/name).read_bytes()).hexdigest()==classes[name],name
for name in ['client/CarrierCameraDiagnostics.java','client/ClientDiagnostics.java','mixin/CarrierConnectionMixin.java']:
 source=Path('src/main/java/dev/fourthdimension')/name;copy(root/source,out/'sources'/source.name)
metadata['source_hashes']={str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in [root/'src/main/java/dev/fourthdimension'/n for n in ['client/CarrierCameraDiagnostics.java','client/ClientDiagnostics.java','mixin/CarrierConnectionMixin.java']]}
copy(root/'src/main/resources/fourthdimension.mixins.json',out/'sources/fourthdimension.mixins.json')
metadata['source_hashes']['src/main/resources/fourthdimension.mixins.json']=hashlib.sha256((root/'src/main/resources/fourthdimension.mixins.json').read_bytes()).hexdigest()
(out/'artifacts.json').write_text(json.dumps(metadata,indent=2)+'\n')
files={str(p.relative_to(out)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(out.rglob('*')) if p.is_file() and p.name not in ['README.md','verification.json','manifest.json']}
(out/'manifest.json').write_text(json.dumps({'files':files},indent=2)+'\n');print(len(files),'artifacts')
