# External route/operator seam integrated on current main

2026-11-26. Primary checkout, branch `main`, baseline
`6f482d9d69e0b613eed5b1a8c30b5f818fbf9436`. Selectively applied overlay
`9f0bbf479576c988f32b6930432f94f3bb3a30a1` (parent `dc1c83a`).
The earlier family contract is already integrated; no older base files replaced
current production files.

## Reconciliation

The only textual conflict was the `validateExternal` signature beside the newer
specialty restriction. Both are retained: route validation receives controller
position, while specialty vehicles still require native Chain propulsion.
Current moving retention/drive denial, MovingRouteContext routing, moving-service
editor restrictions and specialty recovery actions remain intact. External editor
callback routing additionally requires no moving retention, so a retained train's
Stop still uses the established native capture path. Moving-track support for
Launch Systems is not added.

External route providers opt in explicitly; native route state owns admission and
reservations for the unchanged eight-segment/six-block/three-train Arrow contract.
The operator panel forwards fresh provider request revisions while retaining player
access, vehicle revision, controller range and identity checks.

## Focused checks and limits

Java launcher and Gradle JVM both Java21. The combined focused run passed **3/3**:
external family authority, bounded native Arrow route, and shared operator panel.
[Raw output](native.txt) includes the three PASS markers and clean server shutdown.
[Gradle JVM](gradle-version.txt) confirms the selected runtime.

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --offline --no-daemon --max-workers=2 \
  --project-cache-dir /mnt/NVME4/fourthdimension-runs/fd-ls-route-main-20261126/project-cache \
  -PfdBuildDirectory=/mnt/NVME4/fourthdimension-runs/fd-ls-route-main-20261126/build \
  -PfdGameTestNamespaces=fourthdimension_ls004,fourthdimension_ls006,fourthdimension_ls602 runGameTestServer
```

These are native seam checks. They do not establish consuming Launch Systems
LS601/LS602 completion, occupied operation, multi-train interaction, or cold restart.
The [overlay evidence](../launch-systems-route-20261126/README.md) and LS-consumed
artifact SHA256 `97772d1d9cba7a843ecb845a442841fe0d553edacd1dae951aef8e1777c8e010`
remain unchanged; this source integration does not replace that pinned artifact.
Published 1.5.0 artifacts/tags remain unchanged. No broad qualification was repeated.

Server saved all dimensions and exited successfully; process inspection found no
Java runtime using this run root. No GUI client/display/service was created.
Historical worlds, worktrees, raw logs and pre-existing untracked `logs/` remain.
