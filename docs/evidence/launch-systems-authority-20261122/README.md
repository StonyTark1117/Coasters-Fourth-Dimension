# Registered external longitudinal authority

2026-11-22. Integration overlay on accepted `59c40d11`, built in the isolated
`codex/launch-systems-authority` worktree. No ongoing 1.4 source was included.

`RideAuthority.EXTERNAL_API = 1` adds registration by stable namespaced provider
id, an eligibility callback and a provider-specific controller resolver. Native
providers retain their existing block-controller path. Duplicate registration
cannot replace a resolver. A missing saved provider, failed eligibility,
unsupported managed route, missing controller or missing body retains native
braking. Explicit resume and transfer recheck eligibility; neither dispatches.
Dispatch/capture remain native operations. The new permit overload checks the
whole owner/epoch/sequence token, including changes caused by hold/capture.

Launch Systems registers `coasterslaunchsystems:local_controller` with a bounded
original-family predicate; FD's native specialties are not excluded globally.
FD's managed multi-train route is refused until its separate interoperability
contract is qualified.

## Verification

Java 21; NeoForge 21.1.256, Sable 2.0.6, Coasters 0.1.5. Targeted command:

```sh
./gradlew --offline --no-daemon --max-workers=2 \
  --project-cache-dir=/mnt/NVME4/launchsystems-runs/bridge-20261122/fd-cache \
  -PfdBuildDirectory=/mnt/NVME4/launchsystems-runs/bridge-20261122/fd-build \
  -PfdGameTestNamespaces=fourthdimension_ls004,fourthdimension_fd1701a \
  runGameTestServer jar
```

[Native log](native.log): **2/2 pass**. The unchanged native ownership fixture and
new external fixture cover claim, second-owner denial, held transfer, stale and
replayed requests, release, capture, persisted hold, explicit resume, controller
loss and replacement. The external fixture also checks duplicate registration,
sequence-aware permission, changed eligibility, unsupported resume/transfer and
null-provider denial. Its resolver throws if invoked for a native station;
the native fixture passes, proving registration does not intercept native
controllers. Each fixture uses separate geometry.

Artifact snapshot:
`/mnt/NVME4/launchsystems-runs/bridge-20261122/artifacts/fourthdimension-authority-api1.jar`
(version label remains 1.1.0), SHA-256
`df3662828a106efe6542abee735b26c7254a9a218e9c1e45783d2e979e63256e`.

The GameTest server saved all dimensions and exited. No graphical client,
display or persistent service was started for this check. Earlier preparation
on the saturated primary disk was cancelled before a server started; moving
owned build/project-cache outputs to NVME4 and using a fresh Java 21 Gradle
process resolved the local launch setup. These checks qualify the facade's
native contract; Launch Systems' consumer force/circuit and occupied-client
acceptance is recorded by its own LS004/0.1 evidence.
