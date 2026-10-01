from pathlib import Path
import shutil,json,gzip,zipfile,hashlib,subprocess
b=Path(__file__).resolve().parent;root=b.parents[1];e=root/'docs/evidence/integrations/2026-10-01/styles-lifecycle';e.mkdir(parents=True,exist_ok=True)
def cp(src,dst):
 dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,dst)
def gz(src,dst):
 dst.parent.mkdir(parents=True,exist_ok=True);dst.write_bytes(gzip.compress(src.read_bytes(),mtime=0))
for name in ['plan.json','cleanup.json','build.exit']:cp(b/name,e/name)
gz(b/'build.log',e/'build.log.gz')
for p in b.glob('*.py'):cp(p,e/'drivers'/p.name)
for p in Path('/tmp/fourthdimension-48535694/build/test-results/test').glob('TEST-*.xml'):cp(p,e/'unit-tests'/p.name)
for case in ['seed','removed','reinstalled']:
 d=b/case;o=e/case
 for pattern in ['*.json','*.exit','*.sh','*.png']:
  for p in d.glob(pattern):cp(p,o/p.name)
 for p in d.glob('*.log'):gz(p,o/(p.name+'.gz'))
 gz(d/'commands.txt',o/'commands.txt.gz')
 (o/'commands.txt').write_text('\n'.join(l.rstrip() for l in (d/'commands.txt').read_text().splitlines()).rstrip()+'\n')
 for folder,prefix in [('world','')]+([('occupied-world','occupied-')] if case=='reinstalled' else []):
  cp(d/folder/'data/fourthdimension_foundation.dat',o/folder/'data/fourthdimension_foundation.dat')
  for p in (d/folder/'playerdata').glob('*.dat'):cp(p,o/folder/'playerdata'/p.name)
  with zipfile.ZipFile(o/(prefix+'entity-regions.zip'),'w',zipfile.ZIP_DEFLATED) as z:
   for p in sorted((d/folder/'entities').glob('*')):
    if p.suffix in ['.mca','.mcc']:z.write(p,p.name)
# Hash the precise source and immutable artifact used in all nine processes.
sources={p:hashlib.sha256((root/p).read_bytes()).hexdigest() for p in ['src/main/java/dev/fourthdimension/integration/StylesLifecycleFixture.java','src/main/java/dev/fourthdimension/integration/IntegrationCommands.java','src/main/java/dev/fourthdimension/client/ClientDiagnostics.java']}
jar=Path('/home/braydon/.cache/fourthdimension-48535694/styles-lifecycle.jar')
with zipfile.ZipFile(jar) as z:classes={n:hashlib.sha256(z.read(n)).hexdigest() for n in sorted(z.namelist()) if n.endswith('.class')}
(e/'artifact.json').write_text(json.dumps({'parent_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=root,text=True).strip(),'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sources':sources,'classes':classes},indent=2)+'\n')
files={str(p.relative_to(e)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(e.rglob('*')) if p.is_file() and p.name not in ['manifest.json','README.md','verification.json']}
(e/'manifest.json').write_text(json.dumps({'files':files},indent=2)+'\n')
print('Packaged',len(files),'files',sum(p.stat().st_size for p in e.rglob('*') if p.is_file()))
