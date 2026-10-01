# Create telemetry during occupied PAX motion and power faults

Two complete occupied PAX cycles verify all six native Create Display Link fields
on the winch and a bound Boarding Station through BOARDING, LIFTING, OUTBOUND,
RETURNING, CAPTURING and FAULT. Two actual packaged clients observe the winch goggles
through each phase. Power restoration leaves the fault latched; explicit validated
resume clears it. Dispatch and unloading remain denied during the fault. Both cycles
finish with normal station capture and validated unloading. No runtime change was needed.

## Fixture and procedure

The pinned combined-pack dedicated server uses the existing four-car, eight-carrier
PAX fixture restored from the preceding occupied reconnect evidence. Its initial
cycle count is four; Engineered requires FE and holds 12,345 FE throughout. The
validation fixture boards sixteen real pig entities through native Create seats,
then closes restraints through the existing operations interface. Dev and Observer
remain unmounted observers, wearing goggles in creative mode.

Four native Display Links attach to the winch at (-606, 180, 608); its south face
stays unobstructed for goggles. Six attach to the Boarding Station at
(-616, 180, 608), bound to the original lead chassis. Ten oak signs receive native
passive transfers. Setup configures source IDs and target offsets through ordinary
block NBT; it never writes sign text or calls display source methods directly.

| Pass | Winch fields | Station fields | Completed cycle |
| --- | --- | --- | --- |
| 1 | Status, occupancy, dispatch, phase; status switched to fault while held | All six | 4 → 5 |
| 2 | Fault, restraints, dispatch, phase | All six | 5 → 6 |

Each cycle follows this sequence:

1. Confirm sixteen mounted riders, closed restraints and dispatch readiness.
2. Dispatch through the existing validation command and observe LIFTING.
3. Remove the winch's native creative motor; observe the power-loss FAULT.
4. Request dispatch and unloading; verify both normal validators reject them.
5. Restore the motor at 128 RPM; confirm FAULT remains latched.
6. Request normal resume; observe LIFTING, OUTBOUND, RETURNING, CAPTURING, then BOARDING.
7. Confirm the cycle increment and readiness; unload through normal validation.

No phase, fault, car pose or rider state is directly edited during the cycle.
All sampled riders remain alive, mounted and unique, with stable identities within
each run. Carriage, four chassis and eight carrier identities persist across both
runs. External Engineered propulsion remains denied and FE remains 12,345.
The first set of unmounted fixture pigs is removed before boarding the second set.
The final saved world has empty cars with open restraints and cycle count six.

## Native displays and client presentation

There are 180 bracketed samples: each reads a native server report, the ten actual
sign lines, then another report. The audit compares the reports with the full server
log, reparses sign responses from the raw RCON transcript, independently derives
expected display strings, and recalculates convergence. It retains all 25 transient
samples as well as the 155 converged samples; it does not discard stale outputs.

All six fields converge in all six phases across the two passes. Fault text appears
on the status/fault sources, then clears after resume. The station is deliberately
outside its eight-block operating range: occupancy, restraints, phase and fault
remain informative, while dispatch consistently reports `Vehicle outside station
range`. Its boarding status consequently stays `Boarding` when the winch reports
`Ready to dispatch`.

The observed interval from the last old-phase report to the first stable, matching
set of ten signs is 16–30 server ticks. These conservative sample brackets include
report cadence, caching and passive Display Link refresh. They are not a universal
latency bound or instantaneous readiness guarantee.

Forty indexed screenshots were visually reviewed: ten stages on both clients in
both cycles. All show complete ride text, correct phase and closed restraints;
fault views show the retained fault after power restoration, resumed views clear it,
and final boarding views restore readiness. The window is 854 × 480 at GUI scale 2,
on owned 1280 × 720 hidden Xvfb displays with software OpenGL. Original root captures
are retained. The five-second `FD_STATION` diagnostic cadence misses some brief
RETURNING/CAPTURING intervals on Observer; the corresponding screenshots provide
visual evidence, rather than claiming every phase appears in both diagnostic logs.

- [Power restored, fault retained](pass2/power_restored_latched-dev.png).
- [Returning on Observer](pass2/returning-observer.png).
- [Final boarding on Dev](pass2/boarding-dev.png).

After unloading, both clients also view the distant station. Removing its fixture
binding gives `No bound vehicle` on both goggles and all six signs. Rebinding restores
0 / 16, open restraints, boarding phase, the range denial and an empty fault field.
Four station screenshots and `station-binding.json` retain this check. The brief
unbound HUD interval is visually captured and can fall between diagnostic log samples.
Binding NBT changes test availability propagation; they do not certify the binding
item interaction, missing-provider behavior or optional-mod removal.

## Reproduce and audit

The retained setup, client/server launch and per-pass harness scripts describe the
exact commands, coordinates, source IDs and sampling procedure. They contain this
run's absolute paths and expect the existing fixture world; they are evidence of the
run, not a fresh-world installer. The first harness preflight failed because setting
an already-identical Display Link block did not replace its old configuration. It
stopped before dispatch. The corrected setup explicitly merged the intended source
and target and read back each source ID. `preflight-existing-link` preserves that
failure; it is excluded from accepted cycle results.

Run the independent read-only audit from the repository root:

```sh
python3 scripts/verify-create-live-phases.py docs/evidence/integrations/2026-10-01/create-live-phases
```

`verification.json` records its successful output. `manifest.json` records source
commit 752040d, the immutable packaged JAR SHA-256, pinned versions and evidence hashes.
Logs and full samples are gzip-compressed. Human-readable command transcripts trim
trailing whitespace; their original bytes are retained alongside them. Runtime
passwords and whole server configuration files are excluded.

Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10-280, Ponder 1.0.82+mc1.21.1,
Sable 2.0.3, Coasters Simulated 0.1.5, Fourth Dimension 0.9.0-dev.1, Java 21.
Combined addons: Engineered 1.1.1, Track Styles 1.2.0, Additions 0.2.0,
Magnetized 1.1.0, Immersion 0.2.3, Aeronautics 1.3.2, Interiors 0.6.1v3.
This uses the preceding checkpoint's built JAR; no fresh build, unit suite or native
GameTest suite is claimed for this evidence-only change.

Both clients disconnect normally, quit, and exit zero. The dedicated server saves
and exits zero. All owned process/display handles are gone, and original runtime
settings are restored byte-for-byte. Full logs retain existing loader/dist-cleaner,
resource and offline local-server warnings; successful startup is not a warning-free
startup claim. No queued-packet exception, null-pointer exception, peer reset,
missing-body or unknown-passenger diagnostic appears in the client logs.

This closes the sampled moving/faulted PAX telemetry gap for the pinned combined
pack. It does not complete other family/addon combinations, client late joining,
restart/chunk interruption, other fault causes, adverse networks, alternative Display
Link targets or the full FD901–FD904 certification matrix.
