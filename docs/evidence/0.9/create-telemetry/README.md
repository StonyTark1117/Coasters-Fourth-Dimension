# Create telemetry native checkpoint

Source revision `177d4f652b1444652288db8a4073fc2f7ed3e6b0` restores shared ride
telemetry, Create goggles and six Display Link sources while retaining the
current support-repair and PAX guards. Schema 6 and protocol 7 are unchanged.

[CI run 41537833198](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/actions/runs/41537833198)
passed its build and all **47 required native GameTests** in each profile:
standalone, Styles, Engineered and combined. Route checks passed in all four;
decoration checks passed in the two profiles where they apply. All four jobs
subsequently reached their 25-minute limit during dense-clearance auditing.
**The workflow conclusion is cancelled, and the clearance gate is incomplete.**

## Native evidence

The original per-job logs are retained as compressed files beside this document.
[manifest.json](manifest.json) records their SHA-256 hashes and bounded results;
[ci-result.json](ci-result.json) retains the job and step conclusions.

Each log records the expected five family snapshots: two Arrow cars with two
riders/eight seats, one Legacy, Free Spin and Revised car with one rider/eight
seats each, and four PAX cars with four riders/sixteen seats. All six Display Link
source outputs and serialized block updates are exercised. An ordinary station
reports no PAX dispatch authority; the bound winch uses its own state machine.

The additional regression marks a linked peer as requiring physical realignment
and separately as retaining an unrepaired support-loss fault. Diagnostics and
dispatch reject those states for every family, including a trailing PAX car.
Direct PAX start also rejects pending realignment. Rejected realignment requests
leave the saved car and winch state unchanged. The native log markers include:

```text
FD_TELEMETRY supportRepairGuards=5 linkedPeer=true paxStartGuard=true
FD_TELEMETRY guards=7 readOnly=true recoveryPreserved=true staleSnapshotRejected=true
FD_TELEMETRY paxPhases=BOARDING,LIFTING,FAULT winchAuthority=true faultVisible=true
FD_TELEMETRY COMPLETE
All 47 required tests passed :)
```

The repair fields in this telemetry-specific negative control are set directly
on actual native cars. It verifies fresh authority checks and immutable sampling;
the separate support-repair regressions exercise physical loss and reattachment.

## CI runtime correction

The earlier [dense-clearance checkpoint](../../0.4/recovery/dense-clearance/acceptance.md)
records 5m49s for single cars plus 11m03s for trains: 16m52s in total on the target
host. This run's hosted build/native phases took 9m13s–11m35s, already requiring
26m05s–28m27s with those historical audit durations, before hosted-runner slowdown
or remaining workflow overhead. The 25-minute whole-job budget was insufficient.
The logs show cancellation rather than a reported clearance assertion failure;
they do not establish what the unfinished audits would have concluded.

Commit `ab14b89` gives the job 90 minutes, retains a 25-minute limit on the native
build/test step, and allows 60 minutes for the dense audits. It uploads the native
checkpoint before static audits and times the single-car and train audits
separately. The four profiles, every original validation command, source pins,
one-tick sampling requirement, minimum sample count, margins and audit algorithm
are unchanged. YAML parsing and command/matrix equivalence checks passed. A full
run under the corrected budget is still required.

## Replacement run could not start

[Replacement run 41541895778](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/actions/runs/41541895778)
on `5af4a97` failed before executing any job steps. GitHub reported an account
payment/spending-limit restriction; see the [original annotations](replacement-ci-annotations.json)
and [job results](replacement-ci-result.json). No runner was assigned and no test
or audit ran. This is neither validation nor a runtime failure of the correction.
Remote retries should wait until the account restriction is resolved. Local
validation remains possible when the shared runtime is available.

## Remaining acceptance

This checkpoint does not certify goggles rendering, Display Link configuration
or visible output, client packet application, two-client/late-join behavior, or
the full FD401/FD904 release gates. The winch's native client goggles method is
not invoked by this dedicated-server test; its common formatting is tested.
No host-specific performance target was enabled in CI. The final integrated
artifact needs its own applicable acceptance, and the earlier branch's evidence
must not be reassigned to it.
