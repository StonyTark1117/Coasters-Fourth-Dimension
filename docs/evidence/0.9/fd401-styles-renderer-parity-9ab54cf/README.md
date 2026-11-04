# FD401 Styles renderer class parity review

The [prior FAST/FABULOUS/Solas render record](../fd401-styles-render-modes/README.md)
retains JAR SHA-256 `de64a6833fd0a4e5f7bdbc36ad40b89ff396d48941c7e6ae0ae2e52ca6ca3a70`.
The [current Styles full-turn fixture](../fd401-styles-banked-fullturn-9ab54cf/README.md)
retains source `9ab54cf` JAR SHA-256
`2d4ecc048875af48bb21a18e5ff1179d5a49f767ed45055a4477aa3b76260fb8`.
The two whole JARs differ.

The [class comparison](class-parity.json) hashes every class file under the
current `OriginalTrackRenderer`, `TrackStylesCatalog`, `TrackStylesRenderer`,
`TrackStylesDrawGuard`, `TrackStylesFallbackState`, `TrackAppearance`,
`TrackRenderOwnership`, `ArrowDecorationPlacement`, and `ModelFrame` names.
All 18 selected class files exist and are byte-identical between those two
JARs. The five sampled [banked player-clearance reports](../fd401-banked-player-clearance-a5df9b8/README.md)
also still have all 19 source-file hashes matching the current checkout; their
native inputs, sampling limit, and original provenance remain unchanged.

This review connects the prior stationary visible Styles screenshots and the
current candidate's renderer implementation. It does not make the entire
artifacts equivalent, recover the earlier FANCY ride's unretained runtime JAR,
show the current client rendering the banked full-turn fixture, or prove
visible component ownership through a moving inversion.
