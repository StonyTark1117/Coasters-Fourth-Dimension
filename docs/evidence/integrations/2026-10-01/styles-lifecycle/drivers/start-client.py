from pathlib import Path
import os,subprocess,json,sys,shlex
root=Path(__file__).resolve().parents[2];base=Path(__file__).resolve().parent/sys.argv[2]
label=sys.argv[1];phase='fixed';out=base
state=json.loads((out/'clients.json').read_text()) if (out/'clients.json').exists() else []
assert label not in [x['label'] for x in state]
r,w=os.pipe()
with (out/(label+'-xvfb.log')).open('w') as log:
 display=subprocess.Popen(['Xvfb','-displayfd',str(w),'-screen','0','1280x720x24','-nolisten','tcp','-noreset'],pass_fds=(w,),stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
os.close(w)
with os.fdopen(r) as pipe:number=pipe.readline().strip()
assert number.isdigit() and display.poll() is None
(out/(label+'-display.txt')).write_text(':'+number+'\n')
cmd=['./gradlew','-Dorg.gradle.jvmargs=-Xmx1G','--max-workers=2',{'dev':'runClient','observer':'runObserver'}[label],'-PfdPackaged','-PfdPackagedJar=/home/braydon/.cache/fourthdimension-48535694/styles-lifecycle.jar','-PfdValidation','-PfdConnect=127.0.0.1:25585']
if sys.argv[2]!='removed':cmd.append('-PfdIntegrations=styles')
shell=out/(label+'.sh');shell.write_text('#!/bin/bash\nexport JAVA_HOME=/usr/lib/jvm/java-21-openjdk\nexport JAVA_TOOL_OPTIONS=-Xmx3G\nexport DISPLAY=:'+number+'\nexport LIBGL_ALWAYS_SOFTWARE=1\nexport ALSOFT_DRIVERS=null\nexport XDG_SESSION_TYPE=x11\nunset WAYLAND_DISPLAY\n'+shlex.join(cmd)+' > '+shlex.quote(str(out/(label+'.log')))+' 2>&1\nresult=$?\nprintf \'%s\\n\' "$result" > '+shlex.quote(str(out/(label+'.exit')))+'\nexit "$result"\n')
with (out/(label+'-wrapper.log')).open('w') as log:
 client=subprocess.Popen(['bash',str(shell)],cwd=root,stdin=subprocess.DEVNULL,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
state.append({'label':label,'display':':'+number,'display_pid':display.pid,'wrapper_pid':client.pid})
(out/'clients.json').write_text(json.dumps(state,indent=2)+'\n');print(state[-1])
