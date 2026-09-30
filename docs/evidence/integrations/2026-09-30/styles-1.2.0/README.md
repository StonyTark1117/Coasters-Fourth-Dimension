# Track Styles 1.2.0 cosmetic spine adapter

This is development evidence for two Arrow spine choices, not FD801 functional
certification or the full FD901–FD904 matrix. Base versions match the parent evidence
directory. Track Styles adds CurseForge file 9020601, SHA-256
`42535a83307f6898b33c13ca201533bb9e337418fd7c73039c867fc80bfbcb38`.

## Automated checks

- Track Styles alone: all 21 registered required native tests passed; the optional
  Engineered test logged a skip. Appearance changes exercised the shared server
  editor and retained family, variant, functional provider, profile, geometry and
  magnetic data. Occupied-approach and unavailable-style requests were rejected.
- Engineered + Track Styles: all 21 required native tests passed, including all three
  occupied Engineered circuits. `FD_APPEARANCE styles=true choices=3` verifies that
  the original appearance and both installed spine choices were registered.
- Final base run: all 21 registered tests passed, with the Engineered test explicitly
  skipped. `FD_APPEARANCE styles=false choices=1` verifies the absent-addon path.
- All 17 unit tests and builds passed. Native circuits in this run retain their
  original appearance; selected styles were checked through persistence/editor and
  packaged rendering, not a styled occupied circuit.

## Packaged client checks

The immutable artifact is identified in `visual-artifact.sha256`. The client used
the Engineered + Track Styles profile and a disposable local save.

1. Selected a complete Arrow curve with the Rotation Tool and cycled original →
   single spine → double spine. The editor appeared and server revisions advanced.
2. Found visible seams in the first rigid-segment renderer. Replaced it with bent
   baked vertices sharing the track frames at segment boundaries, and omitted
   interior end caps. Both final spine choices were inspected; functional rails,
   ties and control rails remained present.
3. Reloaded resources with F3+T and inspected the restored double-spine mesh.
4. Saved and quit, restarted with Track Styles absent, and inspected the complete
   original mesh. Saved and quit again, then reinstalled Track Styles and verified
   that the double-spine choice returned. `removal-round-trip.json` proves every
   saved field of the selected section was identical across those three states.
5. Reopened the save and cycled back through original to single spine using the
   final artifact; `single-spine.png` records that final rendering.

`editor.png` was captured before the renderer repair; the editor behavior is unchanged.
`original-fallback.png` and `double-spine-restored.png` use the same camera and final
artifact across removal/reinstallation.

The upstream artifact emits client-class mixin warnings on dedicated startup and
invalid resource-path/missing-unused-model warnings during client resource loading.
These runs completed, but the logs do not establish a warning-free pack.

## Remaining limits

No two-client or late-join style evidence, interrupted-chunk test, styled occupied
ride, missing/empty-resource override test, or measured performance certification.
Track Styles' own tools, colors, ties and rail models are not integrated. Appearance
changes cover complete unoccupied sections; boundaries are hard style transitions.
Other families retain original appearance. FD305 reconciliation remains open.
