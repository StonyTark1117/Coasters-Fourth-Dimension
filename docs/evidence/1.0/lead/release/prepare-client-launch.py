"""Prepare a Linux launch from installed vanilla/NeoForge version JSON, without ModDev.
Run only after the ordinary NeoForge installer has populated this isolated client.
This uses an offline test identity solely for the owned loopback test server.
"""
import argparse, hashlib, json, os, platform, re, shlex, shutil, urllib.request, uuid, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('client',type=Path);p.add_argument('--asset-cache',type=Path);p.add_argument('--endpoint',required=True);a=p.parse_args()
root=a.client.resolve();assert root.is_absolute() and not root.is_relative_to('/tmp')
assert a.endpoint.startswith('127.0.0.1:')
base=json.loads((root/'versions/1.21.1/1.21.1.json').read_text())
neo=json.loads((root/'versions/neoforge-21.1.256/neoforge-21.1.256.json').read_text())
assert base['id']=='1.21.1' and neo['inheritsFrom']=='1.21.1'
records=[]
def digest(path):return hashlib.sha1(path.read_bytes()).hexdigest()
def fetch(info,path,cache=None):
 path.parent.mkdir(parents=True,exist_ok=True);expected=info.get('sha1')
 if not path.exists() or expected and digest(path)!=expected:
  if cache and cache.exists() and (not expected or digest(cache)==expected):shutil.copy2(cache,path)
  else:
   url=info.get('url');assert url and url.startswith('https://'),info
   with urllib.request.urlopen(url,timeout=60) as response:path.write_bytes(response.read())
 if expected:assert digest(path)==expected,path
 records.append({'path':str(path),'sha1':digest(path),'source':info.get('url','installer output')})
 return path

def allowed(obj):
 if 'rules' not in obj:return True
 result=False
 for rule in obj['rules']:
  osrule=rule.get('os',{});matches=True
  if 'name' in osrule:matches &= osrule['name']=='linux'
  if 'arch' in osrule:matches &= bool(re.fullmatch(osrule['arch'],platform.machine()))
  if 'version' in osrule:matches &= bool(re.search(osrule['version'],platform.release()))
  for key,wanted in rule.get('features',{}).items():matches &= wanted is False
  if matches:result=rule['action']=='allow'
 return result

def coordinate_path(name):
 name,_,extension=name.partition('@');parts=name.split(':');group,artifact,version=parts[:3]
 suffix='-'+parts[3] if len(parts)>3 else ''
 return '/'.join([group.replace('.','/'),artifact,version,artifact+'-'+version+suffix+'.'+(extension or 'jar')])

def key(lib):
 parts=lib['name'].split(':');return tuple(parts[:2]+parts[3:])
libraries={key(v):v for v in base['libraries']}
libraries.update({key(v):v for v in neo['libraries']})
cp=[];natives=root/'natives';natives.mkdir(exist_ok=True)
def extract_native(path):
 with zipfile.ZipFile(path) as z:
  for name in z.namelist():
   if name.endswith('.so') or '.so.' in Path(name).name:
    (natives/Path(name).name).write_bytes(z.read(name))
for lib in libraries.values():
 if not allowed(lib):continue
 downloads=lib.get('downloads',{});artifact=downloads.get('artifact')
 if artifact:
  path=fetch(artifact,root/'libraries'/artifact['path']);cp.append(path)
  if 'natives-linux' in lib['name']:extract_native(path)
 elif 'url' in lib:
  relative=coordinate_path(lib['name']);path=fetch({'url':lib['url'].rstrip('/')+'/'+relative},root/'libraries'/relative);cp.append(path)
 else:
  path=root/'libraries'/coordinate_path(lib['name']);assert path.exists(),path;cp.append(path)
 if 'linux' in lib.get('natives',{}):
  classifier=lib['natives']['linux'].replace('${arch}','64');info=downloads['classifiers'][classifier]
  extract_native(fetch(info,root/'libraries'/info['path']))
# Match the selected version basename used by NeoForge's declared ignoreList.
vanilla=root/'versions/1.21.1/1.21.1.jar';assert digest(vanilla)==base['downloads']['client']['sha1']
selected=root/'versions'/neo['id']/(neo['id']+'.jar');shutil.copy2(vanilla,selected);cp.append(selected)
assets=root/'assets';index=base['assetIndex'];index_path=fetch(index,assets/'indexes'/(index['id']+'.json'),a.asset_cache/'indexes'/(index['id']+'.json') if a.asset_cache else None)
objects=json.loads(index_path.read_text())['objects']
for info in objects.values():
 sha=info['hash'];relative=Path('objects')/sha[:2]/sha
 fetch({'sha1':sha,'url':'https://resources.download.minecraft.net/'+sha[:2]+'/'+sha},assets/relative,a.asset_cache/relative if a.asset_cache else None)
player='Dev';offline=uuid.UUID(bytes=hashlib.md5(('OfflinePlayer:'+player).encode()).digest(),version=3)
values={'natives_directory':str(natives),'launcher_name':'FourthDimensionCleanInstallCheck','launcher_version':'1',
 'classpath':os.pathsep.join(map(str,cp)),'classpath_separator':os.pathsep,'library_directory':str(root/'libraries'),
 'version_name':neo['id'],'version_type':'release','game_directory':str(root),'assets_root':str(assets),
 'assets_index_name':index['id'],'auth_player_name':player,'auth_uuid':offline.hex,'auth_access_token':'0',
 'clientid':'0','auth_xuid':'0','user_type':'legacy','user_properties':'{}'}
def expand(text):
 return re.sub(r'\$\{([^}]+)\}',lambda m:values[m[1]],text)
def arguments(items):
 result=[]
 for item in items:
  if isinstance(item,str):result.append(expand(item))
  elif allowed(item):result.extend(expand(v) for v in (item['value'] if isinstance(item['value'],list) else [item['value']]))
 return result
args=['-Xmx1536M']+arguments(base['arguments']['jvm'])+arguments(neo['arguments']['jvm'])+[neo['mainClass']]+arguments(base['arguments']['game'])+arguments(neo['arguments']['game'])+['--width','1280','--height','720','--quickPlayMultiplayer',a.endpoint]
argfile=root/'clean-client.args';argfile.write_text('\n'.join('"'+v.replace('\\','\\\\').replace('"','\\"')+'"' for v in args)+'\n')
script=root.parent/'client-launch.sh';script.write_text('#!/bin/sh\ncd '+shlex.quote(str(root))+'\nexec /usr/lib/jvm/java-21-openjdk/bin/java @'+shlex.quote(str(argfile))+'\n')
(root/'clean-launch-manifest.json').write_text(json.dumps({'main_class':neo['mainClass'],'base_version':base['id'],'neoforge_version':neo['id'],'offline_identity':player,'endpoint':a.endpoint,'files':records},indent=2)+'\n')
print('Prepared normal NeoForge client launch:',script)
