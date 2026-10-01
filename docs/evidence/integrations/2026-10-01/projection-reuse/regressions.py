from pathlib import Path
import json,os,subprocess,shutil,hashlib
out=Path(__file__).resolve().parent;root=out.parents[1]
jar=Path('/home/braydon/.cache/fourthdimension-48535694/server-projection-reuse.jar')
source=Path('/tmp/fourthdimension-48535694/build/libs/coasters-fourth-dimension-0.9.0-dev.1.jar')
if jar.exists():assert jar.read_bytes()==source.read_bytes()
else:shutil.copyfile(source,jar)
state={'status':'running','artifact_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'runs':[]}
def save():(out/'regressions-status.json').write_text(json.dumps(state,indent=2)+'\n')
try:
 for name,profile in [('base',None),('engineered','engineered'),('styles','styles'),('additions','additions'),('magnetized','magnetized'),('immersion','immersion'),('combined','engineered,styles,additions,magnetized,immersion')]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runGameTestServer']
  if profile:cmd.append('-PfdIntegrations='+profile)
  if name in ['engineered','combined']:cmd.append('-PfdPaxEnergyReload')
  with (out/('native-'+name+'.log')).open('w') as f:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   row={'profile':name,'pid':p.pid,'status':'running','command':cmd};state['runs'].append(row);save();code=p.wait()
  (out/('native-'+name+'.exit')).write_text(str(code)+'\n');assert code==0,name+' failed'
  row['status']='complete';save();print(name,'passed',flush=True)
 state['status']='complete';save();(out/'regressions.exit').write_text('0\n')
except BaseException as e:
 state['status']='failed';state['error']=repr(e);save();(out/'regressions.exit').write_text('1\n');raise
