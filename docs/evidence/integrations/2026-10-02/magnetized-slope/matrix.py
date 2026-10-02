from pathlib import Path
import subprocess,os,json,shutil
root=Path(__file__).resolve().parents[2];out=Path(__file__).resolve().parent
try:
 for name,profile in [('magnetized','magnetized'),('combined','engineered,styles,additions,magnetized,immersion'),('base',None)]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2']
  if name=='magnetized':cmd.append('build')
  cmd.append('runGameTestServer')
  if profile:cmd.append('-PfdIntegrations='+profile)
  if name=='combined':cmd.append('-PfdPaxEnergyReload')
  with (out/(name+'.log')).open('w') as log:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   state={'status':'running','profile':name,'pid':p.pid,'command':cmd}
   (out/'matrix-status.json').write_text(json.dumps(state)+'\n');code=p.wait()
  (out/(name+'.exit')).write_text(str(code)+'\n')
  state.update(status='complete',exit_status=code)
  (out/(name+'-run.json')).write_text(json.dumps(state,indent=2)+'\n')
  assert code==0,'Profile failed: '+name
  text=(out/(name+'.log')).read_text()
  assert 'All 38 required tests passed' in text
  if profile:assert 'FD_MAGNETIZED_SLOPE_COMPLETE families=4 lanes=8 riders=8 phases=10 paxExternalDrive=false' in text
  if name=='magnetized':
   build=Path('/tmp/fourthdimension-48535694/build')
   shutil.copyfile(build/'libs/coasters-fourth-dimension-0.9.0-dev.1.jar',out/'verified.jar')
   shutil.copytree(build/'test-results/test',out/'unit',dirs_exist_ok=True)
 (out/'matrix-status.json').write_text(json.dumps({'status':'complete'})+'\n');(out/'matrix.exit').write_text('0\n')
except BaseException as e:
 (out/'matrix-error.txt').write_text(repr(e)+'\n');(out/'matrix.exit').write_text('1\n');raise
