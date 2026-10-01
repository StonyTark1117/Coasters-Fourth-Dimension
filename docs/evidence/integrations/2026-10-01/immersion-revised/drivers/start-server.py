from pathlib import Path
import hashlib,json,os,secrets,shlex,shutil,socket,subprocess,sys
b=Path(__file__).resolve().parent;root=b.parents[1];case=sys.argv[1];out=b/case;out.mkdir(exist_ok=True)
assert case in ['immersion','combined']
runtime=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server');jar=Path('/home/braydon/.cache/fourthdimension-48535694/immersion-revised.jar')
source=Path('/tmp/fourthdimension-48535694/build/libs/coasters-fourth-dimension-0.9.0-dev.1.jar')
if jar.exists():assert jar.read_bytes()==source.read_bytes()
else:shutil.copyfile(source,jar)
with socket.socket() as s:assert s.connect_ex(('127.0.0.1',25585))!=0
world='FDImmersionRevised'+case.title();assert not (runtime/world).exists()
if not (b/'server.properties.original').exists():(b/'server.properties.original').write_bytes((runtime/'server.properties').read_bytes())
(runtime/'server.properties').write_text('level-name='+world+'\nserver-ip=127.0.0.1\nserver-port=25585\nrcon.port=25586\nrcon.password='+secrets.token_hex(24)+'\nenable-rcon=true\nonline-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nlevel-type=minecraft:flat\ngenerate-structures=false\nallow-flight=true\n')
cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runServer','-PfdPackaged','-PfdPackagedJar='+str(jar),'-PfdValidation','-PfdFrozen']
cmd.append('-PfdIntegrations='+('engineered,styles,additions,magnetized,immersion' if case=='combined' else 'immersion'))
shell=out/'server.sh';shell.write_text('#!/bin/bash\nexport JAVA_HOME=/usr/lib/jvm/java-21-openjdk\nexport JAVA_TOOL_OPTIONS=-Xmx2G\n'+shlex.join(cmd)+' > '+shlex.quote(str(out/'server.log'))+' 2>&1\ncode=$?\nprintf \'%s\\n\' "$code" > '+shlex.quote(str(out/'server.exit'))+'\nexit "$code"\n')
with (out/'server-wrapper.log').open('w') as f:p=subprocess.Popen(['bash',str(shell)],cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,start_new_session=True)
(out/'server.pid').write_text(str(p.pid)+'\n');(out/'server.json').write_text(json.dumps({'pid':p.pid,'case':case,'world':world,'command':cmd,'artifact':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},indent=2)+'\n');print(case,'server',p.pid)
