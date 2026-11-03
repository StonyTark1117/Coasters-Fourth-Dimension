#!/usr/bin/env python3
"""Run one explicitly selected native profile; preserve evidence before another run.
This uses Gradle's development GameTest runtime, not a packaged-client certification.
"""
from pathlib import Path
import argparse,datetime,gzip,hashlib,json,os,platform,re,shutil,subprocess,sys
p=argparse.ArgumentParser()
p.add_argument('profile',choices=['base','styles','engineered','combined'])
p.add_argument('--source',required=True)
a=p.parse_args()
root=Path.cwd()
def git(*args): return subprocess.check_output(['git',*args],text=True).strip()
source=git('rev-parse','HEAD')
assert source==git('rev-parse',a.source),'Source differs from allocated candidate'
assert not git('status','--porcelain'),'Tracked/untracked work must be clean before a run'
def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()
h=0
for c in str(root): h=(31*h+ord(c))&0xffffffff
build=(Path('/tmp')/f'fourthdimension-{h:x}'/'build') if ':' in str(root) and os.pathsep==':' else root/'build'
out=root/'build/profile-matrix'/source[:7]/a.profile
out.mkdir(parents=True,exist_ok=False)
opts={'base':[],'styles':['-PfdIntegrations=styles'],'engineered':['-PfdIntegrations=engineered'],'combined':['-PfdIntegrations=styles,engineered']}[a.profile]
cmd=['./gradlew','fdRecordRuntimeClasspath','build','runGameTestServer','--no-daemon','--console=plain','-I','build/profile-matrix/runtime-classpath.init.gradle','-PfdPerformanceTargets',f'-PfdClasspathRecord={out}/resolved-runtime-classpath.json',*opts]
env=dict(os.environ,JAVA_HOME='/usr/lib/jvm/java-21-openjdk')
meta={'source':source,'profile':a.profile,'command':cmd,'start_utc':datetime.datetime.now(datetime.timezone.utc).isoformat(),'platform':platform.platform(),'runtime_kind':'Gradle development GameTest server; not fdPackaged','java':subprocess.check_output([env['JAVA_HOME']+'/bin/java','-version'],stderr=subprocess.STDOUT,text=True)}
(out/'metadata.json').write_text(json.dumps(meta,indent=2)+'\n')
print('Starting',a.profile,'source',source,'log',out/'console.log',flush=True)
with (out/'console.log').open('w') as log:
 result=subprocess.run(cmd,env=env,stdout=log,stderr=subprocess.STDOUT)
meta.update(exit_code=result.returncode,end_utc=datetime.datetime.now(datetime.timezone.utc).isoformat())
text=(out/'console.log').read_text()
meta['required_test_summaries']=re.findall(r'All (\d+) required tests passed',text)
meta['complete_summaries']=re.findall(r'(\d+) GAME TESTS COMPLETE IN ([^=\n]+)',text)
meta['markers']=[line for line in text.splitlines() if any(x in line for x in ['FD_PERFORMANCE_','FD_FAMILY_MATRIX','FD_APPEARANCE','FD_COSMETIC_BOUNDARY','FD_PAX_CURVE_RESTORE','FD_ENGINEERED_INTERLOCK complete','FD_ENGINEERED_FE installed','FD_ENGINEERED installed','scenario not exercised'])]
for filename in ['gameTestServerLegacyClasspath.txt','gameTestServerRunVmArgs.txt','gameTestServerRunProgramArgs.txt']:
 f=build/'moddev'/filename
 if f.exists(): shutil.copy2(f,out/filename)
cp=build/'moddev/gameTestServerLegacyClasspath.txt'
resolved_path=out/'resolved-runtime-classpath.json'
if resolved_path.exists():
 resolved=json.loads(resolved_path.read_text()); paths=resolved['files']
 resolved['directories']=[name for name in paths if Path(name).is_dir()]
 resolved['files']=[{'path':name,'sha256':sha(Path(name)),'bytes':Path(name).stat().st_size} for name in paths if Path(name).is_file()]
 resolved_path.write_text(json.dumps(resolved,indent=2)+'\n')
meta['runtime_classpath']=[]
if cp.exists():
 for name in cp.read_text().splitlines():
  f=Path(name.strip())
  if f.is_file(): meta['runtime_classpath'].append({'path':str(f),'sha256':sha(f),'bytes':f.stat().st_size})
meta['source_files']={str(f.relative_to(root)):sha(f) for f in sorted((root/'src/main').rglob('*')) if f.is_file()}
meta['source_files'].update({name:sha(root/name) for name in ['build.gradle','gradle.properties']})
meta['compiled_files']={str(f.relative_to(build)):sha(f) for part in ['classes/java/main','resources/main'] for f in sorted((build/part).rglob('*')) if f.is_file()}
meta['cpu']=next((line.split(':',1)[1].strip() for line in Path('/proc/cpuinfo').read_text().splitlines() if line.startswith('model name')),'unknown')
meta['built_jars']={f.name:sha(f) for f in sorted((build/'libs').glob('*.jar'))}
logs=build/'runs/gametest/logs'
for name in ['latest.log','debug.log']:
 f=logs/name
 if f.exists():
  with gzip.GzipFile(filename=str(out/(name+'.gz')),mode='wb',mtime=0) as z: z.write(f.read_bytes())
world=build/'runs/gametest/world'
if world.exists(): shutil.copytree(world,out/'stopped-world')
meta['working_tree_after']=git('status','--porcelain')
meta['source_after']=git('rev-parse','HEAD')
(out/'metadata.json').write_text(json.dumps(meta,indent=2)+'\n')
print(json.dumps({k:meta[k] for k in ['exit_code','required_test_summaries','complete_summaries','markers']},indent=2),flush=True)
sys.exit(result.returncode)
