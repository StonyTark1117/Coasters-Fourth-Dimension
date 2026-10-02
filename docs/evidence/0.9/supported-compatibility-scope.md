# FD901 advertised compatibility scope

This is the release-candidate compatibility boundary for Fourth Dimension
0.9.0-dev.27. It identifies the combinations the addon actually advertises and
the evidence that covers each one. It does not promote an inspected optional
addon into a functional-track provider.

| Installed profile | Supported content | Functional-track rule | Evidence |
| --- | --- | --- | --- |
| Standalone | All five original 4D tracks and matching cars; certified bare ordinary bearings | A 4D car requires its matching original family and variant. A fitting bare ordinary bearing receives no 4D state. | [Family matrix](family-eligibility-matrix.md), [ordinary-cart matrix](ordinary-cart-snap-matrix.md) |
| Track Styles 1.2.0 | Optional cosmetic spine/tie appearance on controlled original Arrow track | The style decorates the original Arrow rail. It cannot create a functional Arrow rail, change family/variant, or grant a non-Arrow car eligibility. | [Family matrix](family-eligibility-matrix.md), [integration boundary](../../INTEGRATIONS.md) |
| Engineered 1.1.1 | Optional operating/motor adapter on original functional rails | Engineered does not supply an accepted external functional-track contract. Original family/variant eligibility remains authoritative. | [Optional profile matrix](optional-profile-matrix.md), [integration boundary](../../INTEGRATIONS.md) |
| Both audited addons | The two optional behaviors above, combined | Decorations and operating adapters leave original track identity and family matching unchanged. | [Optional profile matrix](optional-profile-matrix.md) |

## Explicit exclusions

No third-party functional track is supported in this candidate. In particular,
an Arrow-looking Track Styles model is not a mechanical provider and cannot
attach Fourth Dimension cars by appearance. Any future functional adapter must
declare an equivalent track contract, pass matching/mismatch/ordinary-cart
tests, and receive its own recorded release before it is listed here.

This means the FD901 matrix covers every currently advertised profile. It does
not claim compatibility with future addon versions, modified cart assemblies,
or unreviewed third-party tracks.
