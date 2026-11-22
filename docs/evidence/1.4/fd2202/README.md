# FD2202 Motorbike evidence — 2026-11-22

Primary `main`, baseline `59c40d11`; original fixed two-saddle `motorbike/straddle_v1`, one unlinked car. [Specification](../../../specs/FD2202-motorbike.md) and [builder guide](../../../MOTORBIKE.md). The finite FD2202 outcome is complete; shared integration and the FD2203 gate remain parent-owned.

Persistent run: `/mnt/NVME4/fourthdimension-runs/fd14-motorbike-20261122`. Private host Xvfb `:342`, endpoint `127.0.0.1:25723`, RCON `25724`, player `Dev`; service prefix `fd14-motorbike-20261122`. Java 21, client heap1536M in6GiB Minecraft-only service, separate server4GiB service. Per-owner Gradle project cache and build directory. Latest checked JAR SHA256 `7101b562cbeea7e2286c51b2c241935d3a727f3eb4db22de0297964a33c92ad0`; `prepare-straight-drive.log` passed. `evidence/source-sha256.json` records owned source. Exact input, setup commands and readbacks live in `evidence/input.jsonl` and `evidence/console.jsonl`.

## Native and geometry

`native-motorbike-5.log` passes1/1 with clean shutdown: actual native movement; original dock/platform and open-restraint dispatch guards; lost/reversed drive and missing-support retained stops; explicit resume; valid and malformed normal saved-state handling. Parent independently reviewed the assertions. Earlier native attempts were fixture/setup diagnoses and are retained, not substituted for this pass. [Geometry audit](geometry-audit.json) verifies connected frame/saddle/grip/footrest/hinge paths,110° hold-down sweep and all palette geometry. [Generated manifest](generated-files.txt) lists46 exports.

## Occupied operation and corrected example

Generated example construction and disclosed teleports/aims are setup, not normal building/camera claims. Camera movement and vanilla crafting-table use were first verified in the same live client. Normal saddle use, tool controls, Shift and mouse look are actual input. Original run already proved boarding, closed-restraint Shift denial, Stop, straight and banked travel, but stalled near a return curve. Its `server/world` remains preserved.

The stall was an actual native-cart collision with a Create shaft at `(97,100,88)`: cart AABB X95.976475…97.008351/Z88.662523…89.689869 overlapped shaft X97…98/Z88.3125…88.6875. Canonical edge direction was investigated and was not the demonstrated cause. The original example now adds midpoints `(0,56)` and `(96,56)`, powers the following five edges from each of those straight locations, and removes curve-joint shafts. Station platforms end atZ50 to avoid theZ56 drive. The validation-only `fd motorbike_inspect` is read-only and records support, drive, rider and collision diagnostics.

Fresh `server/world-corrected`, car `8126c329-39bb-478a-8c5b-d3e5d74e964c`: normal boarding19:24:37UTC, Close19:25:08, Dispatch19:25:22. Actual4blocks/s straight motion,30° bank and both drive ports passed. Returning station captured at `(0.5,100.625,36.25)`, one rider, no fault. Open19:27:20 then ordinary Shift19:27:28 put Dev on platform `(-2.5,101,36.5)`, RootVehicle absent, ordinary standing model/camera. Captures `20261122T192701.654202Z-after.png`, `192720.987475Z-after.png`, `192728.396826Z-after.png` are under the run evidence directory (full date prefix on every filename).

## Distinct cold restart and drive interruption

Second normal boarding/Dispatch with iron chestplate/leggings verified armor follows the straddle pose (`20261122T192846.021258Z-after.png`). Saved while occupied/running, then cleanly stopped server and client. `pre-cold-running.json` records running1/stopping0/capture0, one assigned rider. Server/client PIDs1015461/1016699 ended; new processes1055104/1055516 loaded the same world and car. `post-cold-retained.json` and `cold-restart-comparison.json` prove identical chassis/carrier/rider-seat identity, running0/stopping1/capture1 and explicit restart fault. Native Create seat entity UUID is recreated on login; carrier and rider assignment remain unchanged. No automatic start occurred.

Normal Resume19:30:08 restored physical movement. A separate disclosed external shaft removal `(2,100,56)` interrupted accepted drive while occupied on the bank. Readback19:30:25: stopped/captured, one rider, closed restraint, zero drive. Replacing the identical shaft restored4blocks/s native target but car stayed stopped (`19:30:34`); normal Resume19:30:50 deliberately released it. The car physically returned to the original platform `(0.5,100.625,36.88)` with one rider and no fault. The panel kept open during station capture had an older revision; its first Open was correctly rejected as stale. Reopening the authoritative panel then Open19:33:09 and normal Shift19:33:42 unloaded to `(-2.5,101,36.5)`, with RootVehicle absent. No simulated resume or teleport of the car was used.


## Survival construction and recipes

Upstream ingredients, recipe-book knowledge, two native anchors, flat work floor, crafting table and disclosed position/aim commands were supplied as setup. The player remained in Survival (`playerGameType=0`); only ordinary client actions crafted and placed the new content. In vanilla crafting UI, Motorbike Car used two saddles, five iron ingots, one native track and one native cart; the resulting one car appeared in inventory. Motorbike Track used eight native tracks plus eight orange dye and produced eight binders. Inputs were consumed and outputs verified by authoritative inventory reads.

Normal Motorbike Track use on reachable anchors `(200,100,0)` and `(200,100,32)` selected/stored the first endpoint, committed the second, created matching `motorbike/straddle_v1` track and consumed all eight items. The actual native binder graph is Y100.8125; the disclosed car-placement aim was calculated from that graph height before clicking. Normal Survival car use at19:37:22 consumed the one crafted car and created `2217f5e0-4ea4-4056-80c1-2a0c70ef55be`, stopped/captured/open, no fault. [Saved comparison](survival-placement-summary.json) shows vehicle count1→2 with matching bound endpoints. The new straight is construction evidence; the occupied loop supplies ride acceptance. No full shared Arrow construction replay was needed.

## Retention and cleanup

Both original failed-world and corrected-world data remain under the run's `server/` directory. Raw input/console JSONL, native logs, builds and pre/post cold/save snapshots remain at the persistent run root. `final-server.log` records all dimensions saved at19:38:08UTC; [cleanup](cleanup.txt) verifies client, server and Xvfb inactive/dead with MainPID0. [Measured memory](final-memory.txt) stayed within the client6GiB/server4GiB limits. No runtime is handed off live.

No central docs, existing shared source or other owner's files were changed by this package. Owned new source/assets/spec/guide/evidence remain uncommitted for the parent's selective integration. Existing `logs/` and other owners' concurrent 1.4 changes remain preserved.

## Representative captures

- [Occupied straddle and connected frame](occupied-straddle.png), [armor follows the same pose](armored-straddle.png).
- [Normal platform unload](normal-unload.png), [cold restart held](cold-retained.png), [recovered platform ready](recovered-ready.png), [recovered unload](recovered-unload.png).
- [Real car recipe](car-recipe.png), [real track recipe](track-recipe.png), [crafted car placed in Survival](survival-placement.png).

These views combine with normal input/authoritative state and the native geometry/guard checks. They do not claim two simultaneous live riders, an all-family campaign, arbitrary unpowered-coast recovery, a second-client synchronization redesign or release publication.
