# FD401 broken PAX consist source handover

On 2026-11-04, an isolated combined Styles + Engineered NeoForge development
server ran only `brokenpaxconsistcanberetaineddetachedandrebound` from committed
source `2373def`. The fixture assembled a four-car PAX shuttle with a mounted
trailing-car rider, closed restraints, faulted the winch, and deliberately
cleared every native coupler. It checked that each chassis was a separate
one-car native component.

The test then used the editor request path to **Retain** each separated car and
**Detach shuttle** from the lead. It recoupled the stationary cars with the
native link API, used the editor to **Release retention**, rebound the winch,
and used the editor **Dispatch** action. The [server log](server.log.gz) records
`FD_PAX_BROKEN_CONSIST retained=true detached=true recoupled=true rebound=true
dispatched=true occupied=true`. The test asserted four native links and the
same mounted rider before successful dispatch. The server stopped cleanly with
exit code 0; [SHA256SUMS](SHA256SUMS) identifies the log.

This proves the source interlocks can hand over, rebuild and dispatch a
deliberately separated occupied train when cars remain close enough for native
recoupling. Link removal and recoupling were fixture API actions, and winch
binding was called directly. It does not prove normal operator recovery of the
saved failed `81ee2e6` world, physical repositioning of displaced cars,
normal client recoupling/binding, or a packaged candidate replay. The older
[read-only broken-world replay](../fd401-candidate-broken-consist-2e12e9d/README.md)
remains the evidence for its unavailable telemetry and Display Link message.
