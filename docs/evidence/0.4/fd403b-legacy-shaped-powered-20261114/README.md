# FD403B Legacy player-built shaped powered route

**Result: FD403B complete 2026-11-14.** This record closes the missing
player-built shaped powered route with a normal connected-client build and ride.
It reuses the previously accepted Legacy cold-reopen record and the unchanged
Legacy walkthrough review; this run itself was saved but not cold-reopened.

## Run identity and provenance

- Owner: `01a19ed5-0739-74b0-80d9-e8eb888e6fe0` (Legacy lead).
- Retained run: `/home/braydon/.local/state/fourthdimension-runs/fd403b-legacy-shaped-20261113`.
- Private display `:262`; server port `25620`; player `Dev`.
- Runtime source/artifact and earlier crafting/flat-route provenance are retained
  in the run root and linked accepted [Legacy powered route](../survival-client-legacy-powered/acceptance.md).
- Required track and car were crafted through the normal recipe-book/table UI
  from upstream ingredients. No Fourth Dimension output item was granted.
- The Create Creative Motor was an explicitly disclosed upstream fixture; its
  placement itself used normal in-world right-click. This is not unassisted
  gathering evidence.

## Player-built shaped route and drive

The player used normal connected-client controls to commit the shaped Chain span
from `(314,-60,31)` to `(300,-60,31)`. Server block readback at the first anchor
reported `ChainLiftFirstHop`, committed segments, travel sign `-1`, shaft axis
`z`, and committed length `16.192384009274715`. The motor placed at
`(314,-60,32)` read back as `create:motor`, speed `-16.0`, with kinetic network
size `2`; the anchor's source points to that motor and its speed is `-16.0`.
This confirms the powered direction and connected drive on the actual shaped
player-built route.

## Occupied ride, free response, and safe exit

The car was normally placed on the route and the rider boarded the original
Legacy seat with empty hands. An earlier apparent failed boarding click had F3
hit `simulatedcoasters:coaster_cart`; after correcting aim, F3 identified
`fourthdimension:legacy_seat`, and one normal right-click boarded. The server
reported a `create:seat` RootVehicle, `occupants=1`, and a
`FD_BOARDING_EVENT action=mount`. This was an aim miss, not an item-use
regression.

The mounted editor showed Ready with restraints closed; normal Dispatch began
the powered traversal. Server position samples advanced from
`(316.729,-58.633,33.598)` to `(318.661,-58.625,35.918)` while the player
remained aboard. `/fd status` sampled both Legacy carriers released
(`capture=false`): the first sample had angles about `0.10499` and `-0.07508`
with distinct angular velocities. Later samples show them settling toward
capture alignment. Screenshots include the occupied route in motion.

Normal Capture returned the mounted editor to Ready with restraints closed.
Restraints were normally opened, then Shift dismount succeeded. Server
`RootVehicle` had no result, both carriers reported captured with
`occupants=0`, and the validation log recorded `FD_BOARDING_EVENT
action=dismount`. `save-all flush` completed and the server logged the world
save. This run supplements, rather than replaces, the accepted Legacy cold
reopen record.

## Teaching reconciliation

The [Legacy teaching reconciliation](../fd403b-legacy-teaching-reconciliation-20261112.md)
reviews the unchanged Building guide, Legacy Ponder lesson, current rendered
teaching evidence, and accepted Legacy mechanics. This normal shaped ride now
supplies the route/operation observation that record previously lacked. No
teaching resource or control path changed, so its Legacy walkthrough conclusion
is reused for the matching FD403F claim without repeating the walkthrough.

## Selected artifacts

- [F3-confirmed Legacy seat hit](screenshots/seat-hit-f3.png)
- [Mounted editor, restraints open](screenshots/editor-restraints-open.png)
- [Mounted editor, restraints closed](screenshots/editor-restraints-closed.png)
- [Occupied powered traverse](screenshots/occupied-powered-traverse.png)
- [Post-capture Ready state](screenshots/post-capture-ready.png)
- [Carrier status after capture](postcapture-status.json-string.txt)
- [Carrier status after dismount](dismount-status.json-string.txt)
- [Ride carrier sample 2](ride-sample-2.json-string.txt)
- [Ride carrier sample 3](ride-sample-3.json-string.txt)
