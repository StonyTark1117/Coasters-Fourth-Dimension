from pathlib import Path
import json,os,subprocess,time
out=Path(__file__).resolve().parent;root=out.parents[1]
try:
 while not (out/'fixed/run.exit').exists():
  os.kill(202272,0);time.sleep(2)
 assert (out/'fixed/run.exit').read_text().strip()=='0'
 for name,profile in [('base',None),('magnetized','magnetized'),('combined','engineered,styles,additions,magnetized,immersion')]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runGameTestServer']
  if profile:cmd.append('-PfdIntegrations='+profile)
  if name=='combined':cmd.append('-PfdPaxEnergyReload')
  with (out/('native-'+name+'.log')).open('w') as f:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   (out/'regressions-status.json').write_text(json.dumps({'profile':name,'pid':p.pid,'status':'running'})+'\n');code=p.wait()
  (out/('native-'+name+'.exit')).write_text(str(code)+'\n');assert code==0,name+' failed'
 (out/'regressions-status.json').write_text(json.dumps({'status':'complete'})+'\n');(out/'regressions.exit').write_text('0\n')
except BaseException:
 (out/'regressions.exit').write_text('1\n');raise
