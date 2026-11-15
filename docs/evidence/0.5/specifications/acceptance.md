# Ten specification packages — completion review

Completed 2026-11-14 America/Phoenix (queue date 2026-11-15 UTC). This review maps
the **existing specification criteria** to complete written inputs; it does not
close later vehicle implementations. Owner, checkout and current state are in
[README](README.md). No production changes, Gradle build or game runtime was used.

| Package | Resolved decisions and finite handoff |
| --- | --- |
| [FD1401](../../../design/0.5/FD1401-pipeline.md) | Seated between-side-rails interpretation, primary TOGO evidence, original P contacts/4-seat geometry, fixed route instead of transfer table, coupling and occupied clearance/station prototype plan. |
| [FD1502](../../../design/0.5/FD1502-roller-ball.md) | Distinct transverse-X four-seat shared body, smooth vertical fixed zigzag, native graph/coupler disposition, guided/latched lift and free return, retention/capture/wrong-family/restart prototype plan. |
| [FD1501](../../../design/0.5/FD1501-spinning.md) | Separate finite S1/S2 matrix, local-Y physics/loading/capture, dedicated functional track choices, S2 native Chain standalone launch with named governor work, spin-only magnetic influence and independent acceptance/content. |
| [FD1302](../../../design/0.5/FD1302-axis.md) | Explicit longitudinal-Z outboard carrier, free/damp/driven modes distinguished from reference certainty, inertia/propulsion/capture/rollback, two-car clearance and falsifiable prototype plan. |
| [FD1301](../../../design/0.5/FD1301-suspended.md) | Measured N bogie interface, dimensioned bounded lateral swing and cabin, safe moving lift lock guards/release, banked/linked sweep, interrupted-lock and load tests. |
| [FD1601S](../../../design/0.5/FD1601S-fixed-inverted.md) | One four-abreast overhead fixed configuration, contact/seat/hanger envelope, consist bounds, inherited fixed-seat ownership and distinct inverted restore/clearance claims. |
| [FD1702S](../../../design/0.5/FD1702S-dive.md) | One six-abreast fixed configuration, approach/acquire/hold/release/stop states, FD1701A authority, timed interruption/commit semantics and finite acceptance without full multi-train dependency. |
| [FD1801S](../../../design/0.5/FD1801S-conventional-flying.md) | Seated boarding, stopped station prone changeover/locks, original hinge and brace relocation, physical rider/eye ownership, interrupted-transition recovery and occupied prone proof. |
| [FD1802S](../../../design/0.5/FD1802S-new-generation-flying.md) | Seated/sideways and prone route sections, fixed-track vehicle-joint changeover, compound yaw/posture sweep and physical targeting in both postures, complete reusable yaw contract and finite synchronization/failure claims. |
| [FD1901S](../../../design/0.5/FD1901S-powered-inverted-yaw.md) | Upright below-track per-car yaw, powered forward/reverse/speed/dwell and standalone native Chain path, winding/capture, finite light/audio events, common yaw reuse and distinct authority/interruption claims. |

All ten include reference/design separation; dimensions and functional fit/rejection;
axes/pivots/motion/load; operation/failure states; restraint/seat/camera/propulsion
ownership; persistence/sync inputs; required native/add-on interfaces; original
content list and finite acceptance. [Conventions](../../../design/0.5/specification-conventions.md)
carry genuinely common inputs; [shared yaw](../../../design/0.5/shared-yaw-contract.md)
is normative for the two consumers. Each package retains a self-contained mechanism
and the distinct claims its later implementation must prove.

## Bounded validation performed

- [Pinned native signatures/bytecode review](native-interfaces.md) verified graph,
  frames, contacts, joints, motor/limit, native Chain and coupling seams. In
  particular, `GenericConstraintHandle.setLimit` is distinct from the rotary motor,
  and native quaternion +Z is backward. Specs explicitly convert coordinates.
- [Analytic design check](check-design.py) passed; [result](design-check.json)
  records local-link checks, declared clearance bounds, 5,016 combined Flying
  corner samples and prone/seated target-heading composition across bank/inversion.
  These supplement the conservative sphere bounds, not a native solver test.
- The original Conventional Flying brace had only .18445 blocks of calculated
  clearance. Its specification now requires relocating that brace, giving .33445;
  no native or occupied pass is claimed for the unbuilt geometry. The compound
  yaw envelope also includes rotating forks, and Roller Ball's fore/aft seat
  offsets make its declared asymmetric-loading prototype meaningful.
- No accepted 0.4 claim was changed or repeated. New runtime, content, native
  dynamics and player acceptance remain at the assigned later A/B/C milestones.

## Feasibility boundary and integration

No inaccessible prerequisite remains for these **specifications**. Native API
review resolves representability of the selected fixed routes and joints;
prototypes that measure actual new native dynamics/occupied geometry are retained
as explicit finite implementation plans where the canonical specification asks
for a plan. No successful future-vehicle prototype is fabricated here. Exact
manufacturer geometry unavailable from public primary sources is labelled original
game design. The selected standalone provider is present in the pinned dependency;
new speed/ramp/governor logic is named later implementation work, not an optional
provider assumed available.

Lead may now close these ten specification rows and consume them for FD503.
Selective integration owns only the twelve design files listed in `check-design.py`
and this evidence directory; **FD1101-wing.md belongs to the lead**. No staging,
commit, branch, worktree or production edit was made by this owner. No client,
server, display, world, process scope or port was started, so no runtime cleanup
or handoff remains. Existing concurrent lead/dependency work is untouched.
