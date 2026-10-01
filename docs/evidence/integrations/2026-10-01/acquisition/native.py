from pathlib import Path
import hashlib,json,os,shutil,subprocess
b=Path(__file__).resolve().parent;root=b.parents[1]
state={'status':'running','runs':[]}
def save():(b/'native-status.json').write_text(json.dumps(state,indent=2)+'\n')
try:
 for case,extra in [('build', ['build']),('base',['runGameTestServer']),('combined',['runGameTestServer','-PfdIntegrations=engineered,styles,additions,magnetized,immersion','-PfdPaxEnergyReload'])]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2',*extra]
  with (b/('native-'+case+'.log')).open('w') as f:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   row={'case':case,'pid':p.pid,'command':cmd,'status':'running'};state['runs'].append(row);save();code=p.wait()
  (b/('native-'+case+'.exit')).write_text(str(code)+'\n');assert code==0,case
  row['status']='complete';save()
  if case=='build':
   jar=Path('/home/braydon/.cache/fourthdimension-48535694/acquisition-v2.jar');assert not jar.exists()
   shutil.copyfile('/tmp/fourthdimension-48535694/build/libs/coasters-fourth-dimension-0.9.0-dev.1.jar',jar)
   state['artifact']={'path':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()};save()
 state['status']='complete';save();(b/'native.exit').write_text('0\n')
except BaseException as e:
 state.update(status='failed',error=repr(e));save();(b/'native.exit').write_text('1\n');raise
