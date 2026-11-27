# FD 1.6 — Launch Systems moving handoff

FD2008 acceptance complete, 2026-11-27. This is an unpublished implementation
package. The [artifact manifest](artifacts.json) identifies FD 1.6.0 and its
required LS 1.0.1 companion; unchanged published LS 1.0.0 cannot read FD schema 9.
No tags, platform uploads or releases were created by this assignment.

Original Arrow and Wing retain their existing native inserts, bed/train limits,
restraints and eight moving-mechanism permissions. LS operates only on its
qualified straight, level fixed sections at physically locked docks. This does
not grant LS operation on a tilted/unlocked bridge or on the moving bed itself.
The [finite contract](../../specs/FD2008-launch-systems-1.6.md) and
[player guide](../../MOVING-TRACK.md#launch-systems-handoff-16) define the supported
Tire/LSM/LIM/fin setup, fresh receiving arm and separate launch request. Cable,
swing, specialty/other configurations and managed moving circuits remain denied.

| Required behavior | Evidence |
| --- | --- |
| Actual source/receiving LS force, native capture and physical movement | [Native Arrow and Wing](native/README.md); Arrow approach impulse51.7786, departure64.5458, peak2.64976m/s; Wing departure64.3403, peak2.67481m/s. |
| Complete retention and authority exclusion | Both native consumers: zero retained propulsion; LS traction and braking flags false; old token and competing receiver denied; whole train clears moving service. |
| Power interruption, disk restart, rider continuity and normal operation | [Two-real-client Wing](occupied/README.md): normal board/close/capture/move, interrupted power, full process restart, same seat, Resume/Release, fresh receiver arm, normal top-lever request, downstream exit, Stop/open/dismount. |
| Missing provider/controller and old adapter denial | Native dependency-loss 1/1; registry absence is a disclosed injection, actual receiving controller removed, no native fallback, explicit re-arm, dispatch revoked and measured stop. |
| Arrow fixed cabinet clearance/mounting | LS mounting helper plus8 targeted unit checks, prior absent-FD native1/1 with8 mechanism/side cases; combined Arrow proves the live positive path. Ordinary3m and total4m bounds retained; horizontal1.025m derives from FD socket tolerance. |
| Scope/trajectory guards | FD moving capability/program 12 unit tests pass. Existing1.5 trajectories, native body/posture/restore and family limits carry forward; shared representative moving handoff adds no new axes. |
| Endpoint/authority math | LS TrackDistance 5 and MotionAuthority 2 tests pass; actual endpoint traversal is exercised by both new native consumers and occupied departure. |
| Package identity and cleanup | Exact SHA256s, expanded metadata and source manifest here; FD archive reproduction matches, LS rebuilt JAR equals its consumed candidate. No companion classes are embedded. All owned native servers saved/exited, both GUI clients/server/Xvfb units inactive/success. |

FD production class bytes in the delivered JAR match the frozen occupied run
exactly. Fixture additions do not relabel old executions. Later LS changes from
the occupied input are the bounded deep-mount helper/cabinet path; Wing's ordinary
three-block mounts remain unchanged. Final Arrow and dependency-loss runs use
socket-envelope candidate66ec71f1. LS ordinary standalone/fixed qualification and
all FD1.0–1.5 accepted cases carry forward; no broad family/profile campaign.

Known limits remain the declared game-scale/native-solver limits, not physical
hardware certification. The earlier GUI setup's automatic GameTest chunk-unforce
is preserved as a fixture diagnostic. No general chunk-reload behavior is newly
claimed from that attempt. The passing cold recovery uses a saved dedicated
world with its original artifacts and exact car/seat identities.

Source baselines: FD main 3ac29538; LS codex/launch-systems-0-4-0 at be7f4f6.
The implementation commit contains the source manifest, tests and evidence. Raw
runs/worlds remain under `/mnt/NVME4/fourthdimension-runs/fd16-20261127`,
`fd16-occupied-20261127` and `/mnt/NVME4/launchsystems-runs/fd16-mounting-20261127`.
