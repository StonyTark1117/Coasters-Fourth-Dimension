# FD903 historical Legacy and Free Spin client operator cycle

On 2026-11-02, the archived schema-3 world was opened by the current packaged
`0.9.0-dev.27` dedicated server and a connected packaged client. The client
boarded the migrated Legacy ZacSpin and S&S Free Spin cars separately, closed
restraints, sent the ordinary editor dispatch request, returned each car to a
safe captured state, opened restraints, and dismounted. The final save preserves
the three historical chassis and all eight carriers without a rider assignment.

## Inputs and verification

| Input | SHA-256 |
| --- | --- |
| Archived schema-3 world | `3f4f5d820deb9ffde91e576df7a582bd0234c8bfd2ddff2e3fe70e4dc0dbfac3` |
| Current packaged addon JAR | `81626641c2689a8ddf797dd6010767304ba2ad0bb455bc546b510a586e36888b` |
| Final schema-6 Foundation data | `2e39f43aeea3ae66c2ebefbeb218ddae48dbb551ac8d0ae42116d044df2569f8` |

The server and client used `-PfdPackaged -PfdValidation`; the client connected
with `-PfdConnect=127.0.0.1:25565`. The isolated server allowed flight because
the original Sable-mounted platforms are above the ordinary-world surface. The
player’s initial platform position and one Rotation Tool were supplied through
RCON. All boarding and editor actions used the connected client.

Before boarding, `fd validation availability` reported `chassis=3/3 carriers=8/8
assignments=0`. The historical Foundation record was upgraded from schema 3 to
schema 6 when loaded.

## Client observations

| Family | Client interaction | Server observation |
| --- | --- | --- |
| Legacy ZacSpin | The client mounted a native Legacy seat ([mounted view](native-schema3-legacy-boarded.png)), opened its car editor ([editor](native-schema3-legacy-editor.png)), closed restraints, sent Dispatch, then opened restraints and dismounted. | The mounted Legacy carrier reported `occupants=1` and global availability reported `assignments=1`. The server logged the accepted native seat assignment and later a normal restraint-open dismount. |
| S&S Free Spin | The client mounted one native Free Spin seat ([mounted view](native-schema3-free-spin-boarded.png)), opened its four-carrier editor ([editor](native-schema3-free-spin-editor.png)), closed restraints, sent Dispatch, then opened restraints and dismounted. | One of the four Free Spin carriers reported `occupants=1`; after a held Shift dismount, availability returned `assignments=0`. |

After the two cycles, `save-all flush` completed and the server shut down normally.
The final record has schema 6, 24 rails, three vehicles, and eight carriers. The
Legacy and Free Spin vehicles are each `stopping=true`, fully captured,
`restraints=false`, fault-free, and contain no saved rider seats.

The on-disk comparison passed:

```text
FD903_PACKAGED_MIGRATION sourceSchema=3 upgradedSchema=6 rails=24 vehicles=3 families=arrow_controlled,sns_free_spin,zacspin_legacy identitiesPreserved=true recoveryInvented=false profilesPreserved=true
```

## Scope

This advances historical client coverage from Arrow-only to bounded Legacy and
Free Spin operator cycles. The screen closed immediately after each Dispatch
request and the sampled state was captured again before a sustained route
traversal was measured. It therefore does **not** establish a sustained powered
Legacy or Free Spin historical ride. Two-client migration, broader historical
worlds and third-party functional-provider conversion also remain FD903 work.
