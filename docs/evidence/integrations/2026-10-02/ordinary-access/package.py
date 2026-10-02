from pathlib import Path
import gzip,hashlib,json,shutil,zipfile
root=Path(__file__).resolve().parents[2]
out=Path(__file__).resolve().parent
dest=root/'docs/evidence/integrations/2026-10-02/ordinary-access'
assert (out/'matrix.exit').read_text().strip()=='0'
dest.mkdir(parents=True,exist_ok=True)
def sha(data): return hashlib.sha256(data).hexdigest()
def write(name,value): (dest/name).write_text(json.dumps(value,indent=2)+'\n')
profiles=['base','engineered','styles','additions','magnetized','immersion','combined']
attempts=['setup-base','engagement-base','precision-base']
runs={}
for name in profiles+attempts:
 raw=(out/(name+'.log')).read_bytes()
 (dest/(name+'.log.gz')).write_bytes(gzip.compress(raw,mtime=0))
 text=raw.decode()
 checks='\n'.join(line.rstrip() for line in text.splitlines() if 'FD_' in line or 'required tests' in line or 'failed at' in line or line.startswith('BUILD '))+'\n'
 (dest/(name+'-checks.txt')).write_text(checks)
 mods=text.split('Mod List:',1)[1].split('[',1)[0]
 (dest/(name+'-mods.txt')).write_text('\n'.join(line.strip() for line in mods.strip().splitlines())+'\n')
 runfile=out/(name+'-run.json')
 runs[name]=json.loads(runfile.read_text()) if runfile.exists() else {'exit_status':int((out/(name+'.exit')).read_text())}
 runs[name]['raw_log_sha256']=sha(raw)
write('runs.json',runs)
cleanup={name: not (Path('/proc')/str(runs[name]['pid'])).exists() for name in profiles}
assert all(cleanup.values())
write('cleanup.json',{'profile_launchers_stopped':cleanup,'matrix_exit':int((out/'matrix.exit').read_text())})
for name in ('initial-fixture.java.txt','engagement-fixture.java.txt','precision-fixture.java.txt','matrix.py','package.py'):
 shutil.copyfile(out/name,dest/name)
shutil.copyfile(root/'src/main/java/dev/fourthdimension/gametest/OrdinaryAccessGameTests.java',dest/'fixture.java.txt')
shutil.copytree(out/'unit',dest/'unit',dirs_exist_ok=True)
# Only XML reports, not Gradle's binary result database.
if (dest/'unit/binary').exists(): shutil.rmtree(dest/'unit/binary')
old=Path('/home/braydon/.cache/fourthdimension-48535694/camera-connections-fixed.jar')
new=out/'verified.jar'
with zipfile.ZipFile(old) as a,zipfile.ZipFile(new) as b:
 namesa=set(a.namelist());namesb=set(b.namelist())
 diff={'added':sorted(namesb-namesa),'removed':sorted(namesa-namesb),
       'changed':sorted(n for n in namesa&namesb if a.read(n)!=b.read(n))}
 entry='dev/fourthdimension/gametest/OrdinaryAccessGameTests.class'
 cls=Path('/tmp/fourthdimension-48535694/build/classes/java/main')/entry
 artifact={'baseline_jar_sha256':sha(old.read_bytes()),'jar_sha256':sha(new.read_bytes()),'diff':diff,
           'fixture':{'source_sha256':sha((dest/'fixture.java.txt').read_bytes()),'compiled_sha256':sha(cls.read_bytes()),'jar_sha256':sha(b.read(entry))}}
write('artifact.json',artifact)
cache=old.parent/'ordinary-access.jar'
if cache.exists(): assert sha(cache.read_bytes())==artifact['jar_sha256']
else: shutil.copyfile(new,cache)
write('manifest.json',{str(p.relative_to(dest)):sha(p.read_bytes()) for p in sorted(dest.rglob('*')) if p.is_file() and p.name not in ('manifest.json','README.md','verification.json')})
print(json.dumps({'destination':str(dest),'artifact':artifact},indent=2))
