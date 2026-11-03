from pathlib import Path
import datetime, hashlib, json, os, shutil, subprocess, sys

out = Path(__file__).resolve().parent
metadata = json.loads((out / 'preflight.json').read_text())
head = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
assert head == metadata['source_revision'], 'Source changed since preflight'
assert not subprocess.check_output(['git', 'status', '--porcelain'], text=True).strip(), 'Dirty source'
assert not (out / 'gradle.txt').exists(), 'Refusing to overwrite a previous run'
jar = Path(metadata['jar']['path'])
assert hashlib.sha256(jar.read_bytes()).hexdigest() == metadata['jar']['sha256'], 'JAR changed'
now = lambda: datetime.datetime.now(datetime.timezone.utc).isoformat()
# The outer flock serializes cooperating owners; also reject an already-live non-daemon Java process, including custom game run directories.
live_games = []
for proc in Path('/proc').iterdir():
    if not proc.name.isdigit():
        continue
    try:
        if Path(os.readlink(proc / 'exe')).name != 'java':
            continue
        cwd = os.readlink(proc / 'cwd')
        args = (proc / 'cmdline').read_bytes()
        if b'GradleDaemon' not in args:
            live_games.append({'pid': int(proc.name), 'cwd': cwd})
    except OSError:
        continue
if live_games:
    print('Refusing runtime overlap with existing game JVMs:', live_games, flush=True)
    sys.exit(75)
metadata.update(source=head, profile='engineered', start_utc=now(), status='running shared availability guard replay', prelaunch_game_jvms=live_games)
meta_path = out / 'metadata.json'
meta_path.write_text(json.dumps(metadata, indent=2) + '\n')
with (out / 'gradle.txt').open('w') as log:
    process = subprocess.Popen(metadata['planned_command'], stdout=log, stderr=subprocess.STDOUT)
    metadata['gradle_pid'] = process.pid
    meta_path.write_text(json.dumps(metadata, indent=2) + '\n')
    print('Started shared availability guard replay, Gradle PID', process.pid, flush=True)
    exit_code = process.wait()
metadata.update(end_utc=now(), exit_code=exit_code, status='completed; result requires review', source_status_after=subprocess.check_output(['git', 'status', '--porcelain'], text=True).splitlines())
meta_path.write_text(json.dumps(metadata, indent=2) + '\n')
(out / 'exit-code.txt').write_text(str(exit_code) + '\n')
shutil.copy2(jar, out / 'baseline.jar')
run_dir = Path('/tmp/fourthdimension-2aa0362f/build/runs/gametest')
shutil.copy2(run_dir / 'logs/latest.log', out / 'server.txt')
with (out / 'capture.txt').open('w') as log:
    capture = subprocess.run([sys.executable, 'scripts/capture-native-exports.py', '--run-dir', str(run_dir), '--metadata', str(meta_path), '--output', str(out / 'native-exports')], stdout=log, stderr=subprocess.STDOUT)
print('Gradle exit', exit_code, 'export capture exit', capture.returncode, flush=True)
sys.exit(exit_code)
