# Optional Waterparked and Create Coasters configuration

Use the [selected 0.9 dependency baseline](evidence/0.9/dependencies/README.md). Waterparked0.2.8-beta-hotfix1 needs KotlinForForge5.10.0; Create Coasters2.0 needs the selected Create baseline. Install these gameplay addons and their dependencies on server and clients. Immersion1.1.1, if selected, belongs only on clients; see the separate effects guide/evidence.

For development, add `waterparked,createcoasters` to `-PfdIntegrations`; Gradle includes Waterparked's language loader automatically. The tested coexistence profile also contains `styles,engineered,additions,ctnb,magnetized,skin`. These addons remain optional for Fourth Dimension standalone use.

Waterparked slides and Fourth Dimension rides may occupy the same world and shared native Coasters graph. Build their routes independently. Use ordinary Coasters anchors for Fourth Dimension track: slide anchors, slide car placement and slide snap eligibility are explicitly refused. Shared graph membership does not make a slide an FD route. No amphibious car or cross-system transfer is supported.

Create Coasters' lock utility acts on native Create contraptions and seats. Configure and power it using its native controls. It does not close Fourth Dimension restraints, own a Sable ride, dispatch it or prevent its normal open-seat boarding/unload. Continue using the Rotation Tool and Fourth Dimension station controls for FD rides. No Create-train control bridge is advertised.

The [coexistence record](evidence/0.9/coexistence/README.md) covers the actual slide graph and renderer, explicit vehicle refusal, native utility lock/unlock and normal Wing boarding/unload alongside that utility. Other accepted ride mechanics carry forward under the validation policy.
