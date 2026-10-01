from pathlib import Path
import hashlib,json,os,secrets,shlex,socket,subprocess
b=Path(__file__).resolve().parent;root=b.parents[1];runtime=Path('/tmp/fourthdimension-48535694/build/runs/packaged-server')
assert json.loads((b/'native-status.json').read_text())['artifact']['path'].endswith('/acquisition.jar')
with socket.socket() as s:assert s.connect_ex(('127.0.0.1',25585))!=0
world='FDAcquisition';assert not (runtime/world).exists()
(b/'server.properties.original').write_bytes((runtime/'server.properties').read_bytes())
(runtime/'server.properties').write_text('level-name='+world+'\nserver-ip=127.0.0.1\nserver-port=25585\nrcon.port=25586\nrcon.password='+secrets.token_hex(24)+'\nenable-rcon=true\nonline-mode=false\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nlevel-type=minecraft:flat\ngenerate-structures=false\nallow-flight=true\n')
cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2','runServer','-PfdPackaged','-PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/acquisition.jar','-PfdValidation']
shell=b/'server.sh';shell.write_text('#!/bin/bash\nexport JAVA_HOME=/usr/lib/jvm/java-21-openjdk\nexport JAVA_TOOL_OPTIONS=-Xmx2G\n'+shlex.join(cmd)+' > '+shlex.quote(str(b/'server.log'))+' 2>&1\ncode=$?\nprintf \'%s\\n\' "$code" > '+shlex.quote(str(b/'server.exit'))+'\nexit "$code"\n')
with (b/'server-wrapper.log').open('w') as f:p=subprocess.Popen(['bash',str(shell)],cwd=root,stdin=subprocess.DEVNULL,stdout=f,stderr=subprocess.STDOUT,start_new_session=True)
(b/'server.pid').write_text(str(p.pid)+'\n');print('server',p.pid)
