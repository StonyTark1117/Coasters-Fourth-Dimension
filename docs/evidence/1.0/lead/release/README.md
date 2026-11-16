# FD1002 — reproducible1.0.0 package and clean installation

Complete2026-11-16 UTC. Source **`0e2be865d13d1c968ca64c634a5c26aac5a3bf9e`**.
Artifact **`coasters-fourth-dimension-1.0.0.jar`**, SHA-256
**`0192b8150460924ebf57ee2d0352472f21cfe9685f6081c556eeedd963743837`**.
Persistent deliverable: `/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/release-1.0.0/`.
[Checksums](SHA256SUMS), [reproducibility](reproducibility.json), [package inspection](package-check.json).
No publication, release tag or upload occurred.

## Packaging

Two independent fresh build directories ran Java21 Gradle9.2.1 `jar --no-build-cache`
and produced byte-identical JARs. [Build A](build-release-a.log.gz) and
[build B](build-release-b.log.gz) each executed all4 tasks. Compared with qualified
candidate17, the only changed ZIP entry is `META-INF/neoforge.mods.toml` (version).
All production class and asset bytes are identical. The [verifier](verify-release-package.py)
checked603 source resources with the intended metadata expansion, including489
unchanged accepted FD917 assets/authoritative exports, MIT license and intended
content boundaries. No optional binaries, foreign art, authoring sources or run
data are bundled. The skin-provider inventory is intentional metadata.
All qualified family/profile, lifecycle, camera and performance claims therefore
carry forward from [FD905](../release-candidate.md) at their real historical identities.

## Clean production installation

The official NeoForge21.1.256 installer populated separate new client and dedicated
server directories; [installer source identity](source.json),
[client log](install-client.log.gz), [server log](install-server.log.gz), and
[launcher preparation](prepare-client-launch.py) retain the procedure. Launch uses
installed production BootstrapLauncher/libraries, not ModDev's development classpath.
The new mods folders contain only the1.0.0 JAR and documented upstream Create
6.0.10-280, Coasters0.1.5 and Sable2.0.6 distributions. Their bundled supporting
libraries remain upstream Jar-in-Jar dependencies; [exact distribution hashes](core-mods.json)
are recorded. No optional providers are installed, exercising standalone fallback.

Owner: lead `01a1dd1d-1468-7f81-8f31-5532ad7a178d`; private host Xvfb`:324`,
loopback25666/RCON25667, unique client/server/world and units in [run record](run.json).
Java21, native RX7900XTX VirtualGL,1280×720, Fancy/render8/simulation6;
client1536M/6GiB, server2048M/4GiB. [Preflight](preflight.json) checked30GiB host
headroom, unlimited enclosing cgroup and47GiB persistent disk. [Live scope evidence](live-resources.json)
shows client peak2.49GB/server1.37GB, zero max/OOM events. This smoke run is not a
replacement park benchmark and its settings do not alter accepted budgets.

Both normal production installs loaded Fourth Dimension1.0.0. The client joined
at23:41:55 local and remained connected until normal Disconnect. [Console readbacks](parkctl.jsonl),
[input records](input.jsonl), [client log](client-latest.log.gz) and
[server log](server-latest.log.gz) preserve the actual sequence:

- New-run input preflight retained default buttons/raw input. Relative XTest device
  motion changed yaw−90→−71.85; a calculated setup aim hit reachable stone at2,100,0.
  One actual right-click [placed stone](input-place.png) at2,101,0, verified server-side.
- Disclosed `/fd demo arrow` built the representative native-chain circuit;
  `fd validation board Dev` used the normal seat API for fixture mounting. These
  setup helpers are not normal construction or boarding acceptance; existing
  accepted player workflows carry forward unchanged.
- Actual Rotation Tool use opened the [vehicle editor](editor-ready.png). One
  Close input produced [closed restraints](editor-closed.png); Dispatch started
  native motion. Readbacks traverse(0.5,104.1,32.5)→(0.5,104.1,50.33)→
  (32.5,104.1,31.04)→(8.325,104.1,12.5)→(0.5,104.1,42.01), covering a complete
  return circuit with the same occupied chassis. Native velocity reached1.994m/s.
  Existing Arrow renderer diagnostics also record actual angle range approximately0–2π.
- Actual Capture stopped/aligned the car; [Ready/closed](editor-captured.png) and
  `capture=true` with velocity below0.001m/s confirm completion. The first Open
  was [correctly refused as stale revision](stale-revision-refused.png) after
  capture changed state. Inspection of EditorNetwork's revision guard and a fresh
  editor snapshot corrected the precondition; one subsequent Open yielded
  [Ready/open](editor-open.png). No guard was bypassed or click sequence repeated blindly.
- Actual Shift [dismounted normally](normal-unload.png); server `RootVehicle` was
  absent. Disconnect left zero players; Quit Game exited the client. `save-all flush`
  and `stop` saved all dimensions/native sublevels. [Cleanup](cleanup.json) verifies
  client/server/Xvfb inactive with MainPID0; the saved world remains on persistent disk.

The logs retain absent optional-target mixin warnings, unavailable host narrator
`flite`, vanilla empty flat-generator fallback, and three early Sable movement
packets before fixture sublevels appeared. The subsequent successful load, native
ride, synchronized editor, rendered geometry and saved shutdown resolve this
installation claim; none is a continuing crash, missing required asset or rider
failure. No universal zero-warning claim is made. No further runtime run is required.

[Evidence manifest](manifest.json) preserves hashes and external paths; full input
before/after screenshots and launch arguments remain with the saved run. Server
credentials are intentionally not included in this source evidence package.
