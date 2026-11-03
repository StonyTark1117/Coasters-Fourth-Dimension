# Prospective two-car Arrow appearance transitions — ff7c831

The frozen-source offline analysis completed with exit 0 and no changed
source/tool/input/dependency/plan hashes between startup and completion. It uses
[the same native two-car trace](../fd402-isolated-base-ff7c831/README.md) as the
[uniform-style analyses](../fd401-prospective-styles-ff7c831/README.md), from source
`ff7c831fc2e72c640958fa265486a100fd5720d6`. No Minecraft runtime was launched.

In native route section order, the exact plan is **Original → single spine →
double spine → Original → single spine**. `style-plan.json` identifies each rail;
its SHA-256 is `9e01e56e5b430b741cf7dfbb15614df9cd96c294f7ffd58994de7bd182501e1a`.
The five existing sections remain unchanged mechanically. The plan changes
appearance between whole sections; it does not simulate a style edit inside a
single curve or prove normal editor behavior.

**PASS:** 6,548 one-tick sampled poses, all five sections visited, no
uncertified body/track pairs. The minimum separating bound is
**0.04878234623 blocks**, above the **0.02** requirement.
The limiting pair remains the native recovery-cart upstop wheel envelope and an
Original outboard support. The reconstructed surface has 148,808
triangles, 2 Original and 3 styled sections,
262 provider spine pieces, 68 ties, 69 crossbraces,
6 endpoint bridges and 138 lowered control mounts.

The report retains wheel, chassis/carrier/linkage, native-rider and neighboring
body measurements. Native fixture riders are not player meshes. Proposed style
identity is separate from saved appearance. This is prospective sampled body
clearance only: no visible rendering, UI styling, decoration self-clearance,
continuous swept clearance, custom resources, failed-draw fallback, or full-turn
operator acceptance is established. FD401/FD402/0.9 remain open.

`metadata.json` preserves the exact command, tool/source/input hashes, dependency
hashes, plan hash, process ID, timestamps, exit and report hashes. Its runner
checks start/end identity without changing the frozen audit implementation.
Start/end equality does not itself provide a filesystem snapshot; the frozen
checkout and retained exports were kept unchanged. Independent report review
matched the input hashes to the committed native-export manifest and verified
the plan, source/tool hashes, result and exit metadata. It did not rerun the
geometry using a separate implementation. NumPy was 2.5.4.
