from pathlib import Path
import json,os,subprocess,sys,time
b=Path(__file__).resolve().parent;c=json.loads((b/'clients.json').read_text())[0]
assert Path('/proc',str(c['display_pid'])).exists() and Path('/proc',str(c['wrapper_pid'])).exists()
env={**os.environ,'DISPLAY':c['display']}
def x(*args):return subprocess.check_output(['xdotool',*map(str,args)],env=env,text=True).strip()
def capture(name):
 time.sleep(.35);subprocess.run(['import','-window','root',str(b/(name+'.png'))],env=env,check=True)
if __name__=='__main__':
 if sys.argv[1]=='capture':capture(sys.argv[2])
 elif sys.argv[1]=='key':x('key',sys.argv[2]);capture(sys.argv[3])
 elif sys.argv[1]=='hover':x('mousemove',sys.argv[2],sys.argv[3]);capture(sys.argv[4])
 elif sys.argv[1]=='click':x('mousemove',sys.argv[2],sys.argv[3]);x('click','1');capture(sys.argv[4])
