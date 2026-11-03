# Availability regression: fixture setup failure

Source `6d6367cfe41d73c1f7f56083665a65205d464875`, Engineered-only development
profile, Minecraft 1.21.1 / NeoForge 21.1.248 / Sable 2.0.3 / Coasters 0.1.5 /
Engineered 1.1.1. This run did **not** exercise the availability regression and
provides no evidence of a motor-guard defect or fix.

The fixture used X=-1200. `TrackPlacement.checkEndpoint` rejected it before native
cars or motor probes were created because each endpoint's absolute X/Z must be
at most 1024. All 51 GameTests completed; this fixture was the sole required
failure. The server saved and stopped normally; Gradle returned exit 1.

Command: `./gradlew runGameTestServer --offline --no-daemon -PfdIntegrations=engineered`.
The isolated checkout and output paths, source identity, initial clean state,
JAR digest and guard/test class parity are in `preflight.json` and `metadata.json`.
The run loads development classes, not the retained packaged JAR. Start time is
the fresh redirected log's filesystem birth time immediately before Gradle;
end time was recorded after the execution session returned exit 1.

Both full logs and the matching baseline JAR are retained here. The capture
manifest records successful preservation of all five native exports under
`/tmp/fd-engineered-availability-6d6367c/native-exports/`; those large auxiliary
payloads are local only and are not part of this Git evidence bundle. They are
not being promoted as separate geometry or release acceptance evidence.

The next attempt moves the fixture to (-960,120,-960), updates its forced-chunk
bounds, and retains the same interlock assertions. No production code changed.
A corrected baseline must demonstrate the actual native behavior before a
production hold-policy change is justified. Natural chunk cycling, occupied
passenger restoration, GUI operation and complete FD401 acceptance remain open.
