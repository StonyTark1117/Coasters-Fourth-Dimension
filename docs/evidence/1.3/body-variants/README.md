# FD2102 body choices — accepted 2026-11-22

Primary `main`, baseline `b5e78f31` plus the recorded 1.3 changes. Both choices
consume the accepted [base refinement](../base-refinement.md). The finite
[design contract](../body-variant-design.md) defines their scope.

## Accepted source and native behavior

The [geometry record](geometry-design.txt) and [audit](geometry.txt) establish
compact Wing and open-frame Fixed Inverted as geometric choices, with connected
rigid supports, preserved cushions/hinges, unchanged outer bounds and matching
picker miniatures. All sixteen Wing dye mappings remain supported. These are
choices for an existing car; inventory items still construct the original body.

[Native tests](native/README.md) passed 2/2 on actual Wing and Fixed Inverted
vehicles. The production editor transaction changes only selected body/revision;
save/load and the real snapshot codec preserve it. Invalid, wrong-family,
stale, unauthorized, occupied and physically moving edits reject atomically.
Unknown persisted identifiers remain intact while resolving to Original;
missing old fields default to Original. Pig occupancy and injected native
velocity are disclosed fixture inputs, not player ride evidence.

The coordinator reviewed the transaction and actual test assertions, checked the
raw passing log and clean shutdown. A separate read-only integration review found
no concrete issue in wrapper order, family orientation, dynamic restraint
ownership, palette composition, fallback or persistence guards.

[Package comparison](package.txt) verifies all 85 affected exports against source
and byte-identical independently built JARs. Java 21 builds retain the existing
1.1.0 artifact label; no publication or version promotion is implied.

## Accepted client behavior

[Wing](wing-client/README.md) and [Fixed Inverted](fixed-client/README.md)
private clients prove actual picker/world preview, cancel/apply, independent
upholstery, occupied fit, normal boarding/restraints/ride/stop/unload and
cold-process persistence. Both used the same packaged JAR. Their fixtures and
camera setup are disclosed; no new survival-construction or full-route campaign
is claimed. Inventory construction remains unchanged and reuses accepted base
vehicle evidence; new native vehicles default to Original in the targeted tests.

Wing also exercises the shared missing-resource path with an intentionally
missing texture in one private alternate model. The picker reports fallback and
uses the Original miniature/world body without losing the saved identifier.
Removing that override and using normal F3+T restores the alternate. Reloads
were performed outside the picker, as required by vanilla key handling;
preview-identifier survival within an open screen is source-review coverage.
The whole saved Wing NBT and SHA-256 were identical across cold restart,
fallback and restoration. Fixed Inverted's complete saved vehicle dictionary
was likewise identical before and after restart.

The coordinator independently inspected occupied/banked and restart/fallback
images, raw editor/save logs, both saved-data comparisons and exact inactive
runtime units. All clients, servers and private displays exited cleanly; worlds
and logs remain at the recorded persistent paths. No new production defect was
found. FD2102 is complete within this two-family roster and feeds
[FD2103 reconciliation](../integration-manifest.md).
