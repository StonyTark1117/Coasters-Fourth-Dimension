"""Analytic geometry checks only; no native or client acceptance evidence."""
import copy
import importlib.util
from pathlib import Path
import unittest

import numpy as np

HERE = Path(__file__).resolve().parent

def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result

styles = load('styles_audit', HERE.parent/'audit-recovery-styles.py')
fixtures = load('decoration_fixtures', HERE/'test_recovery_decoration_inputs.py')


def models():
    tri = np.array([[(0, 0, 0), (1, 0, 0), (0, 1, 0)]], dtype=float)
    return {(style, part): tri for style in styles.STYLES for part in ('spines', 'ties')}


class StyledGeometry(unittest.TestCase):
    def test_obj_parser_keeps_raw_origin_and_handles_relative_faces(self):
        text = 'v .5 0 .25\nv 1.5 0 .25\nv .5 1 .25\nf -3/1 -2/2 -1/3\n'
        self.assertTrue(np.array_equal(styles.raw_obj(text)[0, 0], [.5, 0, .25]))
        for bad in ('v nan 0 0\nf 1 1 1', 'v 0 0 0\nf 0 1 1', 'v 0 0 0\nf 1 2 3'):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                styles.raw_obj(bad)

    def test_exported_tie_transform_applies_origin_once(self):
        piece = fixtures.fixture()[0]['sections'][0]['decorationInputs']['ties'][0]
        source = np.array([[([.5, 0, .25]), ([1.5, 0, .25]), ([.5, 1, .25])]])
        world = styles.world_triangles(source, piece)[0]
        np.testing.assert_allclose(world, [[0, 89.4, 0], [.85, 89.4, 0], [0, 90.4, 0]], atol=1e-5)
        bad = copy.deepcopy(piece)
        bad['modelTransform'][3] = 1
        with self.assertRaises(ValueError):
            styles.world_triangles(source, bad)

    def test_styled_ownership_and_lowered_support_geometry(self):
        route = fixtures.fixture()[0]
        (mesh, labels), counts = styles.surface(route, {'fixture-rail':'arrow_4d'}, models())
        self.assertFalse(any(label.endswith(':rail4') for label in labels))
        self.assertEqual(counts, dict(originalSections=0, styledSections=1, spines=7, ties=2,
                                     crossbraces=3, endpointBridges=2, loweredControlMounts=6))
        control = mesh[[':controlMount' in label for label in labels]]
        self.assertAlmostEqual(control[:,:,1].min(), 88.85, places=5)
        bridges = mesh[[':appearanceEndBridge' in label for label in labels]]
        self.assertAlmostEqual(bridges[:,:,1].min(), 88.85, places=5)
        self.assertAlmostEqual(bridges[:,:,1].max(), 89.45, places=5)
        original, original_labels = styles.recovery.track_mesh(route)
        for rail in (':rail0', ':rail1', ':rail2', ':rail3'):
            np.testing.assert_array_equal(mesh[[label.endswith(rail) for label in labels]],
                                          original[[label.endswith(rail) for label in original_labels]])

    def test_mixed_original_and_double_spine_plan(self):
        route = fixtures.fixture()[0]
        second = copy.deepcopy(route['sections'][0])
        second['rail'] = 'second-rail'
        route['sections'].append(second)
        (_, labels), counts = styles.surface(route, {'fixture-rail':'arrow_4d_dbl_spine', 'second-rail':'original'}, models())
        self.assertEqual((counts['originalSections'], counts['styledSections']), (1, 1))
        self.assertTrue(any(label == 'section1:rail4' for label in labels))
        self.assertFalse(any(label == 'section0:rail4' for label in labels))
        self.assertTrue(any(':arrow_4d_dbl_spine:' in label for label in labels))

    def test_missing_unknown_nonarrow_or_unsupported_stroke_refused(self):
        route = fixtures.fixture()[0]
        for plan in ({}, {'fixture-rail':'unknown'}, {'fixture-rail':'original'}):
            with self.subTest(plan=plan), self.assertRaises(ValueError):
                styles.surface(route, plan, models())
        for field in ('family', 'profile'):
            bad = copy.deepcopy(route)
            if field == 'family':
                bad['family'] = 'PAX'
            else:
                bad['sections'][0]['profile'][0]['angle'] = 4*np.pi+.1
            with self.subTest(field=field), self.assertRaises(ValueError):
                styles.surface(bad, {'fixture-rail':'arrow_4d'}, models())

    def test_separation_rejects_containment(self):
        # Containment must remain uncertified and a separated surface must give a real bound.
        box = np.array(list(__import__('itertools').product((-1, 1), repeat=3)))
        surfaces = np.array([[(0, 0, 0), (.5, 0, 0), (0, .5, 0)],
                             [(2, 0, 0), (2, .5, 0), (2, 0, .5)]])
        np.testing.assert_allclose(styles.recovery.separation(box, np.eye(3), surfaces), [0, 1])

    def test_vanilla_player_mesh_envelope_is_conservative_and_orientable(self):
        low, high = styles.recovery.vanilla_player_mesh_bounds()
        self.assertTrue(np.all(low < high))
        self.assertGreater(-low[0], .9)
        self.assertLess(low[1], -1.1)
        self.assertGreater(high[1], .4)
        # The carrier transform must preserve the local envelope dimensions.
        angle = np.pi / 3
        axes = np.array(((1, 0, 0), (0, np.cos(angle), np.sin(angle)),
                         (0, -np.sin(angle), np.cos(angle))))
        eye = np.array((4.0, 8.0, 12.0))
        world = np.array(list(__import__('itertools').product(*zip(low, high)))) @ axes + eye
        local = (world - eye) @ axes.T
        np.testing.assert_allclose(local.min(axis=0), low)
        np.testing.assert_allclose(local.max(axis=0), high)


if __name__ == '__main__':
    unittest.main()
