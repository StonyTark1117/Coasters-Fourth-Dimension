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
closed restraints across all five families. Live late-join is covered
separately; occupied clients, disconnect and full lifecycle coverage remain
FD902 work.

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
Legacy, Free Spin and Revised also restore one rider while the other stays
mounted. Moving-route coverage is recorded for Revised; concurrent interruption
coverage for PAX remains FD902 work.

[Ordinary-cart native snap matrix, dev.27](ordinary-cart-snap-matrix.md)
verifies fitting and deliberately non-fitting ordinary bearings on all five
original tracks through direct and native-filter entry paths. It also rejects
an unassigned ordinary/4D train coupling without mutating the 4D car; guarded
recovery remains the sole mixed-consist path. It is a bounded FD901 checkpoint,
not the complete compatibility matrix.

[Client track-render instrumentation](client-render-sampling.md) records an
initial packaged real-client sample on the recovery scene, under llvmpipe and
on the stated RX 7900 XTX host, plus a native-GPU dedicated-server observation
with a real Track Styles-decorated Arrow rail. It does not claim FPS or shader
coverage; those measurements remain FD904 work.

[Idle shuttle availability, dev.13](idle-shuttle-acceptance.md) records the native/control failure, production fix, packaged loading-boundary comparison and default-flags cold reopen. Its scope is an empty bound shuttle before its first lift. The four inherited PAX faults remain recorded; complete FD902 and earlier 0.4 gates stay open.

Run `python3 scripts/verify-idle-shuttle-checkpoint.py` to verify the committed observations. This reads evidence; it does not replay clients or certify the complete lifecycle matrix.
