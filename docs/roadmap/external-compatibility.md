# Independent external compatibility

[Guide index and execution rules](README.md) · [Canonical roadmap](../../ROADMAP.md) · [Active assignments](../CURRENT-QUALIFICATION.md#current-work-queue)

FD801 depends on an inspectable functional upstream release and applicable terms. Its availability never blocks standalone 1.0.
FD2008 is the assigned 1.6 Launch Systems moving-track interoperability, outside completed 1.5;
the already accepted fixed-track external authority bridge retains its scope.

Card order helps navigation; actual prerequisites determine readiness. These are
procedures, not a second status tracker. Apply the index's evidence and stopping
rules to every step; already-covered steps do not require another run.

## Packages

- [FD801 — Integrate a released Track Styles Arrow 4D](#fd801)
- [FD2008 — 1.6 Launch Systems moving-track interoperability](#fd2008)

## FD801

**Outcome:** An explicitly verified functional Track Styles Arrow 4D adapter when upstream permits it.

**Start conditions:** FD401 and an inspectable external release with applicable terms established.

**Read first:** [canonical scope, status and completion criteria](../../ROADMAP.md#fd801-integrate-a-released-track-styles-arrow-4d); [agreed integration capabilities](../INTEGRATION-BACKLOG.md#what-each-integration-means), [existing integrations](../INTEGRATIONS.md), [OptionalIntegrations](../../src/main/java/dev/fourthdimension/integration/OptionalIntegrations.java), [RideOperations](../../src/main/java/dev/fourthdimension/integration/RideOperations.java), [Track Styles terms](../../PLAN.md#d05-track-styles-licensing-and-integration-approach).

**Ordered steps:**

1. Confirm an inspectable released functional track and applicable terms; record a precise upstream blocker if unavailable, without holding standalone release.
2. Inspect actual rail/control contacts, native APIs and supported version; cosmetic matching or equal main-rail gauge is insufficient.
3. Implement the bounded functional adapter through reviewed eligibility/provider contracts, preserving Arrow profiles, persisted identities and supported rejection paths.
4. Verify Arrow fit/control behavior, ordinary fitting carts without 4D effects, one control-rail render owner and persisted profiles; cover same-family provider transitions with their affected multiplayer/lifecycle checks.
5. Complete uncovered normal player operation for the named upstream configuration, then record supported versions, installation/terms and evidence; keep optional appearance acceptance and unrelated release gates closed.

**Validation:** Targeted native contacts/eligibility/provider checks plus uncovered player/control/render behavior. Certification is for the inspected functional release only.

**Finish and handoff:** Close the independent adapter when its canonical criteria pass; unavailable upstream functionality never becomes a hidden 1.0 prerequisite. Record evidence and closure in the canonical package/current queue as applicable, then stop.

## FD2008

**Outcome:** Explicitly supported Launch Systems handoff to/from the named
moving-track mechanisms. Assigned to 1.6; no 1.5 dependency. See the [finite contract](../specs/FD2008-launch-systems-1.6.md).

**Start conditions:** Accepted FD2001 moving-support contract and consuming
mechanism(s), plus an inspectable Launch Systems controller/authority contract.
Implementation needs its own assignment in the current queue.

**Read first:** [Canonical scope](../../ROADMAP.md#fd2008),
[1.5 boundaries](../specs/FD2001-moving-track-1.5.md#future-launch-systems-compatibility)
and [existing external authority evidence](../evidence/launch-systems-authority-20261122/README.md).

**Ordered steps:**

1. Specify a finite supported controller/car/rail/mechanism list and inspect the
   actual authority API on both sides. Existing fixed-track bridge acceptance
   does not establish moving-segment support.
2. Implement whole-consist retained handoff, fixed-span force inhibition,
   destination reservation and explicit return of ownership. Reject stale or
   competing commands and unsupported specialty/managed-route combinations.
3. Prove actual consumer force, power-loss/restart, controller/mod removal and
   occupied transition/recovery. Reuse the accepted native moving lifecycle and
   unchanged bridge evidence rather than reopening them.
4. Update both projects' capability metadata and player guidance to report only
   qualified combinations; reconcile their separately owned deliverables.

**Validation:** Distinct native consumer/authority checks and connected occupied
handoff on the declared combination; no unrelated mechanism or family sweep.

**Finish and handoff:** Close this adapter only with actual integration
evidence. FD2007 never waits for it, and publication remains separate.
