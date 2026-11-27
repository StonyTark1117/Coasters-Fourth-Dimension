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
