#!/usr/bin/env python3
"""Run a packaged, occupied Engineered cold restart and removal in disposable servers.

Build the JAR first. OUTPUT must not exist. Each phase is a new server JVM; the
runner stops it normally, retains the prepared save, and checks explicit test
markers rather than treating a successful Gradle exit as a passed GameTest.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import secrets
import shutil
import socket
import struct
import subprocess
import time
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('output', type=Path)
parser.add_argument('--profiles', default='engineered', choices=('engineered', 'styles,engineered'))
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
output = args.output.resolve()
output.mkdir(parents=True, exist_ok=False)
# Minecraft's synchronous region writes can be slow on the checkout filesystem.
# Use an isolated temporary runtime, retaining its path and prepared snapshot.
game = Path(tempfile.mkdtemp(prefix='fd-engineered-restart-'))
(output / 'game-directory.txt').write_text(str(game) + '\n')


def available_port():
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        return sock.getsockname()[1]


server_port, rcon_port = available_port(), available_port()
while rcon_port == server_port:
    rcon_port = available_port()
password = secrets.token_hex(16)
(game / 'eula.txt').write_text('eula=true\n')
(game / 'server.properties').write_text(
    f'online-mode=false\nserver-ip=127.0.0.1\nserver-port={server_port}\n'
    f'enable-rcon=true\nrcon.port={rcon_port}\nrcon.password={password}\n'
    'level-type=minecraft\\:flat\n'
    'generator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\n'
    'spawn-protection=0\nview-distance=4\nsimulation-distance=4\n'
    'max-tick-time=180000\nallow-flight=true\n'
)


def receive(sock, count):
    result = b''
    while len(result) < count:
        chunk = sock.recv(count - len(result))
        if not chunk:
            raise EOFError('RCON closed')
        result += chunk
    return result


def packet(sock, request_id, kind, text):
    data = struct.pack('<ii', request_id, kind) + text.encode() + b'\0\0'
    sock.sendall(struct.pack('<i', len(data)) + data)
    size = struct.unpack('<i', receive(sock, 4))[0]
    if not 10 <= size <= 1024 * 1024:
        raise ValueError('Invalid RCON packet length')
    data = receive(sock, size)
    return struct.unpack('<ii', data[:8]), data[8:-2].decode()


def command(text):
    with socket.create_connection(('127.0.0.1', rcon_port), timeout=15) as sock:
        header, _ = packet(sock, 1, 3, password)
        if header[0] != 1:
            raise RuntimeError('RCON authentication failed')
        return packet(sock, 2, 2, text)[1]


def world_hashes(world):
    return {str(p.relative_to(world)): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(world.rglob('*')) if p.is_file() and p.name != 'session.lock'}


results = []
try:
    for phase, marker in [('prepare', 'PREPARED'), ('restore', 'RESTORED'), ('restoreAbsent', 'REMOVED')]:
        if phase == 'restoreAbsent':
            shutil.move(game / 'world', output / 'restored-world')
            shutil.copytree(output / 'prepared-world', game / 'world')
            assert world_hashes(game / 'world') == world_hashes(output / 'prepared-world')
        profiles = args.profiles if phase != 'restoreAbsent' else args.profiles.replace('engineered', '').strip(',')
        cmd = ['./gradlew', '-Dorg.gradle.jvmargs=-Xmx1G', '--max-workers=2', '-PfdPackaged',
               f'-PfdIntegrations={profiles}', f'-PfdEngineeredRestart={phase}',
               f'-PfdRestartDirectory={game}', 'runEngineeredRestartServer']
        logfile = output / (phase + '.log')
        start = time.monotonic()
        sent = success = False
        with logfile.open('w') as log:
            process = subprocess.Popen(cmd, cwd=root, stdin=subprocess.DEVNULL,
                                       stdout=log, stderr=subprocess.STDOUT)
            (output / (phase + '.pid')).write_text(str(process.pid))
            try:
                while process.poll() is None:
                    text = logfile.read_text(errors='replace')
                    if not sent and 'RCON running on 127.0.0.1:' in text:
                        response = command('execute positioned 0 180 0 run test run occupiedprogramcoldrestart')
                        (output / (phase + '-command.txt')).write_text(response + '\n')
                        sent = True
                        if 'Running 1 tests' not in response:
                            raise RuntimeError(f'Test command did not start: {response}')
                    if f'FD_ENGINEERED_RESTART {marker} families=5' in text:
                        success = True
                        break
                    if 'failed at' in text or 'Exception in server tick loop' in text:
                        break
                    if time.monotonic() - start > 360:
                        raise TimeoutError(f'{phase} exceeded six minutes')
                    time.sleep(1)
            finally:
                if process.poll() is None:
                    command('stop')
                code = process.wait(timeout=600)
        results.append({'phase': phase, 'profiles': profiles, 'command': cmd, 'exit': code,
                        'success': success, 'seconds': time.monotonic() - start})
        (output / 'commands.json').write_text(json.dumps(results, indent=2) + '\n')
        if not success or code != 0:
            raise RuntimeError(f'{phase} failed; inspect {logfile}')
        if phase == 'prepare':
            shutil.copytree(game / 'world', output / 'prepared-world')
            shutil.copy2(game / 'engineered-restart-fixture.json', output / 'fixture.json')
            (output / 'prepared-world-sha256.json').write_text(json.dumps(world_hashes(output / 'prepared-world'), indent=2) + '\n')
    (output / 'exit').write_text('0\n')
    print('PASS prepare, cold restart, and addon removal; all five families retained and unloaded')
except BaseException:
    (output / 'exit').write_text('1\n')
    raise
