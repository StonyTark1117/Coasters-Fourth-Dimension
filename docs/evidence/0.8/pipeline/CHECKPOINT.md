# Pipeline implementation checkpoint

Owner: `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary checkout
`/home/braydon/Documents/Coasters: Fourth Dimension`, existing `main`, starting
HEAD `cb46ea072d2c731948e4cac07e4d5350e269dbc5`. Lead owns integration and all
commits. Pre-existing untracked `logs/` is preserved.

Assigned outcome is FD1401A–C, followed by Dive FD1702A–C/FD1702. Pipeline consumes
the adopted seated P-gauge specification and accepted Wing/fixed-seat contracts.
It has no seat compliance and no FD1701A or Axis dependency. Dive awaits the
lead's qualified FD1701A handoff before dependent mechanics acceptance.

New package source, original models/recipes, native contract fixture and guide
are in progress. Model generation verifies legal element bounds and explicit UVs.
Shared Pipeline integration is coordinated with the lead; no mechanics or player
acceptance is claimed yet. The smallest next native check covers P-vs-N fit,
one/two cars at eight blocks, exact fixed seats, asymmetric/full equal mass,
dock-only unload, persistence and missing-frame whole-train hold. The connected
prototype then covers normal input, contact/rider clearance, forward/full-roll
and reverse return, with its distinct interrupted stop/restart.

Reserved run root: `/mnt/NVME4/fourthdimension-runs/fd1401-pipeline-20261115`.
Display `:317`, server/RCON ports `25652/25653`, units
`fd1401-pipeline-20261115-{server,client,xvfb}.service`. Launch01 used Java21,1536M client heap and6GiB Minecraft-only service. World
`server/PipelineWorkshop` remains outside builds. Native contract1/1 passed.
Normal mouse camera, vanilla use, Pipeline boarding, Close restraints and Dispatch
passed; linked two-car full roll and fixed return completed without fault.
Interrupted Stop on the incline exposed steady gravity creep (~.077 blocks/s),
above the explicit Resume threshold. Pipeline-only native stationary hold after
deceleration is being repaired and checked on the exact saved world. All three
owned services stopped cleanly (inactive/MainPID0), all worlds saved, before
launch02 preparation. A/BC acceptance remains open; no other cases reopened.

Existing shared files temporarily edited and returned to lead: Family, TrackDesign,
VehicleFactory, VehicleParts, ConsistRules, EligibilityHooks, Boarding, NativeLoading,
FourthDimension and mixin configuration. OriginalTrackRenderer was also handed back to the lead on 2026-11-15 after
the occupied views verified its bounded P rail/support placement. New Pipeline
files/resources/generator/guide/evidence remain with this owner.

FD1401A closed in README.md after reverse full-roll return and normal unload.
All three services inactive/MainPID0; A-qualified-world saved outside builds.
B/C next: supplied ingredients, normal recipes/paid track/car/Chain/station build.
Dive authority prerequisite accepted from lead at main cc094ada2382a597c8fcdba4590f38494803d31c.
