from pathlib import Path
import json,gzip,zipfile,shutil,hashlib,subprocess
b=Path(__file__).resolve().parent;root=b.parents[1];e=root/'docs/evidence/integrations/2026-10-01/additions-lifecycle';e.mkdir(parents=True,exist_ok=True)
def cp(src,dst):dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
def gz(src,dst):dst.parent.mkdir(parents=True,exist_ok=True);dst.write_bytes(gzip.compress(src.read_bytes(),mtime=0))
for p in b.glob('*.py'):cp(p,e/'drivers'/p.name)
for p in b.glob('*.json'):cp(p,e/p.name)
for p in b.glob('*.exit'):cp(p,e/p.name)
for p in b.glob('*.log'):gz(p,e/(p.name+'.gz'))
for p in b.glob('*bytecode.txt'):cp(p,e/p.name)
for p in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):cp(p,e/'unit-tests'/p.name)
for case in ['invalid-frequency','uninitialized-link','boundary-observation','seed','restored','combined']:
 d=b/case;o=e/case
 if not d.exists():continue
 for pattern in ['*.json','*.exit','*.sh','*.png']:
  for p in d.glob(pattern):cp(p,o/p.name)
 for p in d.glob('*.log'):gz(p,o/(p.name+'.gz'))
 if (d/'commands.txt').exists():
  gz(d/'commands.txt',o/'commands.txt.gz');(o/'commands.txt').write_text('\n'.join(l.rstrip() for l in (d/'commands.txt').read_text().splitlines()).rstrip()+'\n')
 for nested in ['overlapping-view-attempt','feet-anchor-view-attempt']:
  if (d/nested).exists():
   for p in (d/nested).iterdir():
    if p.suffix=='.log':gz(p,o/nested/(p.name+'.gz'))
    elif p.suffix=='.png':cp(p,o/nested/p.name)
 if (d/'unloaded-playerdata').exists():
  for p in (d/'unloaded-playerdata').glob('*.dat'):cp(p,o/'unloaded-playerdata'/p.name)
 if (d/'world').exists():
  world=d/'world';cp(world/'data/fourthdimension_foundation.dat',o/'world/data/fourthdimension_foundation.dat')
  with (o/'foundation.json').open('w') as f:subprocess.run(['python3','scripts/inspect-foundation-save.py',str(world/'data/fourthdimension_foundation.dat')],cwd=root,stdout=f,check=True)
  for p in (world/'playerdata').glob('*.dat'):cp(p,o/'world/playerdata'/p.name)
  for kind,folder,script in [('pigs','entities','inspect-entities.py'),('blocks','region','inspect-blocks.py')]:
   with (o/('native-'+kind+'.json')).open('w') as f:subprocess.run(['python3',str(b/script),str(world)],cwd=root,stdout=f,check=True)
   with zipfile.ZipFile(o/('entity-regions.zip' if kind=='pigs' else 'block-regions.zip'),'w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted((world/folder).glob('*')):
     if p.suffix in ['.mca','.mcc']:z.write(p,p.name)
jar=Path('/home/braydon/.cache/fourthdimension-48535694/additions-lifecycle-v3.jar')
sourcefiles=['src/main/java/dev/fourthdimension/integration/'+p for p in ['IntegrationCommands.java','additions/AdditionsLifecycleFixture.java','additions/AdditionsWirelessFixture.java','additions/AdditionsStationCheck.java']]
with zipfile.ZipFile(jar) as z:classes={n:hashlib.sha256(z.read(n)).hexdigest() for n in sorted(z.namelist()) if n.endswith('.class')}
(e/'artifact.json').write_text(json.dumps({'parent_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=root,text=True).strip(),'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'source':{p:hashlib.sha256((root/p).read_bytes()).hexdigest() for p in sourcefiles},'classes':classes},indent=2)+'\n')
files={str(p.relative_to(e)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(e.rglob('*')) if p.is_file() and p.name not in ['manifest.json','README.md','verification.json']}
(e/'manifest.json').write_text(json.dumps({'files':files},indent=2)+'\n');print('files',len(files),'bytes',sum(p.stat().st_size for p in e.rglob('*') if p.is_file()))
