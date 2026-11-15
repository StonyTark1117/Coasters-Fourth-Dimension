# Milestone 0.9 dependency baseline

FD501E, FD501G and FD501H are complete on 2026-11-15. FD501F's
[separate inspection](../immersion/README.md) and
[resolved client/server classpaths](../immersion/runtime-classpath.json) record
the client-only dependency disposition. FD501F is complete after actual
client load confirmation of the direct-launch classpath correction below. These closures qualify prerequisites;
FD910–FD914 still own their feature and coexistence behavior.

## Selected artifacts

The [artifact audit](artifact-audit.json) records rehashed local upstream artifacts,
embedded metadata and mixins. Pins are NeoForge21.1.256, Create6.0.10,
Coasters Simulated0.1.5, Sable2.0.6, Sable Companion1.6.0, Flywheel1.0.6 and
Veil4.3.2. Skin1.1.0/file9409938 requires the NeoForge/Sable upgrade.
No optional Sodium or shader profile is claimed. Skin remains optional.
Waterparked0.2.8-beta-hotfix1/file9058331 and Create Coasters2.0/file8110328
require no additional core, provider or schema migration beyond this shared
baseline. Waterparked additionally needs the KotlinForForge language loader; the
optional profile now includes KotlinForForge for NeoForge5.10.0. [Artifact record](../coexistence/language-loader.json)
and the passing targeted native load close this dependency omission. Their graph/renderer and
utility/seat/camera overlaps remain explicit FD913/FD914 consumer checks.
Immersion1.1.1/file9542588 is installed only in the client/observer runtime;
the resolved server classpath excludes it. No further baseline upgrade is needed.

## FD501E affected contract evidence

* Java21 [compile](compile-01.log) passed with the exact Skin profile. The
  [Sable API audit](sable-api-audit.json) compares30 consumed classes: all remain
  present; native poses, carrier constraints and mass interfaces remain compatible.
  Serializer change is logging-only; physics initialization now refreshes config.
* [Native controlled check](native-01.log) passed the existing Arrow full-turn,
  capture, identity, eligibility and save contract. The same invocation exposed
  a test-only free-carrier endpoint assumption. [Diagnostic](free-02.log) measured
  genuine oscillation returning near its initial angle. The corrected existing
  assertion measures maximum excursion during the interval, without changing
  production physics. [Targeted free check](free-03.log) passed: excursion1.4372rad,
  independent-carrier separation0.7016rad, capture/save and unknown-schema denial.
  Closed sibling families were not rerun.
* Veil4.3.2 `ThreadTaskScheduler.class` is byte-identical to4.1.4
  (SHA256 `7e0c57af4afe9bacfa052d7b208f82c933c11115e92a3abb7e05099622cf338a`).
  The retained constructor-order/empty-schedule fix still applies.
  [Client log](client.log) records512 schedules,3072 completed tasks and128 empty
  schedules passing against the transformed installed class.
* The offline copy of the accepted Dive completed world loaded on the packaged
  upgraded server/client. [Comparison](migration-comparison.json) retains schema8,
  all20 rail records, all4 vehicle identities, carrier/seat identities and Dive
  phase/countdown/release records. [Train](restored-train.png) and the separate
  [constructed car](restored-constructed-car.png) render on native restored bodies.
  The source completed world was untouched. No serialized format conversion was
  required and no downgrade compatibility is claimed.
* The remote constructed car initially retained the existing unloaded-chassis
  safety fault. Its native body appeared when its chunk loaded. One normal
  Rotation Tool **Stop** revalidated it: [Ready editor](revalidated-car.png),
  [saved readback](revalidated-save.json) with all4 faults empty. This needed no
  production change. The accepted three-car cycle remains closed.

Disclosed setup used Creative/time/position commands to inspect the copied world,
plus one supplied crafting table and Rotation Tool. One mistaken position off the
platform caused a fall/respawn; it did not change the vehicle test. A Create Wrench
first produced its native no-links message; the correct Rotation Tool then opened
the editor. Camera turning and reachable vanilla table use were separately proven
before the normal Stop input. [Input record](input.jsonl) and [console record](console.jsonl)
retain those distinctions; this is upgrade/load validation, not a new construction
or occupied cycle claim.

## Runtime and cleanup

[Identity](runtime-identity.json) records the packaged JAR hash, owner, persistent
world, display316 and ports25650/25651. Java21 used1536M client heap inside6GiB,
2048M server heap inside4GiB, with Gradle outside. Memory had over32GiB host headroom
before launch; [final measurements](memory-final.txt) remained below caps.
Both client sessions ended, the server saved every dimension and exited, and owned
Xvfb stopped. [Cleanup](cleanup.txt) shows all three units inactive/MainPID0.
Full worlds/logs remain at `/mnt/NVME4/fourthdimension-runs/fd09-baseline-20261115`;
passing native worlds are outside disposable build output. No runtime ownership
was handed off. Skin/Visual and Seats/Immersion received the qualified baseline.

## FD501F direct-launch correction

The first scan combined generated files, and the first repair put Immersion in
both legacy and direct classpaths. Actual loading still omitted it. Read-only
FML4.0.45/BootstrapLauncher2.0.2 inspection found `additionalRuntimeClasspath`
means bootstrap libraries: paths already located in the bootstrap layer are
skipped by UserdevLocator when discovering mods. The dedicated resolvable
`fdImmersionClientMods` configuration now goes only on client/observer direct
launch and JavaExec classpaths, never their LegacyClasspath or either server
classpath. The [exact-file record](../immersion/runtime-classpath.json) supersedes
both earlier broad/incorrect checks. The owner's launch04 confirms actual Immersion1.1.1 discovery, startup, resources
and authenticated dedicated-server connection with the server omitting Immersion.
FD501F is closed again; FD912 still owns camera/seat effects, and no unrelated
acceptance is reopened.
