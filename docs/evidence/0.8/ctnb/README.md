# FD811 CTNB — work in progress

Owner: lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`, primary checkout `main`.
This is an implementation/native checkpoint, not FD811 or FD813 closure.
Connected-client construction/operation and installed combined conflicts remain.

Selected artifact: CTNB1.2 file8718286,
SHA256 `e989c33e305779e37d4cd61bd4a7a13700faa45a604bd4aa92eacad738145115`.
Core versions unchanged. [Guide](../../../CTNB.md) enumerates seven materials,
Wing/Arrow scope and manual validated station/reverse release semantics.

`geometry-audit.json` compares 28 selected rail, beam and tie OBJ vertex lists
against native Coasters0.1.5. Every ordered vertex matches. Runtime fingerprinting
also includes their model JSON and the relevant addon control/render/alias classes.
This geometry comparison does not claim a player or native-physics run.

Native runs use Java21, `-PfdIntegrations=ctnb`, one named namespace at a time,
under `/mnt/NVME4/fourthdimension-runs/fd08-operations-20261115/ctnb-build`.
Saved worlds are retained outside that build directory with their run names.

- `ctnb-native-01.log` rejected the adapter's unsafe-material test. Diagnostic02
  established the mixin was installed and the direct guard itself accepted the
  material. CTNB aliases **all** `*_track` resource IDs to native Coasters in
  `ResourceLocation.equals`. Exact string IDs fix this without removing the addon.
- `ctnb-native-03.log`: 1/1 passed. Actual Chain entry and optional callback fence,
  bounded launch impulse, explicit reverse, station dwell/redstone hold, sensor
  reporting, signal-loss latch, restored-power denial, occupied repaint rejection,
  exact Arrow profile preservation and unknown/angular material rejection.
  Fixture material and velocity changes are disclosed invariant inputs.
- `ctnb-joins-01.log` exposed native graph edges retaining their previous material
  after repaint. The car had released authority but stayed on the initial span.
  Eligibility/drive now read live anchor material without changing route geometry.
- `ctnb-joins-02.log`: 1/1 passed. An actual native Wing body travelled from CTNB
  Boost through a native segment to CTNB Sensor, reaching approximately
  `(1800,100.625,677.5)` and activating its native sensor. An unconnected crossing
  was rejected. No direct velocity write drives this transition check.

`ctnb-native-04.log` repeats only the affected control case after live-material
lookup changed: 1/1 passed. All native server processes exited cleanly.

Client resources are separately reserved at
`/mnt/NVME4/fourthdimension-runs/fd08-ctnb-20261115`: display316, loopback game/RCON
25650/25651, prefix `fd08-ctnb-20261115`, 1536M Java heap/6GiB client scope.
No client acceptance or final cleanup is claimed by this checkpoint.
