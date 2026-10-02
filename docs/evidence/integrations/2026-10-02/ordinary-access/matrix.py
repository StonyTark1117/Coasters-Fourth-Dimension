from pathlib import Path
import subprocess, time, os, json, shutil
root = Path(__file__).resolve().parents[2]
out = Path(__file__).resolve().parent
build = Path('/tmp/fourthdimension-48535694/build')
try:
    profiles = [('base', None), ('engineered', 'engineered'), ('styles', 'styles'),
                ('additions', 'additions'), ('magnetized', 'magnetized'), ('immersion', 'immersion'),
                ('combined', 'engineered,styles,additions,magnetized,immersion')]
    for name, profile in profiles:
        cmd = ['./gradlew', '-Dorg.gradle.jvmargs=-Xmx1G', '--max-workers=2']
        if name == 'base': cmd.append('build')
        cmd.append('runGameTestServer')
        if profile: cmd.append('-PfdIntegrations=' + profile)
        if name in ('engineered', 'combined'): cmd.append('-PfdPaxEnergyReload')
        with (out / (name + '.log')).open('w') as log:
            p = subprocess.Popen(cmd, cwd=root, stdin=subprocess.DEVNULL, stdout=log,
                stderr=subprocess.STDOUT, env={**os.environ, 'JAVA_HOME': '/usr/lib/jvm/java-21-openjdk', 'JAVA_TOOL_OPTIONS': '-Xmx2G'})
            status = {'status': 'running', 'profile': name, 'pid': p.pid, 'command': cmd}
            (out / 'matrix-status.json').write_text(json.dumps(status) + '\n')
            code = p.wait()
        (out / (name + '.exit')).write_text(str(code) + '\n')
        status.update(status='complete', exit_status=code)
        (out / (name + '-run.json')).write_text(json.dumps(status, indent=2) + '\n')
        assert code == 0, 'Native profile failed: ' + name
        text = (out / (name + '.log')).read_text()
        assert 'All 37 required tests passed' in text
        assert 'FD_ORDINARY_MATRIX families=5 appearances=4 accepted=40 rejected=100 traversals=10 registered4D=0' in text
        if name == 'base':
            shutil.copyfile(build / 'libs/coasters-fourth-dimension-0.9.0-dev.1.jar', out / 'verified.jar')
            shutil.copytree(build / 'test-results/test', out / 'unit', dirs_exist_ok=True)
    (out / 'matrix-status.json').write_text(json.dumps({'status': 'complete'}) + '\n')
    (out / 'matrix.exit').write_text('0\n')
except BaseException as e:
    (out / 'matrix-error.txt').write_text(repr(e) + '\n')
    (out / 'matrix.exit').write_text('1\n')
    raise
