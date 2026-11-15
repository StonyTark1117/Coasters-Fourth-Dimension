# S1 face-UV correction — complete 2026-11-15 UTC

The retained [normal placement view](../player/normal-car-placement.png) revealed
the same atlas-bleed defect diagnosed in Suspended: automatic UVs for elements
outside a block's 0–16 coordinates sampled neighboring textures. Orange seat
backs appeared dark and metal faces contained unrelated patterns. This reopened
only FD1501B texture correctness under FD916; A mechanics and C player workflow
remained accepted.

`generate-spinning-content.py` now emits explicit bounded face UVs. The generated
19 models cover all seat facing/restraint variants, the bearing, controller and
car item. [Static comparison and hashes](source-models.json) verify that every
face stays within its sprite and that geometry, texture identities and all other
model properties are unchanged. `--skip-language` permits family-only generation
without writing the language file owned by the concurrent Fixed Inverted task.

The lead reopened its retained `SpinningWorkshop` world on private display`:311`,
game/RCON25640/25641, root
`/home/braydon/.local/state/fourthdimension-runs/fd1501-spinning-20261114`.
The Java21 packaged JAR remained
`894c3e6efe8ff6414a99b0cf6d1eac51ac9a3a64b57953fdca6bdc1589682336`.
The exact corrected models were copied into the client-only resource pack
`client/resourcepacks/s1-uv-correction`; [resource-load evidence](resource-load.txt)
confirms it loaded. No shared Java was rebuilt, no mechanics was changed, and
this observation claims no new interaction or ride acceptance.

The [corrected saved-scene view](corrected-seat-and-bearing.png) shows orange seat
backs, black restraints and clean metal surfaces without neighboring atlas
patterns. [Client state](client-state.json) and the server join confirm the client
was connected to the retained world. The source comparison covers the identical
UV correction across the variants; another ride or profile check is unnecessary.
The named texture case is closed.

Host headroom was30GiB available with no limiting ancestor cgroup; persistent
storage had263GiB free. Client1536M heap/6GiB unit and server2GiB heap/4GiB unit
were verified. [Runtime identity and peak](runtime-before-stop.txt) are retained.
After the view, the client exited, the server saved and stopped through RCON, and
the private display stopped. [All three units are inactive](final-units.txt).
World, source override and evidence remain on persistent storage.
