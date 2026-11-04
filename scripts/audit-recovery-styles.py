#!/usr/bin/env python3
"""Prospective default Styles geometry at recorded recovery poses; no rendering claim.

Requires new same-route exports and their matching one-tick body trace. A style
plan proposes a visual configuration; it does not prove the client selected or
rendered it. Models stay in memory and only aggregate results are emitted.
"""
import argparse
import copy
import gzip
import hashlib
import importlib.util
import json
import math
import sys
from pathlib import Path
import zipfile

import numpy as np

ROOT = Path(__file__).resolve().parents[1]


def module(name, filename):
    spec = importlib.util.spec_from_file_location(name, ROOT / 'scripts' / filename)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


recovery = module('recovery_mesh', 'audit-recovery-mesh-trace.py')
inputs = module('decoration_inputs', 'verify-recovery-decoration-inputs.py')
route_geometry = module('route_geometry', 'verify-recovery-bank-frames.py')
STYLES_PIN = '42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38'
STYLES = ('arrow_4d', 'arrow_4d_dbl_spine')
# Filled from the inspected implementation; refuse silent renderer/export changes.
SOURCE_PINS = {
    "scripts/verify-recovery-bank-frames.py": "80749110c47c7794d7a8bd6bc729bf51fa4f634b0138e7ed0d0412c312cfc2af",
    "src/main/java/dev/fourthdimension/vehicle/BankedRecoveryExamples.java": "662dd6d3382fad445ced3fe12a24725c517fc8b907720890222b8ff02e968a41",
    "src/main/java/dev/fourthdimension/gametest/RecoveryRouteFrames.java": "ed3d7dbe58b6759b8a511f49796d1bb30bbdfb43ff4439d41f0fc6dc8a9896ce",
    "src/main/java/dev/fourthdimension/client/integration/TrackStylesRenderer.java": "fa69593b5a8b9f93ece00f40ebd67f00692db9d8af8d18648563f42146edb217",
    "src/main/java/dev/fourthdimension/client/integration/TrackStylesCatalog.java": "2b124893d6477a9b4be453a8a34f93a42b7b3f0b04fc926619d99c5d6ec16dbc",
    "src/main/java/dev/fourthdimension/physics/ArrowDecorationPlacement.java": "d13de25c3a03ce24343d92133457332a4887e0eabac54348a99f11ae37b193cd",
    "src/main/java/dev/fourthdimension/physics/ModelFrame.java": "b2d95f95df0630e7780b84c6a901a517b3adb7e7de2680072bba0ac828ae37b6",
    "src/main/java/dev/fourthdimension/physics/RotationProfile.java": "041667e8a450b154e1bfe769f2879299a768ae5f7beeb3db1ec00499090f9b54",
    "src/main/java/dev/fourthdimension/track/TrackGeometry.java": "a3e89095a217d3e978aa414704dea7212d36020bd1c0f9865387ac8a62643012",
    "src/main/java/dev/fourthdimension/contract/Family.java": "a441e66dfcbee53a83a52f5b475fab07d60e58e03d2ef789ebfbe1c0ab9d7c2c",
    "src/main/java/dev/fourthdimension/integration/TrackAppearance.java": "cf12254fb753172bc1c110dd8f5b1203de740b02658f81612427a86cd2695a3c",
    "src/main/java/dev/fourthdimension/integration/TrackRenderOwnership.java": "8d7b795ccf9e1a54b1c7ffe375cf8a94d6d9f10c72c344f9bce94480c4c9887e",
    "scripts/audit-recovery-mesh-trace.py": "a5df9b8ad3c5cb87812b4044c4d216148cb7d5d1bce5830bb3d8a37fba88dc5a",
    "scripts/verify-recovery-decoration-inputs.py": "c6c443d58a411d98881109633becb55595ea16cd21df2e2b853be2e08353d594"
}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def raw_obj(source):
    """Pinned OBJ positions without placement offsets (the exported matrix owns them)."""
    vertices, triangles = [], []
    for line in source.splitlines():
        parts = line.split()
        if not parts or parts[0].startswith('#'):
            continue
        if parts[0] == 'v':
            require(len(parts) == 4, 'unsupported OBJ vertex')
            point = tuple(map(float, parts[1:]))
            require(all(math.isfinite(v) for v in point), 'nonfinite OBJ vertex')
            vertices.append(point)
        elif parts[0] == 'f':
            require(len(parts) >= 4, 'degenerate OBJ face')
            face = []
            for entry in parts[1:]:
                index = int(entry.split('/')[0])
                require(index != 0 and -len(vertices) <= index <= len(vertices), 'invalid OBJ index')
                face.append(vertices[index-1 if index > 0 else index])
            triangles.extend((face[0], face[i], face[i+1]) for i in range(1, len(face)-1))
    require(triangles, 'empty OBJ geometry')
    return np.array(triangles, dtype=float)


def models(archive, used_styles):
    result, hashes = {}, {}
    for style in sorted(used_styles):
        require(style in STYLES, 'unsupported style')
        for kind, component in (('spines', 'center_beam'), ('ties', 'tie')):
            stem = f'assets/coasterssimulatedextratypes/models/block/track/{style}/{style}_{component}'
            metadata_bytes = archive.read(stem + '.json')
            metadata = json.loads(metadata_bytes)
            require(metadata.get('loader') == 'neoforge:obj'
                    and metadata.get('model') == stem.replace('assets/coasterssimulatedextratypes/', 'coasterssimulatedextratypes:') + '.obj'
                    and not any(key in metadata for key in ('transform', 'root_transform', 'display')),
                    'unexpected OBJ loader/model transform')
            payload = archive.read(stem + '.obj')
            result[style, kind] = raw_obj(payload.decode())
            hashes[stem + '.json'] = hashlib.sha256(metadata_bytes).hexdigest()
            hashes[stem + '.obj'] = hashlib.sha256(payload).hexdigest()
    return result, hashes


def world_triangles(triangles, piece):
    transform = np.array(piece['modelTransform']).reshape((4, 4), order='F')
    require(np.isfinite(transform).all() and np.allclose(transform[3], [0, 0, 0, 1]), 'invalid affine transform')
    return triangles @ transform[:3, :3].T + transform[:3, 3]


def surface(route, style_plan, model_triangles):
    """Reconstruct procedural ownership and provider pieces using renderer-oriented inputs."""
    require(route['family'] == 'ARROW', 'Styles geometry requires controlled Arrow')
    ids = {section['rail'] for section in route['sections']}
    require(set(style_plan) == ids, 'style plan must name every rail exactly once')
    require(all(style in (*STYLES, 'original') for style in style_plan.values()), 'unsupported style plan')
    triangles, labels = [], []
    counts = {'originalSections': 0, 'styledSections': 0, 'spines': 0, 'ties': 0,
              'crossbraces': 0, 'endpointBridges': 0, 'loweredControlMounts': 0}
    vertical = inputs.constants()[1]
    def add(label, a, b, ar, au, br, bu, radius):
        values = recovery.tube(a, b, ar, au, br, bu, radius, radius)
        triangles.extend(values)
        labels.extend([label]*len(values))
    for index, section in enumerate(route['sections']):
        style = style_plan[section['rail']]
        styled = style != 'original'
        if styled:
            require(all(key['angle'] <= 4*math.pi for key in section['profile']),
                    'requested style exceeds supported profile; explicitly plan Original fallback')
        counts['styledSections' if styled else 'originalSections'] += 1
        # Reuse Original track reconstruction with its renderer-oriented profile.
        oriented = copy.deepcopy(section)
        for frame in oriented['frames']:
            frame['profileAngle'] = frame['renderProfileAngle']
        original, original_labels = recovery.track_mesh({**route, 'sections': [oriented]})
        for triangle, label in zip(original, original_labels):
            if styled and (label.endswith(':rail4') or ':controlMount' in label):
                continue
            triangles.append(triangle)
            labels.append(label.replace('section0:', f'section{index}:', 1))
        if not styled:
            continue
        for i, frame in enumerate(section['frames']):
            if i % 8 != 0 and i != len(section['frames'])-1:
                continue
            c, r, u, f = (np.array(frame[key]) for key in ('center', 'right', 'up', 'forward'))
            underbrace = c + recovery.constant('RUNNING_UNDERBRACE_HEIGHT')*u
            outboard = recovery.constant('RUNNING_OUTBOARD_HALF_GAUGE')
            spine = c + (recovery.constant('SPINE_HEIGHT') + vertical)*u
            add(f'section{index}:styledCrossbrace', underbrace-outboard*r, underbrace+outboard*r,
                f, u, f, u, recovery.constant('RUNNING_CROSSBAR_RADIUS'))
            counts['crossbraces'] += 1
            if i == 0 or i == len(section['frames'])-1:
                add(f'section{index}:appearanceEndBridge', c+recovery.constant('SPINE_HEIGHT')*u,
                    spine, f, r, f, r, recovery.constant('RUNNING_MOUNT_RADIUS'))
                counts['endpointBridges'] += 1
            control = recovery.constant('CONTROL_BASE_HEIGHT') + frame['renderProfileAngle']*.5/(8*math.pi)
            for side in (-1, 1):
                add(f'section{index}:controlMount{side}', c+side*recovery.constant('CONTROL_HALF_GAUGE')*r+control*u,
                    spine, f, r, f, r, recovery.constant('CONTROL_SUPPORT_RADIUS'))
                counts['loweredControlMounts'] += 1
        for kind in ('spines', 'ties'):
            for piece_index, piece in enumerate(section['decorationInputs'][kind]):
                transformed = world_triangles(model_triangles[style, kind], piece)
                triangles.extend(transformed)
                labels.extend([f'section{index}:{style}:{kind}:{piece_index}']*len(transformed))
                counts[kind] += 1
    require(counts['styledSections'] > 0, 'no proposed Styles section')
    return (np.array(triangles), labels), counts


def read_json(path):
    payload = path.read_bytes()
    return json.loads(gzip.decompress(payload) if path.suffix == '.gz' else payload)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('base_jar', 'minecraft_jar', 'styles_jar', 'frames', 'trace'):
        parser.add_argument(name, type=Path)
    choice = parser.add_mutually_exclusive_group(required=True)
    choice.add_argument('--style', choices=STYLES, help='Propose this uniform default style on Arrow sections')
    choice.add_argument('--style-plan', type=Path, help='JSON map of Arrow rail UUIDs to original or supported style')
    parser.add_argument('--margin', type=float, default=.02)
    args = parser.parse_args()
    try:
        source_pins = {**recovery.SOURCE_PINS, **SOURCE_PINS}
        provenance_paths = {name: getattr(args, name) for name in
                            ('base_jar', 'minecraft_jar', 'styles_jar', 'frames', 'trace')}
        if args.style_plan is not None:
            provenance_paths['style_plan'] = args.style_plan
        provenance_paths['tool'] = Path(__file__)
        provenance_paths.update({'source:' + name: ROOT/name for name in source_pins})
        initial_hashes = recovery.provenance_snapshot(provenance_paths)
        require(math.isfinite(args.margin) and 0 < args.margin <= .05, 'invalid margin')
        for name, pin in (('base_jar', recovery.BASE_PIN), ('minecraft_jar', recovery.MINECRAFT_PIN),
                          ('styles_jar', STYLES_PIN)):
            require(initial_hashes[name] == pin, f'wrong inspected dependency: {name}')
        for name, pin in source_pins.items():
            require(initial_hashes['source:' + name] == pin, 'reinspect source before auditing: '+name)
        require(sys.flags.optimize == 0, 'run without Python -O; route verifier uses assertions')
        require(args.frames.suffix != '.gz', 'use an uncompressed archived frames copy')
        routes = read_json(args.frames)
        inputs.verify(routes)
        # Require raw trace for streaming identity checks and preserve its exact hash.
        require(args.trace.suffix != '.gz', 'use an uncompressed archived trace copy')
        pairing = inputs.verify_trace(routes, args.trace)
        arrows = [route for route in routes if route['family'] == 'ARROW']
        require(len(arrows) == 1, 'expected exactly one Arrow route')
        route = arrows[0]
        native_geometry = route_geometry.verify(args.frames, train_routes=route['carCount'] > 1)
        plan = ({section['rail']: args.style for section in route['sections']} if args.style
                else read_json(args.style_plan))
        require(isinstance(plan, dict), 'style plan must be an object')
        used = set(plan.values()) - {'original'}
        with zipfile.ZipFile(args.styles_jar) as archive:
            geometry, model_hashes = models(archive, used)
        track, counts = surface(route, plan, geometry)
        with args.trace.open() as stream:
            entries = [json.loads(line) for line in stream]
        with zipfile.ZipFile(args.base_jar) as base, zipfile.ZipFile(args.minecraft_jar) as minecraft:
            resources = recovery.Resources(base, minecraft)
            result = recovery.audit(route, entries, resources, args.margin, surface=track)
        recovery.require_unchanged_provenance(provenance_paths, initial_hashes)
        output = {
            'scope': 'Prospective default-model Styles configuration at recorded recovery tick poses, including a conservative vanilla seated-player skin envelope at every fixture seat; not observed client rendering, track-to-decoration self-clearance, continuous sweep, armor/held items/capes/elytra/modded cosmetics, arbitrary resources or full-turn ride acceptance.',
            'savedAppearance': {section['rail']: section['appearance'] for section in route['sections']},
            'prospectiveStylePlan': plan, 'geometryCounts': counts, 'result': result,
            'tracePairing': pairing, 'nativeRouteGeometry': native_geometry, 'numpyVersion': np.__version__,
            'inputHashes': {name: initial_hashes[name] for name in
                            ('base_jar', 'minecraft_jar', 'styles_jar', 'frames', 'trace')},
            'stylePlanSha256': initial_hashes.get('style_plan'),
            'sourceHashes': {name: initial_hashes['source:' + name] for name in source_pins},
            'provenanceCheck': 'Input/source/tool hashes matched before analysis and before report emission; use immutable files to exclude intervening changes.',
            'toolSha256': initial_hashes['tool'], 'providerResourceHashes': model_hashes,
            'bodyResourceHashes': resources.hashes,
        }
        print(json.dumps(output, indent=2))
        if not result['passes']:
            parser.exit(1, 'Prospective sampled geometry has uncertified pairs; no clearance pass.\n')
    except (OSError, ValueError, KeyError, TypeError, IndexError, AssertionError, zipfile.BadZipFile) as error:
        parser.exit(1, f'FAIL: {error}\n')


if __name__ == '__main__':
    main()
