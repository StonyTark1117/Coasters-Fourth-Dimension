# LS601 / LS602 external route and operator seam

Owner: `/root/foundation`, coordinated with `/root` (native route/authority production) and `/root/bridge` (LS consumer). Worktree: `/home/braydon/.codex/worktrees/launch-systems-authority/Coasters: Fourth Dimension`, branch `codex/launch-systems-authority`, based on `dc1c83a543256a5ffeab6d7958e9a3656c132f74`. The FD primary checkout was not edited or built. This narrow API1 overlay adds bounded Arrow route delegation and shared operator callbacks; it does not import the primary checkout's later catalog.

## Accepted checks

- `fourthdimension_ls602.externalpanelsharesstatusandfreshrequests`: PASS. An actual native Wing and registered external barrel-controller adapter prove that the existing panel uses the registered adapter, reports the same controller UUID/charge/blocking reason, denies uncharged/stale/repeated/distant/missing-controller requests, and delegates one fresh request without directly releasing native hold. Explicit hold/resume callbacks remain separate from dispatch. This adapts the selected `ToolsAuthorityGameTests` and `ExternalRideAuthorityGameTests`; it is a seam test, not an LS-loaded or occupied-client claim.
- `fourthdimension_ls006.delegatedrouteretainsoneownerandfailsclosed`: final PASS, 1/1. The real eight-segment bare Arrow example and native articulated bodies exercise explicit route opt-in, current/next admission, exact single owner, native dispatch/capture/explicit recovery, conflicting next owner, coherently replaced stored pair/reservations, changed saved admitted-consist identity, replaced authority owner, opposing direction and an unloaded carrier. Denial retains ownership/reservations and cannot dispatch or silently repair the journal. The fixture injects the stated invalid saved/live metadata; it does not fake a positive multi-car claim.

The panel pass is in `panel-pass-and-initial-route-setup.txt` (its sibling route setup failed before production behavior). It ran before later route-only safety refinements; the panel files and callbacks' accepted request behavior are unchanged. The final route pass is in `route-final.txt`. No full family or mechanism sweep was run.

## Setup diagnosis and boundaries

The first route setup used x3072, outside ExampleLayouts' allowed ±950 construction bounds. Correcting the origin to `(0,100,512)` then exposed a missing chunk ticket: delayed admission ran far from the GameTest server origin and the barrel chunk unloaded. Inspection confirmed that ExampleLayouts itself creates no forced world chunks. The corrected fixture forces its x−1..3/z31..37 circuit region and asserts the loaded barrel before admission. The successful loaded run was followed by one focused route rerun after review added exact observed-pair and whole-consist service guards. These were setup repairs and named safety checks, not reopened sibling acceptance.

The positive route fixture has no competing native Chain drive. The adapter is deliberately simple and opt-in; LS equipment-specific physical stopping/charge/request behavior and real occupied multi-train/rejoin/restart remain the consuming Launch Systems checks.

## Reproduction and shutdown

Java21; packaged JAR; separate persistent build and project cache. From this worktree:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew runGameTestServer -PfdPackaged -PfdValidation \
  -PfdGameTestNamespaces=fourthdimension_ls602,fourthdimension_ls006 \
  -PfdBuildDirectory=/mnt/NVME4/launchsystems-runs/ls06-operator-20261126/fd-build \
  --project-cache-dir /mnt/NVME4/launchsystems-runs/ls06-operator-20261126/fd-cache \
  --no-daemon --max-workers=2
```

For the final safety refinement only `fourthdimension_ls006` was selected. `artifact.json` identifies the frozen final JAR (`97772d1d9cba7a843ecb845a442841fe0d553edacd1dae951aef8e1777c8e010`); `source.sha256` records its relevant source snapshot. The GameTest server saved every dimension and exited successfully at 2026-11-27 04:21:44 UTC. No client, display or service was created; `shutdown.json` verifies no matching Java runtime remained. Worlds and full raw logs remain in the persistent run root.

## Native readiness refinement

The consuming LS first-READY request exposed a real readiness mismatch: the native authority required five stopped ticks, while the consumer could advertise ready earlier. `RideAuthority.dispatchReady` now exposes the existing native release guards, including exact token, held/complete/stopped/safe-release/vehicle checks and `stoppedTicks >= 5`, without calling the equipment adapter recursively. LS consumes the native result; this adds no consumer delay.

The route-only selector passed again, asserting not ready immediately after claim and successfully dispatching at tick70, while preserving every accepted reservation/identity/unload guard. See [route-readiness.txt](route-readiness.txt), [artifact-readiness.json](artifact-readiness.json) and `source-readiness.sha256`. The new immutable readiness JAR supersedes 97772d for subsequent combined runs; the earlier artifact and evidence remain preserved. Existing panel/LS operator passes remain valid. Server saved all dimensions and exited successfully at 2026-11-27 04:34:29 UTC; no matching Java process remained and no display/service was created.

## Explicit operator request payload

Review found that native vehicle-editor Capture sends ordinary nonblank tuning/status text, which the first external routing condition incorrectly parsed as an operator nonce. The existing operator panel now sends an explicit `ls-operator:` prefix. Only tagged action10 routes to the external Hold callback; ordinary native Capture text retains the native path. External Dispatch/Resume still require the tagged fresh request revision.

The panel-only test passed with the actual Wing editor text `Fixed seats — ordinary Coasters track`, asserting native stopping/capture and increased native revision while external callback count/revision remain unchanged. Tagged Hold consumes its request revision and rejects replay; untagged Dispatch/Resume reject. Existing parity, charge, queue-only, range and missing-controller cases also pass. See [panel-tagged.txt](panel-tagged.txt), [artifact-tagged.json](artifact-tagged.json) and `source-tagged.sha256`. This newest immutable JAR retains the readiness fix and supersedes fd267b3d for the occupied client run. Route behavior is unchanged and was not rerun. Server saved all dimensions and exited at 2026-11-27 04:43:34 UTC; no client or display was started.
