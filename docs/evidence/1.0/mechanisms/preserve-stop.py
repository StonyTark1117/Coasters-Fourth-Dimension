"""Preserve stopped phase world and decode exact saved winch/FoundationData."""
from pathlib import Path
import importlib.util,json,sys,shutil,subprocess,gzip,zlib,struct,hashlib
ROOT=Path('/mnt/NVME4/fourthdimension-runs/fd10-mechanisms-20261115')
phase=sys.argv[1].upper();assert phase in ('OUTBOUND','RETURNING','CAPTURING','BOARDING')
state=subprocess.check_output(['systemctl','--user','show','fd10-mechanisms-20261115-server.service','-p','ActiveState','-p','MainPID'],text=True)
assert 'ActiveState=inactive' in state and 'MainPID=0' in state,state
out=ROOT/'checkpoints'/phase.lower();out.mkdir(parents=True,exist_ok=True)
if (out/'world').exists():
 assert (out/'world/level.dat').read_bytes()==(ROOT/'server/PaxLifecycle/level.dat').read_bytes()
else:shutil.copytree(ROOT/'server/PaxLifecycle',out/'world')
shutil.copy2(ROOT/'server/logs/latest.log',out/'server.log')
shutil.copy2(ROOT/'evidence/server.log',out/'stdout.log')
log=(out/'stdout.log').read_text();rows=[]
for line in log.splitlines():
 if 'FD902_STATE ' in line:
  try:rows.append(json.loads(line.split('FD902_STATE ',1)[1]))
  except json.JSONDecodeError:pass
before=next(x for x in reversed(rows) if x['label']=='normal-stop-'+phase)
(out/'before-stop.json').write_text(json.dumps(before,indent=2)+'\n')
spec=importlib.util.spec_from_file_location('nbt',str(Path(__file__).resolve().parents[4]/'scripts/inspect-player-save.py'));m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
# Declared fixture winch (-6,105,73), chunk(-1,4), region(-1,0).
raw=(out/'world/region/r.-1.0.mca').read_bytes();index=31+4*32;offset=int.from_bytes(raw[index*4:index*4+3],'big')*4096
length=struct.unpack('>I',raw[offset:offset+4])[0];kind=raw[offset+4];data=raw[offset+5:offset+4+length]
assert kind in (1,2,3),kind
unpacked=gzip.decompress(data) if kind==1 else zlib.decompress(data) if kind==2 else data
p=out/'winch-chunk.nbt.gz';p.write_bytes(gzip.compress(unpacked,mtime=0));chunk,_=m.read_nbt(p)
winch=next(b for b in chunk['block_entities'] if (b['x'],b['y'],b['z'])==(-6,105,73))
assert winch['shuttle_phase']==phase,winch['shuttle_phase']
(out/'saved-winch.json').write_text(json.dumps(winch,indent=2)+'\n')
f=out/'world/data/fourthdimension_foundation.dat';foundation,digest=m.read_nbt(f)
(out/'saved-foundation.json').write_text(json.dumps(foundation,indent=2)+'\n')
(out/'identity.json').write_text(json.dumps({'phase':phase,'world':str(out/'world'),'foundation_sha256':digest,'saved_winch_distance':winch['shuttle_distance'],'server_state':state},indent=2)+'\n')
print(json.dumps({'phase':phase,'savedPhase':winch['shuttle_phase'],'distance':winch['shuttle_distance'],'beforeTick':before['tick'],'riders':len(before['riders']),'checkpoint':str(out)}))
