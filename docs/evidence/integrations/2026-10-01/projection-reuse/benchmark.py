from pathlib import Path
import os,time,json,hashlib
b=Path(__file__).resolve().parent;root=b.parents[1]
# Wait only on the verified regression coordinator, keeping measurements sequential.
pid=int((b/'regressions.pid').read_text())
while not (b/'regressions.exit').exists():
 os.kill(pid,0);time.sleep(3)
assert (b/'regressions.exit').read_text().strip()=='0'
source=(b/'benchmark-helper.py').read_text()
g={'__file__':str(b/'benchmark.py')};exec(source,g)
old=Path('/home/braydon/.cache/fourthdimension-48535694/server-benchmark-v2.jar');new=g['JAR']
g['STATE']={'status':'running','artifacts':{'old':{'path':str(old),'sha256':hashlib.sha256(old.read_bytes()).hexdigest()},'new':{'path':str(new),'sha256':hashlib.sha256(new.read_bytes()).hexdigest()}},'runs':[json.loads((b/'failed-attempt/status.json').read_text())['runs'][0]],'resumed_after':'partial log record in first base-0-new attempt; original artifacts retained in failed-attempt'}
seed=root/'build/server-benchmark/worlds/FDServerBenchmarkV2Seed'
(b/'source-seed.json').write_text(json.dumps({'path':str(seed),'foundation_sha256':hashlib.sha256((seed/'data/fourthdimension_foundation.dat').read_bytes()).hexdigest()},indent=2)+'\n')
try:
 for repeat in range(2):
  for name,profile in [('base',None),('combined','engineered,styles,additions,magnetized,immersion')]:
   for version in (['old','new'] if repeat==0 else ['new','old']):
    case=f'{name}-{repeat}-{version}';g['JAR']=old if version=='old' else new

    if case=='base-0-old':continue
    world=g['launch'](case,profile,seed);g['step'](600);g['rcon']('tick unfreeze')
    g['check'](g['sample'](case+'-parked'),True)
    g['action']('operate');g['check'](g['sample'](case+'-operating'),True)
    g['rcon']('tick freeze');g['stop'](world)
 g['STATE']['status']='complete';g['state']();(b/'benchmark.exit').write_text('0\n')
except BaseException as e:
 g['STATE']['status']='failed';g['STATE']['error']=repr(e);g['state']();(b/'benchmark.exit').write_text('1\n')
 if g['server'] is not None and g['server'].poll() is None:
  g['rcon']('stop');g['server'].wait(timeout=120)
 raise
finally:
 assert g['server'] is None or g['server'].poll() is not None,'Server must terminate before settings restoration'
 (g['RUNTIME']/'server.properties').write_bytes(g['original'])
 (b/'settings-restored.json').write_text(json.dumps({'before':hashlib.sha256(g['original']).hexdigest(),'after':hashlib.sha256((g['RUNTIME']/'server.properties').read_bytes()).hexdigest()},indent=2)+'\n')
