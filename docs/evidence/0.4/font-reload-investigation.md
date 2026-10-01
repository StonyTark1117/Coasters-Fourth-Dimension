# Resource reload investigation after dev.8

The original packaged dev.8 crash remains unresolved. This checkpoint adds
observations, not a production font workaround or post-reload ride acceptance.

The dependency-only control omitted the Fourth Dimension JAR while retaining
the same base, Styles and Engineered dependencies. Its forty input-loop requests
produced only seventeen ResourceManager starts including startup and twenty-eight
debug notifications. Requests overlapped; those counters are not forty completed
reload/chat cycles. The final direct window capture remained on the loading
overlay. Thread observations showed the render thread in a GL draw and later in
GLFW event polling, so a permanent single-call deadlock is not established. The
owned window was closed; the client reported BadWindow/X_WarpPointer and exited
1. No target bitmap error was observed. This is not a successful control run or
proof that the addon caused the original crash. The first control also lost its
X connection and cannot establish clean acceptance.

A separate diagnostic artifact, `0.9.0-font-diag.1`, added only the observation
mixin recorded in `font-reload-diagnostic-mixin.txt`. Its Gradle init file placed
all outputs and client data in separate temporary directories. It logged the
FontManager apply boundary and checked whether a cached FontSet still matched
the manager's current map. It did not reset a cache, change a returned value or
suppress an exception. No diagnostic classes are in the production artifact.

In a closed-world copy of the banked fixture, that client completed startup and
three separate in-world F3+T reloads (generations 1–4). Each apply returned with
`stale=false`; no stale-cache lookup or target bitmap error was observed. Command
suggestions rendered after reloads one and two. After reload three, teleport
commands and an item-data query rendered correctly. The observed item was the
Rotation Tool. Attempts to use it on the anchor did not open the editor; this
run does not certify the editor interaction path. There was no occupied ride.
Normal Save and Quit, then Quit Game completed with client/Gradle exit 0. The
screenshots, selected log, source and artifact/log hashes accompany the JSON
report.

The source inspection suggests a possible lookup during FontManager's apply
window could repopulate an old cache after its initial reset. The observations
above did not demonstrate that condition. Neither that hypothesis nor the
similar upstream report is a proven cause. An unmodified production replay,
a reproduction with actionable lifecycle traces, and the revised styled
post-reload ride gate remain required.
