#!/usr/bin/env python3
"""Snapshot a completed native launch before any Gradle invocation can clean its outputs."""
from pathlib import Path
import argparse,shutil,json,hashlib,re
p=argparse.ArgumentParser();p.add_argument('build',type=Path);p.add_argument('run',type=Path);a=p.parse_args()
assert a.build.is_absolute() and a.run.is_absolute()
a.run.mkdir(parents=True,exist_ok=False)
for part in ['classes/java/main','resources/main']:
 src=a.build/part;assert src.is_dir(),src;shutil.copytree(src,a.run/part)
(a.run/'args').mkdir();(a.run/'game').mkdir()
replacements={str(a.build/'classes/java/main'):str(a.run/'classes/java/main'),str(a.build/'resources/main'):str(a.run/'resources/main'),str(a.build/'runs/gametest'):str(a.run/'game')}
args=list((a.build/'moddev').glob('gameTestServer*.txt'))
for old in args:
 dst=a.run/'args'/old.name;replacements[str(old)]=str(dst)
# Pin all build-local dependency artifacts/configuration, not only production classes.
for old in args:
 for value in re.findall(re.escape(str(a.build))+r"/[^\s:'\"]+",old.read_text()):
  if value in replacements:continue
  src=Path(value)
  if src.is_file():
   dst=a.run/'dependencies'/src.relative_to(a.build);dst.parent.mkdir(parents=True,exist_ok=True)
   shutil.copy2(src,dst);replacements[value]=str(dst)
for old in args:
 text=old.read_text()
 for x,y in replacements.items():text=text.replace(x,y)
 assert str(a.build) not in text,(old,text)
 (a.run/'args'/old.name).write_text(text)
script=(a.build/'moddev/runGameTestServer.sh').read_text()
for x,y in replacements.items():script=script.replace(x,y)
assert str(a.build) not in script
(a.run/'launch.sh').write_text('#!/bin/bash\nset -e\n'+script+'\n')
(a.run/'launch.sh').chmod(0o755)
files={str(q.relative_to(a.run)):hashlib.sha256(q.read_bytes()).hexdigest() for part in ['classes','resources','args','dependencies'] for q in (a.run/part).rglob('*') if q.is_file()}
(a.run/'snapshot.json').write_text(json.dumps({'source_build':str(a.build),'files':files},indent=2)+'\n');print(a.run/'launch.sh',len(files))
