from pathlib import Path
import gzip,hashlib,json,shutil,zipfile
root=Path(__file__).resolve().parents[2];out=Path(__file__).resolve().parent
dest=root/'docs/evidence/integrations/2026-10-02/magnetized-slope'
assert (out/'matrix.exit').read_text().strip()=='0'
dest.mkdir(parents=True,exist_ok=True)
def sha(data):return hashlib.sha256(data).hexdigest()
def write(name,value):(dest/name).write_text(json.dumps(value,indent=2)+'\n')
profiles=['magnetized','combined','base'];runs={}
for name in ['first','observation-magnetized','prebatch-magnetized','capacity-combined',*profiles]:
 raw=(out/(name+'.log')).read_bytes();text=raw.decode()
 (dest/(name+'.log.gz')).write_bytes(gzip.compress(raw,mtime=0))
 (dest/(name+'-checks.txt')).write_text('\n'.join(line.rstrip() for line in text.splitlines() if 'FD_' in line or 'required tests' in line or 'failed at' in line or line.startswith('BUILD '))+'\n')
 (dest/(name+'-mods.txt')).write_text('\n'.join(line.strip() for line in text.split('Mod List:',1)[1].split('[',1)[0].strip().splitlines())+'\n')
 r=out/(name+'-run.json');runs[name]=json.loads(r.read_text()) if r.exists() else {'exit_status':int((out/(name+'.exit')).read_text())}
 runs[name]['raw_log_sha256']=sha(raw)
write('runs.json',runs)
write('observation-audit.json',{'exit_status':int((out/'observation-matrix.exit').read_text()),'reason':'Native suite passed, but completion marker still counted six main phases and omitted four added drift observation phases; final marker corrected to ten.'})
cleanup={name:not (Path('/proc')/str(runs[name]['pid'])).exists() for name in profiles};assert all(cleanup.values())
write('cleanup.json',{'profile_launchers_stopped':cleanup,'matrix_exit':0})
for name in ('first-fixture.java.txt','observation-fixture.java.txt','capacity-registration.java.txt','matrix.py','package.py'):shutil.copyfile(out/name,dest/name)
shutil.copyfile(root/'src/main/java/dev/fourthdimension/integration/magnetized/MagnetizedSlopeCheck.java',dest/'fixture.java.txt')
shutil.copyfile(root/'src/main/java/dev/fourthdimension/gametest/MagnetizedIntegrationGameTests.java',dest/'registration.java.txt')
shutil.copytree(out/'unit',dest/'unit',dirs_exist_ok=True)
if (dest/'unit/binary').exists():shutil.rmtree(dest/'unit/binary')
old=Path('/home/braydon/.cache/fourthdimension-48535694/ordinary-access.jar');new=out/'verified.jar'
with zipfile.ZipFile(old) as a,zipfile.ZipFile(new) as b:
 aa=set(a.namelist());bb=set(b.namelist())
 diff={'added':sorted(bb-aa),'removed':sorted(aa-bb),'changed':sorted(n for n in aa&bb if a.read(n)!=b.read(n))}
 classes={n:{'compiled_sha256':sha((Path('/tmp/fourthdimension-48535694/build/classes/java/main')/n).read_bytes()),'jar_sha256':sha(b.read(n))} for n in diff['added']+diff['changed'] if n.endswith('.class')}
 artifact={'baseline_jar_sha256':sha(old.read_bytes()),'jar_sha256':sha(new.read_bytes()),'diff':diff,'classes':classes,
           'sources':{name:sha((dest/name).read_bytes()) for name in ('fixture.java.txt','registration.java.txt')}}
write('artifact.json',artifact)
cache=old.parent/'magnetized-slope.jar'
if cache.exists():assert sha(cache.read_bytes())==artifact['jar_sha256']
else:shutil.copyfile(new,cache)
write('manifest.json',{str(p.relative_to(dest)):sha(p.read_bytes()) for p in sorted(dest.rglob('*')) if p.is_file() and p.name not in ('README.md','verification.json','manifest.json')})
print(json.dumps({'destination':str(dest),'artifact':artifact},indent=2))
