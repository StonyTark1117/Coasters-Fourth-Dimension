# FD401 Styles component ownership at the coupled inversion

This review uses the exact `2c5d9a7` candidate JAR from the [visible coupled
ride](../fd401-styles-coupled-visible-2c5d9a7/README.md). Its SHA-256 is
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`.
The [candidate identity](../fd401-final-candidate-2c5d9a7/README.md) ties that
JAR to the committed source. No new client run was made for this review.

The [banked inversion](../fd401-styles-coupled-visible-2c5d9a7/banked-inversion.png)
and [flat inversion](../fd401-styles-coupled-visible-2c5d9a7/flat-inversion.png)
show the occupied coupled Arrow train beside a red Styles frame, gray running
rails, yellow control rails, and their attachments. The raw client log has
positive `inRangeQuads` and `inRangeDecorationPieces` throughout the ride; the
render counter is cumulative across frames and rails, so it does not identify
individual pixels or sections.

| Component on styled Arrow sections | Assigned owner | Candidate draw path and observed limit |
|---|---|---|
| Spine | Styles | `TrackStylesRenderer.build` places the selected catalog's `centerBeam` pieces along the native path. `OriginalTrackRenderer.build` skips its procedural spine tube when that mesh exists. The red frame is visible through both inversions; the screenshots alone cannot distinguish every provider beam from the native crossbrace. |
| Ties | Styles | `TrackStylesRenderer.build` separately places the catalog's `tie` pieces every two blocks. The Original renderer has no separate tie mesh to double draw. Individual ties are too small in the retained moving frames for a pixel-level count. |
| Running rails | Original | Procedural tubes for both main rails are unconditional in `OriginalTrackRenderer.build`; the gray pair is visible beside the vehicle in both inversion frames. Styles uses only `centerBeam` and `tie`, never its catalog's rail models. |
| Running supports | Original | The outboard underbrace and uprights are guarded by `RUNNING_SUPPORTS`, which `TrackRenderOwnership` fixes to Original. The native crossbrace and endpoint spine-step bridges also remain in the Original mesh when Styles owns the spine. The screenshots show the combined truss but do not isolate each support from the Styles frame. |
| Control rails and mounts | Original | Arrow is a driven family. Both control tubes and their mounts are built from the live profile in `OriginalTrackRenderer.build`; the yellow pair remains visible through inversion. The provider is not passed the profile or control geometry. |
| Magnetic equipment | Original | The ownership contract reserves it for Original. This Arrow circuit has no magnetic spans, so this run makes no visible magnetic-equipment claim. |

The selected provider owns decoration only; `TrackRenderOwnership` rejects a
provider that claims mechanical components. A null/missing Styles mesh selects
fully Original ownership before mesh construction. A draw failure restores the
suppressed Original spine immediately and quarantines that provider mesh for
subsequent frames. The failure frame can contain partial provider output; this
review does not certify a rendered failure/reload sequence.

This is a component-by-component **ownership-path review** paired with the
client-visible inversion, not a pixel-isolated proof for ties and every support.
The relevant candidate source SHA-256 values are:

| Source | SHA-256 |
|---|---|
| `TrackRenderOwnership.java` | `8d7b795ccf9e1a54b1c7ffe375cf8a94d6d9f10c72c344f9bce94480c4c9887e` |
| `OriginalTrackRenderer.java` | `d1323d09d93e738ff01140133aa0e7fb83e6bcc7ca99884e3b265dde1c2faba4` |
| `TrackStylesRenderer.java` | `fa69593b5a8b9f93ece00f40ebd67f00692db9d8af8d18648563f42146edb217` |
| `TrackStylesCatalog.java` | `2b124893d6477a9b4be453a8a34f93a42b7b3f0b04fc926619d99c5d6ec16dbc` |
