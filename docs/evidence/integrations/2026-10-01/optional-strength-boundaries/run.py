from pathlib import Path
import os,subprocess,json
b=Path(__file__).resolve().parent;root=b.parents[1]
try:
 for name,profile in [('base',None),('additions','additions'),('magnetized','magnetized')]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2']
  if name=='base':cmd.append('build')
  cmd.append('runGameTestServer')
  if profile:cmd.append('-PfdIntegrations='+profile)
  with (b/(name+'.log')).open('w') as f:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   (b/'status.json').write_text(json.dumps({'status':'running','profile':name,'pid':p.pid,'command':cmd})+'\n');code=p.wait()
  (b/(name+'.exit')).write_text(str(code)+'\n');assert code==0,name+' failed'
 (b/'status.json').write_text(json.dumps({'status':'complete'})+'\n');(b/'run.exit').write_text('0\n')
except BaseException:
 (b/'run.exit').write_text('1\n');raise
