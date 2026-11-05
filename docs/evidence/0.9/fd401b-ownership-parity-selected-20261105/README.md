# FD401B selected-JAR render ownership parity

The selected `ec2fcda` qualification JAR is
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
The earlier [component review](../fd401-styles-component-ownership-2c5d9a7/README.md)
used candidate JAR
`4b23cf0e1766617eb106a7ffcf293f73639d2bad119872c218b7cb2bb52b7781`.
This record compares the packaged class entries and the source difference; it
does not add a client run.

| Packaged class | Selected SHA-256 | Difference from earlier candidate |
| --- | --- | --- |
| `TrackRenderOwnership` | `5bfb0007ba7caff67aaba0365d89a324c4f3882828082ed7f2a33873e05cce62` | Byte identical |
| `TrackStylesRenderer` | `c53a0c5e46938a0cb11931e470cb8c2ac13ccaa2b9cbd0931f8da9dd770ddd0b` | Byte identical |
| `TrackStylesCatalog` | `50970a056400c312c5986b41f0dbb156c27e51d7e3be4407f31ea88503e39f85` | Byte identical |
| `OriginalTrackRenderer` | `1000dc13d673319f29a2f5a070ff9c64b32be2eefbeef4a2f4709b80e91fdd25` | Changed; see below |

`git diff 2c5d9a7 ec2fcda --
src/main/java/dev/fourthdimension/client/OriginalTrackRenderer.java` shows
exactly two added lines after its native quad draw. They report
`renderedNonzeroProfile(rail.id)` when a rotation-profile key has an angle of at
least 0.25 radians. They do not alter the mesh, ownership selection or draw
guards. The current source SHA-256 for this renderer is
`059f1e2dcf52425fd0f8f161c9d12424d59eb51ff1c45a2c4654224c4e70f67c`.

The unchanged ownership contract requires one nonblank owner for each of six
components. Only spine and ties may be owned by Styles. Original always owns
running rails, running supports, control rails and magnetic equipment. A
successful Styles mesh contains only spine and tie pieces. On a styled section,
the Original mesh omits its spine tube and draws its rails, supports and driven
control geometry; a missing or unsupported Styles mesh selects fully Original
ownership. The selected [FANCY coupled ride](../fd401b-coupled-styles-selected-20261105/README.md)
records nonzero native geometry and Styles decoration pieces while occupied.

This establishes selected-JAR **ownership-path parity** with the earlier
component review and a bounded runtime indication that both paths drew. It
does not isolate individual rendered pixels, prove all six components visible
in one frame, or exercise a provider draw failure on this JAR. The earlier
review's visible inversion frames remain at their recorded older-JAR identity.
