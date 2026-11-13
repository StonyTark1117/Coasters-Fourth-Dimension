"""Regression checks for private-run refusal and failure reporting; no Minecraft launch."""
import argparse
import importlib.util
import json
from pathlib import Path
import struct
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location('private_client', Path(__file__).parents[1] / 'fd-private-client.py')
client = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(client)


def packet(request, kind, text):
    payload = struct.pack('<ii', request, kind) + text.encode() + b'\0\0'
    return struct.pack('<i', len(payload)) + payload


class FragmentedSocket:
    def __init__(self, payload):
        self.payload = payload
        self.sent = []
    def __enter__(self): return self
    def __exit__(self, *args): pass
    def recv(self, size):
        chunk, self.payload = self.payload[:min(size, 3)], self.payload[min(size, 3):]
        return chunk
    def sendall(self, payload): self.sent.append(payload)


class PrivateClientTests(unittest.TestCase):
    def test_multipart_rcon_survives_fragmented_tcp_and_auth_preamble(self):
        sock = FragmentedSocket(packet(1, 0, '') + packet(1, 2, '') +
                                packet(2, 0, 'first ') + packet(2, 0, 'second') + packet(3, 0, ''))
        with patch.object(client, 'properties', return_value={'server-ip':'127.0.0.1', 'rcon.port':'25603', 'rcon.password':'test'}), patch.object(client.socket, 'create_connection', return_value=sock):
            self.assertEqual(client.rcon({'root':'/unused'}, 'list'), 'first second')
        self.assertEqual([struct.unpack('<i', p[4:8])[0] for p in sock.sent], [1, 2, 3])

    def test_rcon_disconnect_is_uncertain_not_empty_success(self):
        with self.assertRaisesRegex(RuntimeError, 'disconnected'):
            client.receive(FragmentedSocket(b'xx'), 4)

    def test_cgroup_prefix_does_not_adopt_another_unit(self):
        self.assertTrue(client.owns_group('0::/task/client.service', '/task/client.service'))
        self.assertTrue(client.owns_group('0::/task/client.service/child', '/task/client.service'))
        self.assertFalse(client.owns_group('0::/task/client.service-other', '/task/client.service'))
        self.assertFalse(client.owns_group('0::/task/client.service', ''))

    def test_owner_mismatch_refuses_before_runtime_access(self):
        with tempfile.TemporaryDirectory() as root:
            path = Path(root) / 'run.json'
            path.write_text(json.dumps({'owner':'another-task'}))
            with self.assertRaisesRegex(RuntimeError, 'Owner does not match'):
                client.PrivateRun(path, 'this-task')

    def test_disconnected_player_prevents_input_and_screenshots(self):
        p = object.__new__(client.PrivateRun)
        p.c = {'player':'Dev'}
        args = argparse.Namespace(menu=False)
        with patch.object(p, 'window', return_value='42'), patch.object(p, 'options'), patch.object(p, 'state'), patch.object(client, 'rcon', return_value='There are 0 of a max of 20 players online: '), patch.object(p, 'snapshot') as snapshot, patch.object(p, '_input') as inject:
            with self.assertRaisesRegex(RuntimeError, 'not connected'):
                p.action(args)
            snapshot.assert_not_called()
            inject.assert_not_called()

    def test_failed_input_records_uncertainty_and_releases_modifier(self):
        with tempfile.TemporaryDirectory() as root:
            p = object.__new__(client.PrivateRun)
            p.evidence = Path(root)
            p.env = {'DISPLAY':':252'}
            args = argparse.Namespace(menu=False, action='click', values=['3'], expect='Named state change', modifier='Shift_L')
            with patch.object(p, 'window', return_value='42'), patch.object(p, 'options'), patch.object(p, 'connected', return_value='Dev'), patch.object(p, 'snapshot', return_value='before.png'), patch.object(client, 'run', return_value='geometry'), patch.object(client.subprocess, 'run') as command, patch.object(client.time, 'sleep'), patch.object(p, '_input', side_effect=RuntimeError('input failed')):
                with self.assertRaisesRegex(RuntimeError, 'input failed'):
                    p.action(args)
            records = [json.loads(line) for line in (p.evidence/'input.jsonl').read_text().splitlines()]
            self.assertEqual([r['state'] for r in records], ['sending', 'uncertain'])
            self.assertEqual(records[-1]['error'], 'input failed')
            self.assertEqual(command.call_args.args[0], ['xdotool','keyup','--window','42','Shift_L'])


if __name__ == '__main__':
    unittest.main()
