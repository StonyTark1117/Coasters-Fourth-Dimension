# Separate recordings: these timings are not pooled with the unprofiled benchmark.
from pathlib import Path
import re
b=Path(__file__).resolve().parent
source=(b/'run.py').read_text()
namespace={'__file__':str(b/'run.py')}
prefix=source.split('\ntry:\n with socket.socket()')[0]
prefix=prefix.replace("OUT/'status.json'", "OUT/'profile-status.json'").replace("OUT/'server.properties.original'", "OUT/'profile-server.properties.original'")
exec(prefix,namespace)
g=namespace
original=g['original']
try:
 assert (b/'run.exit').read_text().strip()=='0'
 seed=b/'worlds/FDServerBenchmarkV2Seed'
 for name,profile in [('base',None),('combined','engineered,styles,additions,magnetized,immersion')]:
  world=g['launch']('profile-'+name,profile,seed)
  g['step'](600)
  for mode in ['parked','operating']:
   g['rcon']('jfr start')
   if mode=='parked':g['rcon']('tick unfreeze')
   else:g['action']('operate')
   d=g['sample']('profile-'+name+'-'+mode);g['check'](d,True)
   output=g['rcon']('jfr stop')
   candidates=list((g['RUNTIME']/'debug').rglob('*.jfr'))
   assert candidates,output
   recording=max(candidates,key=lambda p:p.stat().st_mtime)
   g['shutil'].copyfile(recording,b/(name+'-'+mode+'.jfr'))
  g['rcon']('tick freeze')
  g['stop'](world)
 g['STATE']['status']='complete'
 (b/'profile-status.json').write_text(g['json'].dumps(g['STATE'],indent=2)+'\n')
 (b/'profile.exit').write_text('0\n')
except BaseException:
 (b/'profile.exit').write_text('1\n')
 server=g['server']
 if server is not None and server.poll() is None:
  try:g['rcon']('stop');server.wait(timeout=120)
  except BaseException:pass
 raise
finally:
 (g['RUNTIME']/'server.properties').write_bytes(original)

 (b/'profile-settings-restored.json').write_text(g['json'].dumps({'before':g['hashlib'].sha256(original).hexdigest(),'after':g['hashlib'].sha256((g['RUNTIME']/'server.properties').read_bytes()).hexdigest()},indent=2)+'\n')
