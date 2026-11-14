"""Regression checks for private-run refusal and failure reporting; no Minecraft launch."""
import argparse
import importlib.util
import json
from pathlib import Path
import struct
import tempfile
import unittest
from unittest.mock import MagicMock, call, patch

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
        args = argparse.Namespace(menu=False, action='use', values=[])
        with patch.object(p, 'window', return_value='42'), patch.object(p, 'options'), patch.object(p, 'state'), patch.object(client, 'rcon', return_value='There are 0 of a max of 20 players online: '), patch.object(p, 'snapshot') as snapshot, patch.object(p, '_input') as inject:
            with self.assertRaisesRegex(RuntimeError, 'not connected'):
                p.action(args)
            snapshot.assert_not_called()
            inject.assert_not_called()

    def test_named_use_sends_right_button_and_attack_sends_left_on_private_display(self):
        p = object.__new__(client.PrivateRun)
        p.display = ':252'
        p.env = {'DISPLAY': ':252'}
        for action, values, button in [('use', [], 3), ('attack', [], 1),
                                       ('click', ['3'], 3), ('click', ['1'], 1)]:
            with self.subTest(action=action, values=values):
                x, xtest = MagicMock(), MagicMock()
                x.XOpenDisplay.return_value = 99
                with patch.object(client.ctypes, 'CDLL', side_effect=[x, xtest]), patch.object(client.ctypes.util, 'find_library', return_value='mock'), patch.object(client.subprocess, 'run'), patch.object(client.time, 'sleep'):
                    p._input('42', argparse.Namespace(action=action, values=values), {})
                x.XOpenDisplay.assert_called_once_with(b':252')
                self.assertEqual(xtest.XTestFakeButtonEvent.call_args_list,
                                 [call(99, button, 1, 0), call(99, button, 0, 0)])
                x.XCloseDisplay.assert_called_once_with(99)

    def test_named_use_refuses_conflicting_button_before_runtime_access(self):
        p = object.__new__(client.PrivateRun)
        with patch.object(p, 'window') as window:
            with self.assertRaisesRegex(RuntimeError, 'takes no button number'):
                p.action(argparse.Namespace(action='use', values=['1']))
            window.assert_not_called()

    def test_hold_use_inspection_cancels_before_releasing_right_button(self):
        p = object.__new__(client.PrivateRun)
        p.display = ':252'
        p.env = {'DISPLAY': ':252'}
        x, xtest, events = MagicMock(), MagicMock(), []
        x.XOpenDisplay.return_value = 99
        xtest.XTestFakeButtonEvent.side_effect = lambda d, b, state, delay: events.append(('button', b, state))
        record = {'at': 'inspection'}
        with patch.object(client.ctypes, 'CDLL', side_effect=[x, xtest]), patch.object(client.ctypes.util, 'find_library', return_value='mock'), patch.object(client.subprocess, 'run', side_effect=lambda args, **kw: events.append(('command', args[-1]))), patch.object(client.time, 'sleep'), patch.object(p, 'snapshot', return_value='held.png'):
            p._input('42', argparse.Namespace(action='hold-use', values=[]), record)
        self.assertLess(events.index(('button', 3, 1)), events.index(('command', 'Escape')))
        self.assertLess(events.index(('command', 'Escape')), events.index(('button', 3, 0)))
        self.assertEqual(record['during'], 'held.png')
        self.assertIn('canceled', record['hold_result'])

    def test_hold_use_selection_moves_only_while_right_is_held(self):
        p = object.__new__(client.PrivateRun)
        p.display, p.env = ':252', {'DISPLAY': ':252'}
        x, xtest, events = MagicMock(), MagicMock(), []
        x.XOpenDisplay.return_value = 99
        xtest.XTestFakeButtonEvent.side_effect = lambda d, b, state, delay: events.append(('button', b, state))
        xtest.XTestFakeMotionEvent.side_effect = lambda d, screen, x, y, delay: events.append(('move', x, y))
        record = {'at': 'selection'}
        with patch.object(client.ctypes, 'CDLL', side_effect=[x, xtest]), patch.object(client.ctypes.util, 'find_library', return_value='mock'), patch.object(client.subprocess, 'run') as command, patch.object(client.time, 'sleep'), patch.object(p, 'snapshot', side_effect=['held.png', 'selected.png']):
            p._input('42', argparse.Namespace(action='hold-use', values=['200', '150']), record)
        self.assertEqual(events, [('button', 3, 1), ('move', 200, 150), ('button', 3, 0)])
        self.assertFalse(any('Escape' in c.args[0] for c in command.call_args_list))
        self.assertEqual(record['selection'], 'selected.png')

    def test_hold_use_screenshot_failure_cancels_and_releases(self):
        p = object.__new__(client.PrivateRun)
        p.display, p.env = ':252', {'DISPLAY': ':252'}
        x, xtest = MagicMock(), MagicMock()
        x.XOpenDisplay.return_value = 99
        with patch.object(client.ctypes, 'CDLL', side_effect=[x, xtest]), patch.object(client.ctypes.util, 'find_library', return_value='mock'), patch.object(client.subprocess, 'run') as command, patch.object(client.time, 'sleep'), patch.object(p, 'snapshot', side_effect=RuntimeError('capture failed')):
            with self.assertRaisesRegex(RuntimeError, 'capture failed'):
                p._input('42', argparse.Namespace(action='hold-use', values=['200', '150']), {'at': 'failure'})
        self.assertEqual(xtest.XTestFakeButtonEvent.call_args_list, [call(99, 3, 1, 0), call(99, 3, 0, 0)])
        self.assertTrue(any('Escape' in c.args[0] for c in command.call_args_list))
        x.XCloseDisplay.assert_called_once_with(99)

    def test_hold_use_rejects_partial_or_negative_selection(self):
        for values in [['100'], ['100', '-1'], ['one', 'two'], ['1', '2', '3']]:
            with self.subTest(values=values), self.assertRaisesRegex(RuntimeError, 'hold-use'):
                client.mouse_button('hold-use', values)

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
            self.assertEqual(records[-1]['mouse_binding'], 'use/right')
            self.assertEqual(command.call_args.args[0], ['xdotool','keyup','--window','42','Shift_L'])


if __name__ == '__main__':
    unittest.main()
