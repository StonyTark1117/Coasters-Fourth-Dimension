#!/usr/bin/env python3
"""Generate vanilla recipe-book discovery for the standalone acquisition items."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
recipes = root / 'src/main/resources/data/fourthdimension/recipe'
output = root / 'src/main/resources/data/fourthdimension/advancement/recipes'
output.mkdir(parents=True, exist_ok=True)
triggers = {
    'rotation_tool': 'create:brass_ingot',
    'boarding_station': 'minecraft:comparator',
    'shuttle_winch': 'create:shaft',
    'magnetic_inducer': 'minecraft:redstone',
    'magnetic_damper': 'minecraft:quartz',
}
for path in sorted(recipes.glob('*.json')):
    name = path.stem
    ingredient = ('#c:ingots/iron' if name.endswith('_track') else
                  'simulatedcoasters:coaster_cart' if name.endswith('_car') else triggers[name])
    recipe = f'fourthdimension:{name}'
    data = {
        'parent': 'minecraft:recipes/root',
        'criteria': {
            'has_ingredient': {'trigger': 'minecraft:inventory_changed',
                               'conditions': {'items': [{'items': ingredient}]}},
            'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked',
                               'conditions': {'recipe': recipe}},
        },
        'requirements': [['has_ingredient', 'has_the_recipe']],
        'rewards': {'recipes': [recipe]},
    }
    (output / path.name).write_text(json.dumps(data, indent=2) + '\n')
