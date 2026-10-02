# Evidence toward the 0.9 candidate

[Recovery schema migration, dev.27](recovery-schema-migration.md) verifies the
supported schema-1–5 loader contract for occupied five-family recovery records.
It is a native regression, not packaged historic-world or provider-conversion
acceptance.

[World upgrade and recovery procedure](../../MIGRATION.md) gives the supported
backup, staged-upgrade and restore steps. It does not convert third-party
functional blocks or establish the remaining FD903 acceptance evidence.

[Idle shuttle availability, dev.13](idle-shuttle-acceptance.md) records the native/control failure, production fix, packaged loading-boundary comparison and default-flags cold reopen. Its scope is an empty bound shuttle before its first lift. The four inherited PAX faults remain recorded; complete FD902 and earlier 0.4 gates stay open.

Run `python3 scripts/verify-idle-shuttle-checkpoint.py` to verify the committed observations. This reads evidence; it does not replay clients or certify the complete lifecycle matrix.
