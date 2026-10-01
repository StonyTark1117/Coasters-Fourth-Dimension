from pathlib import Path
import json,socket,hashlib,shutil
b=Path(__file__).resolve().parent;rt=Path('/tmp/fourthdimension-48535694/build/runs')
handles=[]
for case in ['before','after','base','combined']:
 d=b/case;assert (d/'server.exit').read_text().strip()=='0';handles.append(json.loads((d/'server.json').read_text())['pid'])
 for c in json.loads((d/'clients.json').read_text()):
  assert (d/(c['label']+'.exit')).read_text().strip()=='0'
  handles.extend([c['wrapper_pid'],c['display_pid']])
assert all(not Path('/proc',str(pid)).exists() for pid in handles)
for port in [25585,25586]:
 with socket.socket() as s:assert s.connect_ex(('127.0.0.1',port))!=0
settings={}
for src,target in [(b/'server.properties.original',rt/'packaged-server/server.properties'),(b/'original-options.txt',rt/'packaged-client/options.txt'),(b/'original-observer-options.txt',rt/'packaged-observer/options.txt'),(b/'original-dev-coasters_immersion-client.toml',rt/'packaged-client/config/coasters_immersion-client.toml'),(b/'original-observer-coasters_immersion-client.toml',rt/'packaged-observer/config/coasters_immersion-client.toml')]:
 shutil.copyfile(src,target);assert src.read_bytes()==target.read_bytes()
 settings[str(target)]=hashlib.sha256(target.read_bytes()).hexdigest()
(b/'cleanup.json').write_text(json.dumps({'handles':handles,'handles_gone':True,'ports_closed':True,'settings_restored':True,'settings_sha256':settings},indent=2)+'\n');print('restored',len(settings),'settings;',len(handles),'handles gone')
