# FD917 authoritative art

`materials.json` contains the editable pixel maps and deliberate palettes.
Export with `python3 scripts/fd917_materials.py --export` from the project root.
PNG outputs are deterministic RGBA tiles under
`src/main/resources/assets/fourthdimension/textures/block/fd917/`.
Native/provider artwork stays external.

Geometry remains editable in the owning family Python generator. Its named
cuboids and material slots are the source of truth; exported runtime JSON is not
an independent second source. The generator writes only its assigned resources.
Do not restore earlier combined recipe/texture generators over production art.

Consumers may import `box`, `density_elements`, `materials`, `model`, `texture`
and `item_display` from `scripts/fd917_materials.py`. Importing writes nothing.
Positions are Minecraft model units (16/block), and texture names have no `.png`.
`model` tiles surfaces longer than 16 units, removes internal subdivision faces
and assigns bounded UVs at 16 pixels/block. Keep explicit detail UVs deliberate.
The per-family articulation descriptors are owned by that family's exporter;
Mechanisms owns their common runtime interpretation.
