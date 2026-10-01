from pathlib import Path
b=Path(__file__).resolve().parent
g={'__file__':str(b/'run.py')}
exec((b/'run.py').read_text().split('\ntry:\n with socket.socket()')[0],g)
g['STATE']=g['json'].loads((b/'attempt-2/status.json').read_text())
g['STATE']['runs']=[r for r in g['STATE']['runs'] if r['status']=='complete']
g['STATE']['status']='running';g['STATE'].pop('error',None);g['state']()
(b/'run.exit').unlink()
try:
 seed=b/'worlds/FDServerBenchmarkV2Seed'
 for n in range(3):
  for name,profile in [('base',None),('combined','engineered,styles,additions,magnetized,immersion')]:
   label=name+'-operating-'+str(n)
   world=g['launch']('resumed-'+label,profile,seed);g['step'](600)
   g['rcon']('tick unfreeze');g['action']('operate')
   g['check'](g['sample'](label),True);g['rcon']('tick freeze');g['stop'](world)
 g['STATE']['status']='complete';g['state']();(b/'run.exit').write_text('0\n')
except BaseException as e:
 g['STATE']['status']='failed';g['STATE']['error']=repr(e);g['state']();(b/'run.exit').write_text('1\n')
 if g['server'] is not None and g['server'].poll() is None:
  g['rcon']('stop');g['server'].wait(timeout=120)
 raise
finally:
 (g['RUNTIME']/'server.properties').write_bytes(g['original'])
 (b/'settings-restored.json').write_text(g['json'].dumps({'before':g['hashlib'].sha256(g['original']).hexdigest(),'after':g['hashlib'].sha256((g['RUNTIME']/'server.properties').read_bytes()).hexdigest()},indent=2)+'\n')
