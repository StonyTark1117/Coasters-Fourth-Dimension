from pathlib import Path
b=Path(__file__).resolve().parent
source=(b/'run.py').read_text().split('\ntry:\n with socket.socket()')[0]
source=source.replace("OUT/'status.json'", "OUT/'power-status.json'").replace("OUT/'server.properties.original'", "OUT/'power-server.properties.original'")
g={'__file__':str(b/'run.py')};exec(source,g)
try:
 world=g['launch']('power-inspection',None,b/'worlds/FDServerBenchmarkV2Seed');g['step'](100)
 for p in ['-607 180 608','-606 180 608']:
  print(p,g['rcon']('data get block '+p),flush=True)
 g['rcon']('setblock -607 180 608 minecraft:air');g['step'](5)
 g['rcon']('setblock -607 180 608 create:creative_motor[facing=east]{ScrollValue:128}');g['step'](100)
 for p in ['-607 180 608','-606 180 608']:
  print('replaced',p,g['rcon']('data get block '+p),flush=True)
 g['action']('operate');g['step'](20);g['action']('report');g['stop'](world)
 (b/'power.exit').write_text('0\n')
finally:
 if g['server'] is not None and g['server'].poll() is None:
  g['rcon']('stop');g['server'].wait(timeout=120)
 (g['RUNTIME']/'server.properties').write_bytes(g['original'])
