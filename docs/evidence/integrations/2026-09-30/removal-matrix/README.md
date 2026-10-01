# Schema-5 dedicated-server addon removal matrix

This records cold restarts of parked, unoccupied rides using the packaged mod JAR.
It covers a combined-pack control, five individual addon omissions, complete addon
removal, and reinstallation on the save already written without those addons.
Each omission starts from a separate copy of the same stopped baseline world.
The original QA worlds were not edited.

The baseline contains 35 original functional sections, eight vehicles and twenty
carrier bodies: one Arrow, one legacy ZacSpin, one Free Spin, one revised ZacSpin,
and four coupled PAX cars. One Arrow section selects Track Styles' double spine;
one Free Spin section has original magnetic equipment. The Arrow circuit has
Engineered controllers/observer. Five native Additions stations and five STOP fins
were placed alongside the family routes. Stations have auto-dispatch enabled with
threshold 15, a three-second delay and five-second cooldown; cars are unoccupied
with restraints open, so dispatch must remain inhibited. Fins are unpowered.
No Magnetized-specific saved anchor mode or occupied camera state was seeded.

All server processes start frozen, then run 200 ordinary ticks before inspection,
save and clean shutdown. No save NBT is edited. Original demonstration commands
create the additional family layouts; native `/setblock` NBT configures Additions
spans using its inspected serialization. The setup initially attempted the Arrow
equipment in an unloaded chunk; that was rejected, the corridor was force-loaded,
and both blocks were placed before the baseline backup. The setup transcript
retains those rejected commands.

The verifier compares every saved field except carrier angle and angular velocity,
which may settle differently after a cold restart. It requires exact rail IDs,
functional and appearance providers, variants, families, geometry fingerprints,
rotation profiles, magnetic spans, revisions, chassis/carrier IDs, seat definitions,
bindings, restraints and capture state. Every vehicle must remain fault-free,
parked and boarding-ready after ticking. Native logs must show all three PAX
couplings restored among four cars. This is stronger than a successful startup
alone, but does not replace occupied riding or client rendering tests.

The RCON block reads separately record the upstream equipment. Missing Engineered
or Additions registries cause recoverable chunk-load errors and discard their
block entities. Reinstalling the addons on that subsequently saved world does not
restore those controllers, stations or fins. Our functional ride data and selected
appearance IDs are retained. Retaining an appearance ID is data evidence; the
rendered fallback/reappearance is covered by the earlier Track Styles client work,
not by this dedicated-server run.

All eight runs exited successfully. The verifier confirms 35 unchanged rail
sections, eight parked/ready vehicles, twenty captured carriers and all three PAX
couplings in every case. Direct conditional block queries confirmed all ten
Additions blocks and the queried Engineered controller were air after reinstallation.
`verification.json` records the per-case equipment counts.

## Reproduction and recovery

1. Back up the **entire stopped world**, including Sable sublevels, region data,
   player data and addon SavedData, before changing a pack. A copy of only
   `fourthdimension_foundation.dat` is not a complete ride backup.
2. Prepare the five-family parked baseline with the commands in `setup-commands.txt`.
   The retained setup started from the earlier combined Arrow QA world, which
   supplies the styled section and configured Engineered circuit.
   To build an equivalent baseline from a fresh world, force-load the Arrow corridor
   with `forceload add -624 0 -544 80`, then run
   `execute positioned -600 227 0 run fd demo engineered arrow`. Select one empty
   Arrow section with the Rotation Tool and set its appearance to double spine.
   Park the car, leave restraints open, disconnect the operator, then follow the
   other four demo and equipment commands in the setup transcript. New UUIDs are
   expected; compare each restored copy against its own captured baseline.
3. Use Java 21 with the pinned packaged JAR and `runServer -PfdPackaged
   -PfdPackagedJar=/absolute/path/to/jar -PfdFrozen`. Run once with all five
   `-PfdIntegrations=engineered,styles,additions,magnetized,immersion`, then fresh
   world copies with each named entry omitted, and finally no profile property.
   Profiles resolve their required Aeronautics/Interiors dependencies automatically.
4. Issue `tick step 200`, wait for completion, query the recorded block positions
   and `fd status`, then `save-all flush` and `stop`. Restart the base-written copy
   with all five profiles to reproduce the destructive equipment-removal round trip.
5. To recover removed addon blocks, restore the complete pre-removal world backup
   with its original pack. Reinstalling JARs alone is insufficient after saving
   missing-registry chunks. Alternatively rebuild and rebind the missing equipment,
   then validate the station and dispatch sequence before carrying riders.

Run the retained evidence verifier:

```sh
python3 scripts/verify-removal-matrix.py docs/evidence/integrations/2026-09-30/removal-matrix
```

Per-run mod lists, command output, filtered logs, warnings, SavedData snapshots,
full-log/NBT hashes and exit codes identify the execution. The copied baseline
uses schema 5; this is not an upgrade from every older schema. Known dependency
client-class warnings and explicit missing-registry errors remain visible.

No players were connected. Occupied restart/removal, airborne interruption,
Additions wireless UI/readout behavior, Magnetized anchor-mode migration,
Immersion camera removal, rendered styles, client late joining, every configuration
and measured performance remain outside this evidence. The broader FD901–FD904
matrix remains open.
