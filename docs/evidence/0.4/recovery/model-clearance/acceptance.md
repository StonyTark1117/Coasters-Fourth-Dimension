# FD402 sampled default model clearance during occupied banked recovery

Dev.21 advances the active recovery package in the 0.4 stage toward 0.9.0.
It records actual native chassis, carrier and ordinary-bearing logical poses,
body UUIDs, block offsets and block states every twenty test ticks during the
existing occupied banked recovery cases. The five single-car families and the
original two-car Arrow/four-car PAX trains remain subject to normal towing,
manual braking, external power cut, stationary disconnection and native unloading.
The trace is read-only: it adds no motion, force, teleport, replacement body or
persistent gameplay state.

## Independent default-model check

`audit-recovery-mesh-trace.py` reads the trace, native route frames, our original
block models and separately installed pinned Minecraft/Coasters JAR resources.
External model data stays in memory; committed evidence contains resource hashes
and our native pose/block-state observations, without copied external meshes.

The checker reconstructs the original renderer's four-panels-per-block octagonal
running/control/spine tubes, crossbars, running mounts and Arrow control mounts.
The renderer, dimensions and original recovery examples are source-pinned;
changes require geometry reconstruction to be reviewed again. The recorded
examples have Original appearance and no magnetic spans. It resolves each actual
blockstate's default model and inherited Minecraft cube geometry, including
closed restraints and seat facing. Each model element's entire filled cuboid is
transformed using the recorded body's actual origin and axes. These conservative
volumes include faces that may be culled during normal rendering.

Two-block spatial bins exclude pairs whose world AABBs are separated by at least
four times the required margin. Remaining pairs use world AABB lower bounds,
refined near the margin by oriented-box/triangle separating-axis bounds.
Volumes belonging to different original cars, including the ordinary recovery
body, are also compared. Contacts within one car include intentional strut/hinge
joints and are outside the neighboring-car check. A zero separating bound means
uncertified; it is never interpreted as safe or automatically called a real
mesh collision.

The required gap is 0.02 blocks. Track separation reports are conservatively
capped at 0.08 blocks, so a reported 0.08 proves at least that bound and is not an
exact measurement of the closest surface. Recorded resource/source hashes and
per-route reports accompany each profile. The checker also requires unique,
stable original body IDs, every original chassis visiting all five sections,
at least 150 samples, consecutive twenty-tick intervals, finite orthonormal
poses, matching car/carrier counts and the normal-completion footer.

Offline negative controls move one recorded carrier model into a running rail
and remove a middle pose sample. Both are rejected. Contained/separated geometry
controls retain their expected result after a rigid 30-degree rotation. These
controls change verifier input only, without altering native body motion.

## Reproduction and evidence

Use Java 21 for `./gradlew build runGameTestServer --no-daemon`; add
`-PfdIntegrations=styles,engineered` for the combined profile. Do not use `clean`
on valuable client worlds. The test task resets its own disposable world and
previous trace exports. The CI matrix checks standalone, Styles, Engineered and
combined profiles and retains both traces, route frames and model reports.

Install `scripts/requirements-geometry.txt` in a Python environment (Python 3.12
or newer), then run:

```sh
python3 scripts/audit-recovery-mesh-trace.py BASE_COASTERS_JAR MINECRAFT_CLIENT_JAR \
  docs/evidence/0.4/recovery/model-clearance/standalone-train-frames.json.gz \
  docs/evidence/0.4/recovery/model-clearance/standalone-train-trace.jsonl.gz
```

Repeat with `single` instead of `train`, and with the combined profile's files.
Compressed native inputs preserve the exact raw observations; both compressed
and raw hashes are recorded. The pinned base artifact is Coasters 0.1.5 and
Minecraft resources are the official 1.21.1 client. NumPy is pinned for the
independent checker; the Python 3.12 Linux wheel was verified available.
Dev.20's twelve passed CI jobs remain pinned in the
[original train checkpoint](../banked-trains/ci-dev20.json).

## Final local results

Standalone and combined Track Styles/Engineered profiles each passed all **37
required native tests and 25 unit tests** on the same dev.21 artifact. Both
native route-frame checks and both sampled block-model clearance reports pass
in each profile. The [standalone record](standalone-results.json) and
[combined record](combined-results.json) pin source manifests, JAR/log hashes
and raw/compressed native inputs.

JAR SHA-256: `89045c47da6f13f1125d475ae9e4a71496c1bdaa3f7b0154477fed5ebb6b6d96`.
Source manifest SHA-256: `ea74b2bd457976ee4f703e3dab39778b6f33b6c4c78db150e9c5b42d59375b84`.
No dependency JARs are bundled. Source/resource hashes and every audit route's
metrics are recorded in the four `*-clearance.json` reports.

Every track/model check certifies the capped **0.08-block lower bound**, above
the required 0.02. Standalone single-car samples are Arrow 288, Legacy 292,
Free Spin 293, Revised 293 and PAX 288; original-train samples are Arrow 328
and PAX 349. Every original chassis visits all five sections. The smallest
standalone neighboring-model separating bound is **3.32359 blocks** on the
two-car Arrow train. These bounds apply only to the modeled components and
sampled poses, not the omitted special rendering or continuous motion.

[Negative controls](negative-controls.json) use the final standalone train
trace and pin its compressed input and verifier hashes. Current-source CI
runs the new audit in all four profiles; CI acceptance is recorded separately
once its exact-head jobs finish.

## Remaining FD402 acceptance

This is sampled default block-model clearance on the actual retained occupied
recovery fixtures, including independent carrier poses and neighboring original
cars. Samples begin after native recovery starts and end before disconnection;
they do not cover every continuous position, all possible routes or all car
orientations. Twenty-tick samples are not a continuous swept-clearance proof.

Rotating native bearing wheel rendering, running-wheel contact behavior,
rendered player meshes/animation and safe exits, Arrow's mechanical linkage,
optional decoration, resource overrides, complete rendered operator workflows,
Engineered recovery FE operation, general grade/coupler ratings and full
multiplayer/fresh-JVM lifecycle acceptance remain open. The loaded compact PAX
spike propulsion limitation is unchanged. FD402, FD404 and the 0.9 gate remain
open; the next clearance work must include these omitted rendered components
and continuous motion before full clearance can be accepted.
