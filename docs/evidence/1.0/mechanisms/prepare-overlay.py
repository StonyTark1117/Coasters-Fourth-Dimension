"""Build a test-only JAR overlay without changing any frozen production entry."""
from pathlib import Path
import hashlib,json,shutil,zipfile
ROOT=Path('/mnt/NVME4/fourthdimension-runs/fd10-mechanisms-20261115')
OLD=Path('/mnt/NVME4/fourthdimension-runs/fd917-mechanisms-20261115')
BASE=OLD/'indicator-final/coasters-fourth-dimension-0.9.0-dev.27.jar'
LAUNCH=ROOT/'launch01'
assert hashlib.sha256(BASE.read_bytes()).hexdigest()=='9c14b760e71c69295d697ccd2ef317144d817d96f944094ebb618382af2b406f'
shutil.copytree(OLD/'launch05',LAUNCH)
for p in (LAUNCH/'moddev').iterdir():
    if p.is_file():
        try:s=p.read_text()
        except UnicodeDecodeError:continue
        p.write_text(s.replace(str(OLD/'launch05'),str(LAUNCH)).replace(str(OLD),str(ROOT)))
jar=LAUNCH/'libs/coasters-fourth-dimension-0.9.0-dev.27.jar'
classes=ROOT/'build/classes/java/main'
extra=list(classes.glob('dev/fourthdimension/gametest/Fd902LifecycleFixture*.class'))
assert extra
with zipfile.ZipFile(BASE) as source,zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as target:
    for info in source.infolist():target.writestr(info,source.read(info.filename))
    for p in extra:target.write(p,p.relative_to(classes).as_posix())
with zipfile.ZipFile(BASE) as a,zipfile.ZipFile(jar) as b:
    assert all(a.read(n)==b.read(n) for n in a.namelist())
    added=sorted(set(b.namelist())-set(a.namelist()))
manifest={'frozen_jar':str(BASE),'frozen_sha256':hashlib.sha256(BASE.read_bytes()).hexdigest(),'overlay_jar':str(jar),'overlay_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'production_entries_byte_identical':True,'added_entries':added}
(ROOT/'overlay.json').write_text(json.dumps(manifest,indent=2)+'\n')
for name in ['server','client','evidence']:(ROOT/name).mkdir(exist_ok=True)
p=ROOT/'server/server.properties';props=(OLD/'server/server.properties').read_text().splitlines();overrides={'level-name':'PaxLifecycle','server-port':'25656','rcon.port':'25657','view-distance':'5','simulation-distance':'5','max-tick-time':'180000','sync-chunk-writes':'false'}
p.write_text('\n'.join(k+'='+overrides.get(k,v) if '=' in line and not line.startswith('#') else line for line in props for k,_,v in [line.partition('=')])+'\n')
(ROOT/'server/eula.txt').write_text('eula=true\n')
run={'owner':'01a1dd1d-c6a6-7be0-9cb5-2b033ac550da','root':str(ROOT),'display':319,'unit_prefix':'fd10-mechanisms-20261115','player':'Dev','client_script':str(LAUNCH/'moddev/runClient.sh'),'server_script':str(LAUNCH/'moddev/runServer.sh'),'client_limit':'6G','server_limit':'4G'}
(ROOT/'run.json').write_text(json.dumps(run,indent=2)+'\n')
print(json.dumps(manifest,indent=2))
