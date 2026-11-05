# PAX long-hold source observation — 2026-11-04

The retained [source-server log](server.log.gz) reports:

```text
FD_PAX_SUPPORT_RECOVERY repaired=true realigned=true recovered=true relaunched=true riders=16 unsupportedTicks=4800
```

It then records normal server save/shutdown and `BUILD SUCCESSFUL in 5m 58s`.
This supersedes the statement that no later successful source hold was observed.
It does not erase the earlier OUTBOUND link-loss failure, certify every shuttle
phase, or establish normal connected-client operation.

The source directory associated with the run is labeled `229e586`. On review,
its `src/main`, build scripts, Gradle wrapper and properties match committed
`229e586b7e3f0f7140621da0bd5b219543c6e758` byte for byte. That revision releases
temporary support holds after physical realignment so captured carriers can
settle. The later packaged [source-correspondence JAR](../fd401-pax-packaged-longhold-229e586/candidate.jar)
has SHA-256 `94c17e5040f13dfab9adebfe5b57b1687db875d8501bfd88a2eb0b31dcfb0828`.
It is retained for source/artifact comparison, not asserted to be the binary
loaded by this development-source server: a launch-time binary manifest was
not retained. The source export omitted standalone unit tests; this observation
does not claim a unit-test run.

[observation.json](observation.json) records these identities and provenance
limits. [SHA256SUMS](SHA256SUMS) protects the archived source log and observation record; the packaged bundle separately protects its JAR.
The original ignored `server.log` remains untouched.

The later [packaged named gate](../fd401-pax-packaged-longhold-229e586/README.md)
passes the same 4,800-tick test on its retained JAR. Its connected client only
joined the station; it did not perform the recovery sequence. The stopped gate
world later faults after fixture chunk release, and the separate stationary
BOARDING restart is not interrupted occupied recovery evidence.

The [current qualification record](../../../CURRENT-QUALIFICATION.md) assigns
the remaining acceptance to FD902C, shared with FD401D: perform a normal-client
operator replay of the 4,800-tick LIFTING support-loss hold; retain every original car,
carrier, native link and rider; perform normal Repair → Realign → Recover;
complete the occupied powered cycle and unload; and cold-reopen the retained
state without automatic motion or a lost/duplicated rider. Keep the historical
OUTBOUND failure as a separate required regression. None of these packaged or
cold-restart claims follows from the source log alone.
