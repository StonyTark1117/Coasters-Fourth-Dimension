"""Offline acceptance/negative controls; synthetic frames are not runtime evidence."""
import copy
import importlib.util
import math
import json
import tempfile
from pathlib import Path
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / 'verify-recovery-decoration-inputs.py'
spec = importlib.util.spec_from_file_location('decoration_inputs', SCRIPT)
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


def fixture(length=3.3):
    """Analytic flat +Z track at y=90, with known model origins and scales."""
    n = math.ceil(length*2)
    step = length/n
    def piece(d, spine):
        scale = [1, 1, audit.f32(step)] if spine else [audit.f32(.85), 1, 1]
        x = 0 if spine else -.5*scale[0]
        z = d if spine else d-.25
        return dict(distance=d, parameter=d/length, center=[0, 90, d], right=[-1, 0, 0],
                    up=[0, 1, 0], forward=[0, 0, 1], profileAngle=0, renderProfileAngle=0, scale=scale,
                    modelTransform=[scale[0], 0, 0, 0, 0, 1, 0, 0, 0, 0, scale[2], 0,
                                    audit.f32(x), audit.f32(89.4), audit.f32(z), 1])
    spines = [piece((i+.5)*step, True) for i in range(n)]
    ties = [piece(min(d, length), False) for d in range(0, math.floor((length+1e-6)/2)*2+1, 2)]
    return [dict(family='ARROW', variant='arrow_four_rail_v1', carCount=1, chassis=['fixture-chassis'],
                 dimension='minecraft:overworld', gameTime=120, sections=[dict(
        rail='fixture-rail', family='arrow_controlled', variant='arrow_four_rail_v1', length=length,
        geometryHash='9223372036854775807', revision=0, valid=True,
        appearance={'id':'fourthdimension:original', 'tint':-1},
        profile=[dict(distance=0, angle=0, slope=0), dict(distance=length, angle=0, slope=0)],
        renderProfile=[dict(distance=0, angle=0, slope=0), dict(distance=length, angle=0, slope=0)],
        canonical=True, railFrom=[0, 90, 0], railTo=[0, 90, 4], edgeFrom=[0, 90, 0], edgeTo=[0, 90, 4],
        frames=[dict(distance=length*i/max(8, math.ceil(length*4)),
                     center=[0, 90, length*i/max(8, math.ceil(length*4))],
                     right=[-1, 0, 0], up=[0, 1, 0], forward=[0, 0, 1], profileAngle=0, renderProfileAngle=0)
                for i in range(max(8, math.ceil(length*4))+1)],
        decorationInputs=dict(schema=1,
            scope='potential-renderer-inputs-only; no model availability or clearance claim',
            matrixLayout='column-major-model-to-world', spines=spines, ties=ties))])]


class ExportChecks(unittest.TestCase):
    def test_non_grid_length_and_native_right_convention(self):
        self.assertEqual(audit.verify(fixture()), dict(routes=1, sections=1, spines=7, ties=2))

    def test_quarter_turn_bank_uses_model_handedness(self):
        data = fixture()
        inputs = data[0]['sections'][0]['decorationInputs']
        for piece in inputs['spines'] + inputs['ties']:
            piece['up'] = [1, 0, 0]
            piece['right'] = [0, 1, 0]
            matrix = piece['modelTransform']
            for start in (0, 4, 8):
                matrix[start], matrix[start+1] = matrix[start+1], -matrix[start]
            matrix[12], matrix[13] = matrix[13]-90, 90-matrix[12]
        self.assertEqual(audit.verify(data)['spines'], 7)

    def test_tie_epsilon_clamps_to_real_endpoint(self):
        data = fixture(1.9999995)
        self.assertEqual(data[0]['sections'][0]['decorationInputs']['ties'][-1]['distance'], 1.9999995)
        self.assertEqual(audit.verify(data)['ties'], 2)

    def test_legacy_export_is_rejected(self):
        data = fixture()
        del data[0]['sections'][0]['decorationInputs']
        with self.assertRaises(KeyError):
            audit.verify(data)

    def test_missing_or_rail_grid_decoration_is_rejected(self):
        for mutation in ('missing', 'interpolated'):
            data = fixture()
            pieces = data[0]['sections'][0]['decorationInputs']['spines']
            if mutation == 'missing':
                pieces.pop()
            else:
                pieces[0]['distance'] = 0
            with self.subTest(mutation=mutation), self.assertRaises(ValueError):
                audit.verify(data)

    def test_wrong_tie_origin_and_nonfinite_matrix_are_rejected(self):
        for value in (0, float('nan')):
            data = fixture()
            data[0]['sections'][0]['decorationInputs']['ties'][0]['modelTransform'][12] = value
            with self.subTest(value=value), self.assertRaises(ValueError):
                audit.verify(data)

    def test_profile_and_identity_corruption_are_rejected(self):
        for key, value in [('variant', 'wrong-family'), ('geometryHash', 1), ('profile', [])]:
            data = fixture()
            data[0]['sections'][0][key] = value
            with self.subTest(key=key), self.assertRaises((ValueError, TypeError)):
                audit.verify(data)
        data = fixture()
        data[0]['sections'][0]['decorationInputs']['spines'][0]['profileAngle'] = .1
        with self.assertRaises(ValueError):
            audit.verify(data)

    def test_trace_pairing_rejects_other_run_and_incomplete_trace(self):
        data = fixture()
        rows = [dict(type='body', role='chassis', id='fixture-chassis', car='fixture-chassis'),
                dict(type='sample', variant='arrow_four_rail_v1', poses=[{'id':'fixture-chassis'}]),
                dict(type='complete', variant='arrow_four_rail_v1', cars=1)]
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp)/'trace.jsonl'
            path.write_text(''.join(json.dumps(row)+'\n' for row in rows))
            self.assertEqual(audit.verify_trace(data, path)['samples_by_variant']['arrow_four_rail_v1'], 1)
            path.write_text(''.join(json.dumps(row)+'\n' for row in rows[:-1]))
            with self.assertRaises(ValueError):
                audit.verify_trace(data, path)
            rows[1]['poses'][0]['id'] = 'another-run-chassis'
            path.write_text(''.join(json.dumps(row)+'\n' for row in rows))
            with self.assertRaises(ValueError):
                audit.verify_trace(data, path)

    def test_wrong_persisted_family_and_route_design_are_rejected(self):
        for family in ('unrelated', 'pax_shuttle', 'ARROW'):
            data = fixture()
            data[0]['sections'][0]['family'] = family
            with self.subTest(family=family), self.assertRaises(ValueError):
                audit.verify(data)
        data = fixture()
        data[0]['family'] = 'PAX'
        with self.assertRaises(ValueError):
            audit.verify(data)

    def test_original_frame_nonfinite_values_and_wrong_distance_are_rejected(self):
        for field in ('center', 'right', 'up', 'forward', 'distance', 'profileAngle', 'renderProfileAngle'):
            for bad in (float('nan'), float('inf')):
                data = fixture()
                frame = data[0]['sections'][0]['frames'][0]
                if isinstance(frame[field], list):
                    frame[field][0] = bad
                else:
                    frame[field] = bad
                with self.subTest(field=field, value=bad), self.assertRaises(ValueError):
                    audit.verify(data)
        data = fixture()
        data[0]['sections'][0]['frames'][1]['distance'] = 0
        with self.assertRaises(ValueError):
            audit.verify(data)

    def test_reversed_edge_retains_saved_profile_and_reverses_renderer_profile(self):
        data = fixture()
        section = data[0]['sections'][0]
        section['canonical'] = False
        section['edgeFrom'], section['edgeTo'] = section['railTo'], section['railFrom']
        section['profile'] = [dict(distance=0, angle=0, slope=.2), dict(distance=3.3, angle=1, slope=.2)]
        section['renderProfile'] = [dict(distance=0, angle=1, slope=-.2), dict(distance=3.3, angle=0, slope=-.2)]
        for frame in section['frames'] + section['decorationInputs']['spines'] + section['decorationInputs']['ties']:
            frame['profileAngle'] = audit.angle_at(section['profile'], frame['distance'])
            frame['renderProfileAngle'] = audit.angle_at(section['renderProfile'], frame['distance'])
        self.assertEqual(audit.verify(data)['sections'], 1)
        self.assertEqual(section['frames'][0]['profileAngle'], 0)
        self.assertEqual(section['frames'][0]['renderProfileAngle'], 1)
        for mutation in ('direction', 'slope', 'angle'):
            bad = copy.deepcopy(data)
            s = bad[0]['sections'][0]
            if mutation == 'direction':
                s['canonical'] = True
            elif mutation == 'slope':
                s['renderProfile'][0]['slope'] = .2
            else:
                s['decorationInputs']['ties'][0]['renderProfileAngle'] = 0
            with self.subTest(mutation=mutation), self.assertRaises(ValueError):
                audit.verify(bad)

    def test_duplicate_route_identity_is_rejected(self):
        data = fixture()
        data.append(copy.deepcopy(data[0]))
        with self.assertRaises(ValueError):
            audit.verify(data)


if __name__ == '__main__':
    unittest.main()
