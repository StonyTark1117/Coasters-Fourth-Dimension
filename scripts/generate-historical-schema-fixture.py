#!/usr/bin/env python3
"""Regenerate the historic schema-3 NBT fixture from read-only 0.3 evidence."""
import base64
import gzip
import io
import json
import struct
from pathlib import Path

root_dir = Path(__file__).resolve().parents[1]
source = root_dir / "docs/evidence/0.3/integrated-schema3-before.json"
destination = root_dir / "src/main/resources/data/fourthdimension/fixtures/schema3-foundation.nbt.gz.b64"
with source.open(encoding="utf-8") as input_file:
    root = json.load(input_file)

BYTE, INT, LONG, DOUBLE, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = 1, 3, 4, 6, 8, 9, 10, 11, 12

def utf(s):
    b = s.encode('utf-8')
    return struct.pack('>H', len(b)) + b

def named(kind, name, payload):
    return bytes([kind]) + utf(name) + payload

def scalar(kind, value):
    if kind == BYTE: return struct.pack('>b', int(value))
    if kind == INT: return struct.pack('>i', int(value))
    if kind == LONG: return struct.pack('>q', int(value))
    if kind == DOUBLE: return struct.pack('>d', float(value))
    if kind == STRING: return utf(str(value))
    raise ValueError(kind)

def int_array(values):
    return struct.pack('>i', len(values)) + b''.join(struct.pack('>i', int(v)) for v in values)

def long_array(values):
    return struct.pack('>i', len(values)) + b''.join(struct.pack('>q', int(v)) for v in values)

def list_tag(kind, values, encode):
    return bytes([kind]) + struct.pack('>i', len(values)) + b''.join(encode(x) for x in values)

def compound(fields):
    return b''.join(named(kind, name, payload) for name, kind, payload in fields) + b'\0'

def profile(value):
    return compound([
        ('distance', DOUBLE, scalar(DOUBLE, value['distance'])),
        ('angle', DOUBLE, scalar(DOUBLE, value['angle'])),
        ('slope', DOUBLE, scalar(DOUBLE, value['slope'])),
    ])

def magnet(value):
    return compound([
        ("start", DOUBLE, scalar(DOUBLE, value["start"])),
        ("end", DOUBLE, scalar(DOUBLE, value["end"])),
        ("mode", STRING, scalar(STRING, value["mode"])),
        ("direction", INT, scalar(INT, value["direction"])),
        ("strength", DOUBLE, scalar(DOUBLE, value["strength"])),
        ("falloff", DOUBLE, scalar(DOUBLE, value["falloff"])),
        ("targets", INT, scalar(INT, value["targets"])),
    ])

def rail(value):
    return compound([
        ('from', LONG, scalar(LONG, value['from'])),
        ('to', LONG, scalar(LONG, value['to'])),
        ('family', STRING, scalar(STRING, value['family'])),
        ('profile', LIST, list_tag(COMPOUND, value['profile'], profile)),
        ('id', INT_ARRAY, int_array(value['id'])),
        ('variant', STRING, scalar(STRING, value['variant'])),
        ('provider', STRING, scalar(STRING, value['provider'])),
        ('geometry_hash', LONG, scalar(LONG, value['geometry_hash'])),
        ('revision', LONG, scalar(LONG, value['revision'])),
        ('valid', BYTE, scalar(BYTE, value['valid'])),
        ('magnets', LIST, list_tag(COMPOUND, value['magnets'], magnet)),
    ])

def rider_seat(value):
    return compound([
        ('player', INT_ARRAY, int_array(value['player'])),
        ('seat', LONG, scalar(LONG, value['seat'])),
    ])

def carrier(value):
    fields = [
        ('body', INT_ARRAY, int_array(value['body'])),
        ('side', DOUBLE, scalar(DOUBLE, value['side'])),
        ('longitudinal', DOUBLE, scalar(DOUBLE, value['longitudinal'])),
        ('ballast', DOUBLE, scalar(DOUBLE, value['ballast'])),
        ('angle', DOUBLE, scalar(DOUBLE, value['angle'])),
        ('omega', DOUBLE, scalar(DOUBLE, value['omega'])),
        ('capture', BYTE, scalar(BYTE, value['capture'])),
        ('pivot_height', DOUBLE, scalar(DOUBLE, value['pivot_height'])),
        ('seats', LONG_ARRAY, long_array(value['seats'])),
        ('occupants', LIST, list_tag(INT_ARRAY, value['occupants'], int_array)),
        ('rider_seats', LIST, list_tag(COMPOUND, value['rider_seats'], rider_seat)),
    ]
    if 'child_pivot_offset' in value:
        fields.insert(3, ('child_pivot_offset', DOUBLE, scalar(DOUBLE, value['child_pivot_offset'])))
    return compound(fields)

def vehicle(value):
    return compound([
        ('chassis', INT_ARRAY, int_array(value['chassis'])),
        ('family', STRING, scalar(STRING, value['family'])),
        ('restraints', BYTE, scalar(BYTE, value['restraints'])),
        ('stopping', BYTE, scalar(BYTE, value['stopping'])),
        ('maintenance', BYTE, scalar(BYTE, value['maintenance'])),
        ('fault', STRING, scalar(STRING, value['fault'])),
        ('revision', LONG, scalar(LONG, value['revision'])),
        ('boarding_ready', BYTE, scalar(BYTE, value['boarding_ready'])),
        ('damping', DOUBLE, scalar(DOUBLE, value['damping'])),
        ('ballast', DOUBLE, scalar(DOUBLE, value['ballast'])),
        ('variant', STRING, scalar(STRING, value['variant'])),
        ('boundFrom', LONG, scalar(LONG, value['boundFrom'])),
        ('boundTo', LONG, scalar(LONG, value['boundTo'])),
        ('carriers', LIST, list_tag(COMPOUND, value['carriers'], carrier)),
    ])

data = root['data']
payload = compound([
    ('schema', INT, scalar(INT, data['schema'])),
    ('rails', LIST, list_tag(COMPOUND, data['rails'], rail)),
    ('vehicles', LIST, list_tag(COMPOUND, data['vehicles'], vehicle)),
])
raw = bytes([COMPOUND]) + utf('') + named(COMPOUND, 'data', payload) + b'\0'
compressed = bytearray(gzip.compress(raw, mtime=0))
# Python versions differ in the gzip OS header byte when mtime=0. Keep the
# fixture stable across local generation and GitHub's Python runtime.
compressed[9] = 255
with destination.open("w", encoding="ascii") as output_file:
    output_file.write(base64.b64encode(compressed).decode("ascii") + "\n")
