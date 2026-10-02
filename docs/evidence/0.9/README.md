# Evidence toward the 0.9 candidate

[Recovery schema migration, dev.27](recovery-schema-migration.md) verifies the
supported schema-1–5 loader contract for occupied five-family recovery records.
It also pins the recorded 0.3 schema-3 world extract as a current-loader,
schema-6-reopen regression. This remains below packaged historic-world or
provider-conversion acceptance.

[Appearance provider removal](appearance-provider-lifecycle.md) combines a
recorded save/remove/reinstall cycle with a current-schema absent-provider
GameTest. It verifies cosmetic-provider loss does not alter a functional rail,
but does not establish external functional-track conversion.

[World upgrade and recovery procedure](../../MIGRATION.md) gives the supported
backup, staged-upgrade and restore steps. It does not convert third-party
functional blocks or establish the remaining FD903 acceptance evidence.

[Five-family support loss, dev.27](five-family-support-loss.md) verifies that
an occupied native car fails closed when its only matching track connection is
removed. It preserves rider ownership, matching binding, carrier capture, and
closed restraints across all five families, then permanently removes one
carrier from every family and verifies rider recovery with no retained native
or saved vehicle state. Live late-join is covered
separately; occupied clients, disconnect and full lifecycle coverage remain
FD902 work.

[Repeated PAX shuttle station reload](repeated-shuttle-reload.md) saves and
reinstalls the real winch at its first captured station return, then verifies
that its physical carriage and ordered native consist complete a second cycle.
It is a server-side persistence boundary, with wider client and lifecycle
coverage still open under FD902.

[Dimension-scoped snapshot contract](dimension-snapshot-contract.md) prevents
a queued old-dimension snapshot from replacing the newly active client level.
It advances the snapshot lifecycle boundary. The separate live two-client
late-join check is complete; disconnect, chunk-unload and dimension-transition
replay acceptance remain FD902 work.

[Two-client late-join snapshot replication](two-client-snapshot.md) records a
dedicated-server recovery-world run where a second client joined after the
first and independently received the same six 4D body snapshots. The separate
[occupied five-family disconnect/reconnect check](occupied-disconnect-reconnect.md)
uses fresh client processes and confirms an assigned rider returns to the
intended carrier for every family. It also records server-process restarts and
normal holding-state unload/reload for every family. Clean-world two-client
fixtures retain distinct same-carrier seats across all five families; Arrow,
Legacy, Free Spin, Revised and PAX also restore one rider while the other stays
mounted. Moving-route coverage and one-rider reconnect while the other remains
aboard are recorded for Arrow, Legacy, Free Spin and Revised and during PAX's active powered lift; concurrent interruption coverage is complete
for all five stationary families.

[Ordinary-cart native snap matrix, dev.27](ordinary-cart-snap-matrix.md)
verifies fitting and deliberately non-fitting ordinary bearings on all five
original tracks through direct and native-filter entry paths. It also rejects
an unassigned ordinary/4D train coupling without mutating the 4D car; guarded
recovery remains the sole mixed-consist path. It is a bounded FD901 checkpoint,
not the complete compatibility matrix.

[Family eligibility matrix, dev.27](family-eligibility-matrix.md) verifies
all five original 4D cart families across the five original functional tracks
through construction, locked eligibility and native initial snap. It records
all 20 wrong-family rejections and preserves the prior matching binding. It is
still bounded to original functional tracks and does not certify external
providers or every real-player entry path.

[Advertised compatibility scope](supported-compatibility-scope.md) names every
currently supported standalone and optional profile, and explicitly excludes
third-party functional tracks until a separately verified adapter is released.

[Packaged client carrier transforms](client-carrier-transform-sampling.md)
records a real-client snapshot/native-body orientation check for the six loaded
carriers in the Arrow-only `FDRenderMatrix` fixture. It is deliberately scoped:
five-family, rider, motion, camera and rendered-clearance coverage remain open.

[Five-family packaged client carrier transforms](client-carrier-transform-five-family.md)
adds a fresh-world static client check for Arrow, Legacy, Free Spin, Revised and
PAX: 20 carrier bodies stayed within the 0.02-radian gate for 40 consecutive
samples. Three early Sable sub-level packet-order errors preceded convergence,
so it records eventual transform agreement rather than clean startup or wider
lifecycle coverage. Riders, motion, camera, rendering and multiplayer remain
FD902/FD904 work.

[Five-family packaged client carrier motion](client-carrier-transform-five-family-motion.md)
uses normal production dispatch for the four circuit cars and the assembled PAX
winch after the five-family static matrix has passed. The client completed 40
pre-dispatch 20-carrier samples within the 0.02-radian gate, then observed at
least 1.02 blocks of client-native chassis motion for every family, including all
four PAX chassis. Rider, camera, interpolation, multiplayer and rendered-clearance
coverage remain open.

[Client track-render instrumentation](client-render-sampling.md) records an
initial packaged real-client sample on the recovery scene, under llvmpipe and
on the stated RX 7900 XTX host, plus a native-GPU dedicated-server observation
with a real Track Styles-decorated Arrow rail. It does not claim FPS or shader
coverage; those measurements remain FD904 work.

[Five-family fixture render checkpoint](client-render-five-family-fixture.md)
records 101 packaged llvmpipe client stages in the fresh Arrow, Legacy, Free
Spin, Revised, and PAX fixture. It covers sustained original-track submission
with more than 33 in-range rails per stage on average; the fixed camera and
metrics do not assert full rail, vehicle, rider, clearance, shader, or FPS
coverage.

[Packaged rendered recovery capture](rendered-client-fd16.md) preserves a
HUD-free framebuffer from the actual `FD16Recovery` client world after the
camera was turned toward native vehicle, rail and support geometry. It is a
single llvmpipe visual observation, not full render or clearance acceptance.

[Idle shuttle availability, dev.13](idle-shuttle-acceptance.md) records the native/control failure, production fix, packaged loading-boundary comparison and default-flags cold reopen. Its scope is an empty bound shuttle before its first lift. The four inherited PAX faults remain recorded; complete FD902 and earlier 0.4 gates stay open.

Run `python3 scripts/verify-idle-shuttle-checkpoint.py` to verify the committed observations. This reads evidence; it does not replay clients or certify the complete lifecycle matrix.
