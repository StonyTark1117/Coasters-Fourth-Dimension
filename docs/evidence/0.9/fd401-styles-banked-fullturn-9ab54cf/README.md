# FD401 occupied banked and flat full turns with both Styles appearances

On 2026-11-04, an isolated Styles-only packaged client ran the named
`nativebankedframesandloadedfullturns` GameTest from source `9ab54cf`.
The [archived JAR](coasters-fourth-dimension-0.9.0-dev.27.jar) has SHA-256
`2d4ecc048875af48bb21a18e5ff1179d5a49f767ed45055a4477aa3b76260fb8`.
The invocation used `-PfdPackaged -PfdValidation -PfdIntegrations=styles`,
an isolated output tree, and `JAVA_TOOL_OPTIONS` enabling the
`fourthdimension` GameTest namespace. The [raw client log](client.log.gz),
[pass screenshot](pass-chat.png), [native frame export](bank-native-frames.json),
[stopped world](world.tar.gz), and [hash inventory](SHA256SUMS) retain the run.
The client saved and quit normally; Gradle exited 0.

The existing native eight-section banked Arrow circuit kept its real chassis,
carriers, station, redstone capture, restraints and mounted pig rider. This
focused fixture now assigned installed `arrow_4d` to the 30-degree banked
full-turn section and `arrow_4d_dbl_spine` to the flat full-turn section, with
Original appearance on the other six sections. Both appearances supported the
saved 360-degree profiles, and the state was broadcast to the client before
dispatch. Style assignment in this case was a **server fixture**, not a normal
player UI action.

The native frame export retained zero centerline gap, tangent mismatch
`0.0001735`, up mismatch `0.0000841`, and maximum bank `30.0000008` degrees.
With the rider mounted, the car traversed both styled sections and the measured
peak carrier angles reached `6.2718758` and `6.2811270` radians, each within
the test's `0.08`-radian full-turn tolerance of `2π`. The station captured the
car, the rider unloaded, and the test passed after about 85 seconds. The log
records both appearance identifiers, `fullTurnSections=2`, both peaks, capture,
unload and `All required tests passed :)`.

The client reopened the prior Styles-only remote-controller world, originally
copied from a combined-profile save. Absent Engineered and Crafts & Additions
blocks in that world generated recoverable missing registry warnings; the
banked circuit was newly created at `(-350,210,-200)`, away from those blocks.
The world was opened to LAN to grant the `/test` command. The later spectator
mode and failed teleport command happened after the named test had passed and
did not affect its circuit or result.

This establishes an occupied native full-turn route carrying both Styles
appearances, including whole-section Original/Styles boundaries and native
capture/unload. The client did not view the remote circuit during the ride,
so it does not prove visible decoration, component ownership or client
rendering through inversion. It also does not cover a coupled train, normal
player style selection on this circuit, partway-along-section style changes,
or continuous between-tick swept clearance.
