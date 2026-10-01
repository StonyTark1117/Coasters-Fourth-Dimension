#!/usr/bin/env python3
"""Generate original item guidance and native recipe-book discovery data."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
GUIDANCE = {
    "arrow_track": [
        "Connect two coaster anchors.",
        "Driven carriers follow distance profiles.",
        "Sneak-click cancels selection.",
    ],
    "legacy_zacspin_track": [
        "Connect two coaster anchors.",
        "Use with a Legacy ZacSpin Car.",
        "Sneak-click cancels selection.",
    ],
    "free_spin_track": [
        "Connect two coaster anchors.",
        "Supports Free Spin cars and magnetic spans.",
        "Sneak-click cancels selection.",
    ],
    "revised_zacspin_track": [
        "Connect two coaster anchors.",
        "Use with Revised Free-Rotor Cars.",
        "Sneak-click cancels selection.",
    ],
    "pax_track": [
        "Build one connected shuttle route.",
        "Use with a PAX-Inspired Shuttle Car.",
        "Use a winch for lift, release and capture.",
    ],
    "arrow_car": [
        "Four seats on two driven wing carriers.",
        "Requires Controlled 4D Track.",
        "Capture, close restraints, then dispatch.",
    ],
    "legacy_zacspin_car": [
        "Eight seats; gravity drives rotation.",
        "Requires Legacy ZacSpin Track.",
        "Capture carriers before boarding.",
    ],
    "free_spin_car": [
        "Four carriers with magnetic assistance.",
        "Requires Magnetic Free Spin Track.",
        "Tune with the Rotation Tool.",
    ],
    "revised_zacspin_car": [
        "Independent seat pairs with lower hinges.",
        "Requires Revised Free-Rotor Track.",
        "Capture carriers before boarding.",
    ],
    "pax_car": [
        "Four seats; gravity drives rotation.",
        "Requires PAX-Inspired Shuttle Track.",
        "Board only when the winch is ready.",
    ],
    "rotation_tool": [
        "Use on an anchor or assembled 4D car.",
        "Edit rotation, tuning and restraints.",
        "Rebind profiles after changing track.",
    ],
    "magnetic_inducer": [
        "Adds spin torque on Free Spin track.",
        "Use on a section to install a span.",
        "Edit its direction, strength and targets.",
    ],
    "magnetic_damper": [
        "Opposes spin on Free Spin track.",
        "Use on a section to install a span.",
        "Damping does not lock carriers for boarding.",
    ],
    "boarding_station": [
        "Use empty-handed to bind a nearby 4D car.",
        "Rear power captures; release dispatches.",
        "Front: ready. Right: restraints closed.",
    ],
    "shuttle_winch": [
        "Bind a stopped PAX train within 12 blocks.",
        "Supply Create rotation for the lift.",
        "Use again to start when boarding is ready.",
    ],
}

# Discover family tracks with their dye; cars with their matching track item.
UNLOCK_ITEMS = {
    "arrow_track": "minecraft:red_dye",
    "legacy_zacspin_track": "minecraft:yellow_dye",
    "free_spin_track": "minecraft:blue_dye",
    "revised_zacspin_track": "minecraft:purple_dye",
    "pax_track": "minecraft:green_dye",
    "arrow_car": "fourthdimension:arrow_track",
    "legacy_zacspin_car": "fourthdimension:legacy_zacspin_track",
    "free_spin_car": "fourthdimension:free_spin_track",
    "revised_zacspin_car": "fourthdimension:revised_zacspin_track",
    "pax_car": "fourthdimension:pax_track",
    "rotation_tool": "minecraft:compass",
    "magnetic_inducer": "minecraft:redstone",
    "magnetic_damper": "minecraft:quartz",
    "boarding_station": "minecraft:comparator",
    "shuttle_winch": "fourthdimension:pax_track",
}


def write(path, value):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, indent=2) + "\n")


def generate():
    recipes = {p.stem for p in (ROOT / "data/fourthdimension/recipe").glob("*.json")}
    if recipes != GUIDANCE.keys() or recipes != UNLOCK_ITEMS.keys():
        raise ValueError("Every player recipe needs guidance and a discovery ingredient")
    language_path = ROOT / "assets/fourthdimension/lang/en_us.json"
    language = json.loads(language_path.read_text())
    language = {k: v for k, v in language.items() if not k.startswith("tooltip.fourthdimension.")}
    for name, lines in GUIDANCE.items():
        for index, line in enumerate(lines, 1):
            language[f"tooltip.fourthdimension.{name}.{index}"] = line
    write("assets/fourthdimension/lang/en_us.json", language)
    for name, ingredient in UNLOCK_ITEMS.items():
        recipe_id = f"fourthdimension:{name}"
        write(f"data/fourthdimension/advancement/recipes/{name}.json", {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_ingredient": {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {"items": [{"items": ingredient}]},
                },
                "has_the_recipe": {
                    "trigger": "minecraft:recipe_unlocked",
                    "conditions": {"recipe": recipe_id},
                },
            },
            "requirements": [["has_ingredient", "has_the_recipe"]],
            "rewards": {"recipes": [recipe_id]},
        })


if __name__ == "__main__":
    generate()
