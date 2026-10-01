from pathlib import Path
import os,subprocess,time,json
out=Path(__file__).resolve().parent;root=out.parents[1]
try:
 while not (out/'magnetized.exit').exists():
  os.kill(175334,0);time.sleep(2)
 assert (out/'magnetized.exit').read_text().strip()=='1'
 for name in ('magnetized.log','magnetized.exit'):(out/name).rename(out/('layout-first'+name[len('magnetized'):]))
 for name,profile in [('magnetized','magnetized'),('combined','engineered,styles,additions,magnetized,immersion'),('base',None)]:
  cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','build','runGameTestServer']
  if profile:cmd.append('-PfdIntegrations='+profile)
  if name=='combined':cmd.append('-PfdPaxEnergyReload')
  with (out/(name+'.log')).open('w') as f:
   p=subprocess.Popen(cmd,cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,env={**os.environ,'JAVA_HOME':'/usr/lib/jvm/java-21-openjdk','JAVA_TOOL_OPTIONS':'-Xmx2G'})
   (out/'status.json').write_text(json.dumps({'profile':name,'pid':p.pid,'status':'running'})+'\n');code=p.wait()
  (out/(name+'.exit')).write_text(str(code)+'\n');assert code==0,name+' failed'
 (out/'status.json').write_text(json.dumps({'status':'complete'})+'\n');(out/'followup.exit').write_text('0\n')
except BaseException as e:
 (out/'followup.exit').write_text('1\n');raise
