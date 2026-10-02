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

## Current dependency check

The pinned Create **6.0.10** is the current Minecraft 1.21.1 release. The
upstream Create tracker has an open [issue 9422](https://github.com/Creators-of-Create/Create/issues/9422)
reporting the same class of post-reload failure: a `BitmapProvider` later tries
to upload a closed native image while a menu or other GUI renders. That report
first reproduced with Create 6.0.4 and Curios, and can also occur in larger
Create mod sets; its dependency set is therefore evidence of a matching upstream
failure mode, not proof that this addon's provider integration causes the
recorded dev.8 crash.

The exact 1.21.1 `FontManager.apply` source used by this project clears
`lastFontSetCache` before closing old font sets and providers. The temporary
diagnostic likewise found no stale cached `FontSet` in its three reloads. There
is no published upstream fix or newer compatible Create release to adopt at
this checkpoint. Fourth Dimension intentionally does not alter FontManager,
NativeImage, or bitmap-provider lifecycle without a reproduction that proves
such an intervention correct. The next useful acceptance evidence is a
reproduction with provider/image identity traces or a clean, repeated packaged
post-reload styled ride; until then the crash remains a release-blocking FD401,
FD902, and FD904 investigation.

The production artifact now exposes those traces only with `-PfdFontValidation`.
It records FontManager apply generations, closed FontSet identities, and an
`FD_FONT_IMAGE_UNALLOCATED` line with the active font/codepoint context if
Minecraft is about to upload an already-closed native image. The trace does not
clear a cache, catch the exception, retain the image, or otherwise change the
font lifecycle; a successful build only verifies that the diagnostic is
packaged, not that the intermittent crash has been reproduced or fixed.

An isolated Xvfb startup on 2026-10-01 ran `runClient -PfdFontValidation` with
the pinned standalone dependency profile. All three trace mixins applied and
the first resource load recorded `FD_FONT_APPLY_START generation=1
oldFontSets=0` followed by `FD_FONT_APPLY_END generation=1 newFontSets=7`.
There was no Fourth Dimension injection failure. The process was deliberately
stopped by its 55-second startup timeout, so this checks the mixin bindings and
initial trace only; it is neither a clean client-exit result nor a reload/crash
reproduction.

A second isolated Xvfb attempt reached the same initial trace, then sent F3+T
to the focused Minecraft window both directly and while holding F3 across the
T keypress. Neither input produced a second `FontManager.apply` generation.
The process was deliberately interrupted after the attempted input. This
headless environment therefore validates startup binding only; it cannot add
post-reload acceptance evidence. The next reproduction attempt must use an
interactive client where F3+T is known to reach the game, or another controlled
resource-reload trigger that preserves the packaged dependency set.
