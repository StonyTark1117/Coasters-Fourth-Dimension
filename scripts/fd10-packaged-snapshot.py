#!/usr/bin/env python3
"""Pin completed packaged client/server launchers and every build-local dependency."""
import argparse, hashlib, json, re, shutil
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('build',type=Path);p.add_argument('target',type=Path);a=p.parse_args()
assert a.build.is_absolute() and a.target.is_absolute()
a.target.mkdir(parents=True,exist_ok=False)
pending=[a.build/'moddev'/name for name in ['runClient.sh','runServer.sh','runGameTestServer.sh'] if (a.build/'moddev'/name).exists()]
pending += [q for q in (a.build/'moddev').glob('*.txt') if q.name.startswith(('client','server','gameTestServer'))]
copied=set()
while pending:
 src=pending.pop()
 if src in copied:continue
 assert src.is_file(),src
 dst=a.target/src.relative_to(a.build);dst.parent.mkdir(parents=True,exist_ok=True)
 if src.suffix in ['.txt','.xml','.sh']:
  content=src.read_text()
  for ref in re.findall(re.escape(str(a.build))+r"/[^\s:'\";)]+",content):
   path=Path(ref)
   if path.is_file():pending.append(path)
   elif path.is_dir():shutil.copytree(path,a.target/path.relative_to(a.build),dirs_exist_ok=True)
   elif path.relative_to(a.build).as_posix() in ('classes/java/packaged','resources/packaged','runs/gametest'):
    # The intentionally empty ModDev packaged source set has no compiler output.
    (a.target/path.relative_to(a.build)).mkdir(parents=True,exist_ok=True)
   else:raise RuntimeError('Unresolved launch reference '+ref)
  content=content.replace(str(a.build),str(a.target))
  dst.write_text(content)
 else:shutil.copy2(src,dst)
 copied.add(src)
files={str(q.relative_to(a.target)):hashlib.sha256(q.read_bytes()).hexdigest() for q in a.target.rglob('*') if q.is_file()}
(a.target/'snapshot.json').write_text(json.dumps({'build':str(a.build),'files':files},indent=2)+'\n')
print(json.dumps({'target':str(a.target),'files':len(files)}))
