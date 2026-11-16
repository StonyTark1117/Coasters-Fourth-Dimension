"""Verify the intended local release JAR against current source and accepted FD917 assets."""
import argparse,hashlib,json,subprocess,tomllib,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('jar',type=Path);p.add_argument('--version',required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
repo=Path('/home/braydon/Documents/Coasters: Fourth Dimension');resources=repo/'src/main/resources'
accepted=json.loads((repo/'docs/evidence/fd917/lead/integration-package.json').read_text());frozen=Path(accepted['jar'])
assert hashlib.sha256(frozen.read_bytes()).hexdigest()==accepted['sha256']
result={'jar':str(a.jar.resolve()),'sha256':hashlib.sha256(a.jar.read_bytes()).hexdigest(),'version':a.version,'head':subprocess.check_output(['git','rev-parse','HEAD'],cwd=repo,text=True).strip(),'source_resources':{},'fd917_input':accepted,'asset_changes_since_frozen':[]}
with zipfile.ZipFile(a.jar) as z,zipfile.ZipFile(frozen) as old:
 names=z.namelist();files=[n for n in names if not n.endswith('/')]
 assert len(names)==len(set(names)),'Duplicate ZIP entry'
 assert not any(n.endswith('.jar') for n in files),'Unexpected bundled upstream JAR'
 assert not any(n.startswith(('assets-source/','docs/','logs/','run/','world/')) for n in files),'Authoring/run data in distribution'
 assert all(n.startswith(('META-INF/','assets/fourthdimension/','data/','dev/fourthdimension/')) or n in ('LICENSE','pack.mcmeta','fourthdimension.mixins.json','fourthdimension-skin-provider-classes.txt') for n in files),[n for n in files if not n.startswith(('META-INF/','assets/fourthdimension/','data/','dev/fourthdimension/')) and n not in ('LICENSE','pack.mcmeta','fourthdimension.mixins.json','fourthdimension-skin-provider-classes.txt')]
 metadata=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert metadata['mods'][0]['version']==a.version
 assert metadata['license']=='MIT' and 'Seventeen' in metadata['mods'][0]['description']
 result['metadata']=metadata
 for source in resources.rglob('*'):
  if not source.is_file():continue
  name=source.relative_to(resources).as_posix();data=source.read_bytes()
  if name=='META-INF/neoforge.mods.toml':data=data.replace(b'${version}',a.version.encode())
  assert z.read(name)==data,name+' differs from source'
  result['source_resources'][name]=hashlib.sha256(data).hexdigest()
 for name in files:
  if name.startswith('assets/fourthdimension/'):
   assert name in old.namelist(),'New asset must be reconciled: '+name
   if z.read(name)!=old.read(name):result['asset_changes_since_frozen'].append(name)
 assert not result['asset_changes_since_frozen'],'Changed accepted art needs scoped reconciliation'
 assert z.read('META-INF/LICENSE-fourthdimension.txt')==(repo/'LICENSE').read_bytes()
 result['resource_count']=len(result['source_resources']);result['asset_count']=sum(n.startswith('assets/fourthdimension/') for n in result['source_resources'])
 result['method']='Current resource bytes plus metadata expansion checked against JAR; all runtime asset bytes unchanged from the accepted final FD917 package and authoritative-export reconciliation. No generator rerun needed for unchanged assets.'
 result['status']='pass'
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,indent=2)+'\n');print('Package verified:',result['resource_count'],'resources;',result['asset_count'],'unchanged FD917 assets')
