# FD401 Styles selection from the middle of a native section

On 2026-11-04, the Styles-only packaged client ran the named
`cosmetictransactionsmigrationandmissingproviderpersistence` GameTest from
source `b2403d3`. The [archived JAR](coasters-fourth-dimension-0.9.0-dev.27.jar)
has SHA-256 `97e52b85733a766bb5fb979cc04e04f521d63a5040424d0214652ad62911ca81`.
The build used `-PfdPackaged -PfdValidation -PfdIntegrations=styles` and an
isolated output tree; `JAVA_TOOL_OPTIONS` enabled the `fourthdimension`
GameTest namespace. The [raw client log](client.log.gz), [pass screenshot](pass-chat.png),
[stopped world](world.tar.gz), and [hash inventory](SHA256SUMS) retain the run.
The client saved and quit normally; Gradle exited 0.

The updated case placed a native 32-block Arrow section, selected each of the
two inspected Styles appearances through the ordinary server edit transaction,
and made the edit from a sampled middle point. The fake editor was more than
eight blocks from both endpoints. A point 12 blocks laterally from that same
middle point was rejected for each appearance without changing revision.
The log records `withinCurveStyles=2`, one named test passed, and `All required
tests passed :)`. Existing checks in that same case also retained functional
family, profile and appearance intent across rejected edits, high-stroke
fallback and serialization.

The saved world was copied from the combined-profile remote-controller fixture;
its absent Engineered and Crafts & Additions blocks produced recoverable missing
registry warnings in this Styles-only client. The test rail was newly placed
well away from those blocks. The local world had to be opened to LAN to grant
commands, and two attempts with the wrong `/gametest` spelling are visible in
the raw log before the successful `/test run ...` command.

This is an edit reach and transaction check on one straight native section.
It does not show a style change partway *along* a section, a normal player UI
edit, rendered component ownership, an occupied ride, or a full-turn route.
