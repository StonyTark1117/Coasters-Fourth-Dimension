from pathlib import Path
import sys,io,struct,gzip,zlib,json,uuid
source=Path('scripts/inspect-foundation-save.py').read_text()
exec(source[source.index('def number'):source.index('root = number')])
world=Path(sys.argv[1]);records=[]
def walk(entity,parent=None):
 if entity.get('id')=='minecraft:pig' and 'fd_additions_lifecycle' in entity.get('Tags',[]):
  records.append({'id':str(uuid.UUID(bytes=struct.pack('>4i',*entity['UUID']))),
   'health':entity.get('Health'),'position':entity.get('Pos'),
   'parent_type':parent.get('id') if parent else None,
   'parent_id':str(uuid.UUID(bytes=struct.pack('>4i',*parent['UUID']))) if parent else None})
 for child in entity.get('Passengers',[]):walk(child,entity)
for path in sorted((world/'entities').glob('r.*.*.mca')):
 blob=path.read_bytes();rx,rz=map(int,path.name.split('.')[1:3])
 for i in range(1024):
  entry=int.from_bytes(blob[i*4:i*4+4],'big');offset=entry>>8
  if not offset:continue
  n=int.from_bytes(blob[offset*4096:offset*4096+4],'big');kind=blob[offset*4096+4]
  data=blob[offset*4096+5:offset*4096+4+n]
  if kind&128:
   data=(path.parent/f'c.{rx*32+i%32}.{rz*32+i//32}.mcc').read_bytes();kind &=127
  raw={1:gzip.decompress,2:zlib.decompress,3:lambda x:x}[kind](data)
  stream=io.BytesIO(raw);tag=number('B');string();root=value(tag)
  for entity in root.get('Entities',[]):walk(entity)
assert len({r['id'] for r in records})==len(records),'Duplicate native pig UUIDs in saved entities'
json.dump(sorted(records,key=lambda r:r['id']),sys.stdout,indent=2);print()
