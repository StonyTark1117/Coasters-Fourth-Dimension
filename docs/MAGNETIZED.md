# Optional Magnetized propulsion

FD812 is qualified on the pinned `magnetized` profile (Coasters: Magnetized1.1.0,
file8668703). The addon remains optional. Its longitudinal force is separate from
Fourth Dimension's magnetic torque on a rotating seat carrier. Exact callback,
anchor and force-setting fingerprints gate the adapter; unknown artifacts cannot
authorize Fourth Dimension propulsion.

## Equip and select

On ordinary Wing track or dedicated [Launched Inverting Spinning](SPINNING-S2.md)
track, right-click a native anchor with Gold Ingot to equip launch magnets, or
Lapis Lazuli for brakes. Supply redstone and use the upstream force setting
(0–100%). Brake equipment removes motion in either direction and never reverses
a stopped car. Remove equipment with the
upstream empty-hand interaction. Do not confuse the force percentage with a
travel-direction control.

Bind a Boarding Station to the stopped complete train, within eight blocks of
the lead. Use the Rotation Tool near that station and select **Claim Magnetized**.
This explicitly transfers any held previous owner and selects Magnetized as the
only permitted longitudinal provider. The spinning train's governor lease then
expires and its output stays zero. Native Chain and Engineered cannot add force
while Magnetized owns the train. CTNB conflicts are reconciled separately by FD813.

Select forward or reverse while stopped and held. Direction means the native
section's committed first-to-second anchor direction; connect successive sections
consistently. Close restraints, then Dispatch. Every dispatch checks the whole
train, the current station and powered launch equipment. A zero-strength setting,
brake-only section, open restraints, unavailable owner or stale request cannot
launch. Propulsion approaches the selected signed target, so an opposite-moving
rollback is slowed before reversing. Target speed is bounded by12blocks/s and
the addon server cap. Impulse acceleration is bounded by12blocks/s² for Wing and
3blocks/s² for Launched Inverting Spinning; actual speed also reflects native
drag and track slope.

Launched Inverting Spinning keeps its dedicated track, body, spin, clearance,
route and dock guards. Its optional drive replaces the governor command path
only after explicit selection; all required powered travel sections need
Magnetized equipment because native Chain is fenced. Its carrier-axis magnets
remain independent. The ordinary
standalone governor path is unchanged. A lift lock still needs an actual native
Chain span to define its guided section; it does not grant competing propulsion.

Loss of power on an equipped section latches the selected authority held. Restoring
power does not restart the train. Stop/Capture, repair the source, then explicitly
Resume owner and Dispatch at the bound station. Lost support, body or controller
also denies force. Coasting between unequipped sections is intentional. Saved
ownership restores held with explicit resume required; direction persists.

`/fd demo magnetized` prepares a Wing loop, source power and return equipment.
The first launch anchor remains unequipped: apply Gold normally, select the
provider, board, close and dispatch. Stop on the level dock, open restraints and
Shift to unload. The example's native Chain remains visible but cannot propel a
Magnetized-owned car. Setup commands do not count as player construction.

[Native and normal operator evidence](evidence/0.8/magnetized/README.md).
