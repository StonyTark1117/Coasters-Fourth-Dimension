# FD910 Coaster Skin adapter

Status: **FD910 complete, 2026-11-15**. Targeted native contract and connected-
client selection, rendering, occupied ownership, fallback and persistence passed.
Owner: Skin/Visual `01a1dd1d-6e99-70d2-aef3-6dcebac10d72`, primary `main`.

Selected dependency: Coaster Skin 1.1.0 file9409938, actual mod ID
`create_coaster_skins`, with the FD501E NeoForge21.1.256/Sable2.0.6/Veil4.3.2
baseline. [Provider inspection fingerprint](provider-fingerprint.json) records
the actual JAR and sorted class digest. Third-party artwork is not copied.

## Finite capability

| Choice | Provider components used | Retained 4D components |
| --- | --- | --- |
| Original | None | All |
| Industrial chassis | Industrial underbody | Body, native wheels, carrier, seats/restraints |
| Classic chassis | Default Classic underbody/wheels | Body, carrier, seats/restraints |
| Antarctica chassis | Default Classic underbody, Antarctica wheels | Body, carrier, seats/restraints |
| Hotdog chassis | Default Classic underbody, Hotdog wheels | Body, carrier, seats/restraints |

The adapter replaces only the native chassis presentation, using the provider's
existing native cached track/body frame. It does not select the full Classic
seated/dual body or Industrial platform. WildSpin, SkyGlide, Inverted, Log and
Skimmer are not 4D body replacements: their provider mechanisms own seat,
collision or physics behavior. Full selections and native train-copy mutations
are fenced on owned chassis/carriers. PAX (`foundation_v1`) retains Original
carriage presentation; Pipeline retains its bespoke Original P contact/wheel
geometry. Ordinary native carts retain the provider's own UI.

Selection uses the 4D vehicle editor’s Appearance button. Server action80 checks
reach/build permission, revision, applicable existing operator permission and
an empty, boarding-ready whole train. Only the selected car’s appearance and
revision change. Existing full Foundation snapshots synchronize the saved enum;
no seat/pose synchronization owner is added. Missing provider, unverified release,
unsupported body or missing baked model/texture deterministically renders
Original without deleting a saved supported choice. Resource reload resolves
new baked models rather than reusing stale geometry.

## Targeted native proof

Java21 `runGameTestServer`, namespace `fourthdimension_fd910`, Skin profile,
passed one required test on 2026-11-15. The actual Arrow factory/recovery fixture
settled to a boarding-ready position. Five action80 choices passed; save/load
retained appearance and identical family/variant/carrier/seat/restraint state.
Stale revision, occupied edit and unsupported WildSpin selection rejected.
Native selector and link-copy mutations rejected owned bodies while an ordinary
native bearing remained selectable. Injected stale native style tags resolved to
Native physics on the owned chassis. Selection did not write native userData.

Log: [native01.log](native01.log).
The test server saved and exited successfully. This proves the server contract,
not normal player input or visual fit. Those client observations follow below.

## Connected-client choices and retained ownership — passed

The isolated dedicated-server workshop used ordinary player `Dev`, normal
Rotation Tool and mouse input, native graphics, 854×480 and GUI scale2. Fixture
construction, item grants, camera/position commands and viewing platforms are
disclosed setup; this is appearance acceptance, not another Survival build.
The same live client separately proved camera turning and reachable vanilla
crafting-table use with raw mouse input enabled and default mouse bindings.

- [Normal Appearance selection](normal-selection.png), [Arrow Industrial](arrow-industrial.png)
  and [Arrow Classic](arrow-classic.png): visible underbody/wheel choices retain
  the original red body, two independent carriers and four original seats.
- [Free Spin Antarctica after restart](free-antarctica-restored.png) and
  [after F3+T reload](free-antarctica-reloaded.png): cyan provider wheels remain
  aligned to native running gear; the original blue seats and four carriers remain.
- [Wing Hotdog](wing-hotdog.png): red/gold provider running gear retains the
  cyan fixed-wing body, four original seats and original restraint geometry.
  These views cover constrained, freely rotating and fixed interfaces through
  the shared chassis renderer; no all-family appearance matrix is claimed.
- [Occupied edit rejected](occupied-edit-rejected.png),
  [occupied motion](wing-occupied-motion.png), and
  [normal unload](wing-normal-unload.png): normal empty-hand boarding, Close,
  Dispatch, Stop, Open and Shift dismount preserve original passenger ownership.
  An occupied appearance change rejected and retained Hotdog. The bounded ride
  moved along the workshop loop to the far upright straight; existing family
  mechanics acceptance carries forward.
- [Provider Screwdriver rejected](native-screwdriver-rejected.png): one normal
  use on the reachable Wing seat denied full-body replacement. The screenshot
  uses the earlier “4D Wrench” wording; final source/launch04 says “Rotation Tool.”
  This text correction does not change the exercised guard.

The initial Appearance button was outside the short window. The lead moved it
to the editor title row; the normal-input captures above verify the correction.
Initial Antarctica/Hotdog resource paths omitted the provider's `default/`
directory. [The missing-model view](missing-model-fallback.png) safely rendered
Original. Corrected enum paths were checked against the actual provider JAR,
then the restored Antarctica and Hotdog views passed. Resource restoration and
reload preserve the saved choice; no malformed external resources are shipped.

## Provider removal and save identity — passed

After safe unload, the client exited and the dedicated server saved cleanly.
Launch04 removed Skin from **both** runtime classpaths and reopened the same world.
`/fd integrations` reported Skin absent. The reachable Wing still opened normally:

- [Original fallback](provider-absent-original.png) replaces the previously
  [selected Hotdog running gear](wing-before-provider-removal.png).
- [Appearance screen](provider-absent-selection.png) retains “Selected: Hotdog.”
- [Attempting Classic](provider-absent-rejected.png) reports the missing supported
  server provider and leaves Hotdog selected.

[Saved before](saved-before-removal.json), [saved after](saved-after-removal.json)
and the [exact invariant comparison](provider-removal-comparison.json) preserve
all three cars' chassis/carrier UUIDs, family, variant, selected appearance,
seat positions, occupants, restraints and carrier geometry. Runtime revision,
ticks and small settled free-carrier angle changes are outside that identity
comparison. No full provider reinstall cycle is claimed; corrected resource
restoration and reload are the separate observations above.

## Artifacts, scope and cleanup

Persistent run root:
`/mnt/NVME4/fourthdimension-runs/fd910-skin-20261115`.
Saved world: `server/SkinWorkshop`; input/console records and archived launch
logs: `evidence/`; immutable packaged JARs: `artifacts/`.

| Artifact | Relevant evidence | SHA256 |
| --- | --- | --- |
| launch02 | Visible editor, Industrial/Classic, safe missing-model fallback | `b30f57da7263481cc55215f76660de371bb63a3f33f985777f016df4dee1ec92` |
| launch03 | Corrected Antarctica/Hotdog paths, reload, occupied Wing and Screwdriver guard | `131fa9df24944f23d4beeeaf67b723322b81219e0a670c749fe67e8e662c551a` |
| launch04 absent | Same saved world without provider, identity comparison; final guard wording compiled | `787a01bfafd96f0d407449c43c092c38f3a0978089b365fb46d95cc9f919ab07` |

Java21 was used throughout. The Minecraft-only client scope had 6GiB limit and
1536M heap; observed launch03 peak was 3,056,136,192 bytes. The server used a
4GiB scope/2048M heap. Owned display317, game port25652 and RCON25653 are released.
[Cleanup verification](runtime-cleanup.txt) shows client, server and Xvfb units
inactive with MainPID0. Server launch04 logged all dimensions saved before exit.
The world, inputs, logs and screenshots remain available; no live runtime handoff
or further family/profile replay is required.

[Player configuration guide](../../../COASTER-SKIN.md) documents the finite choices,
installation, normal selection and fallback. [FD916](../visual-quality/README.md)
consumes these affected presentation views. Central status integration belongs
to the 0.9 lead.
