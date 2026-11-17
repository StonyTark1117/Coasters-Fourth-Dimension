# Builder and operator tools (1.1)

Open a vehicle or original track section with the Rotation Tool, then select
**Tools** in the editor header. Client and server must both run the same 1.1 build.
The tools extend the accepted 1.0 vehicles; no optional addon is required.

## Portable setting presets

Select **Setting presets**, enter a name, then **Capture settings**. Save locally
with **Save / replace name**, or **Export clipboard** to share the code. On the
other destination, use **Import clipboard** or **Load next saved**, review the
name/family/settings (hover the shared code to read decoded settings), confirm **Bind selected destination**, then **Apply preset**.
A successful server acknowledgement is required before treating an apply as saved.

| Selected destination | Portable settings |
| --- | --- |
| Arrow or Axis original rail | Complete distance/angle/slope profile |
| Free Spin or Launched Inverting Spinning original rail | Complete magnetic span list |
| Legacy ZacSpin, Free Spin, Revised ZacSpin or PAX car | Damping and ballast |
| New-generation Flying original rail | One named route interval: posture, yaw, blend, winding, rate limits and show stop |
| Powered Inverted Yaw original rail | One named route interval with its powered speed/dwell/direction/event settings |

For route intervals, **Next route section** changes the capture index; it wraps
when captured. Applying an interval uses the existing named-section editor's
replace/add behavior. This does not copy an entire route, train, controller or world.
Other vehicle families retain their existing editors; they do not have a declared
portable motion preset in 1.1.

Format version 1 requires the exact family, variant and destination type. Profile
lengths and junctions must fit the destination; they are never silently scaled.
Existing numeric bounds, item costs, permissions, proximity, geometry validity,
occupancy and revision checks still apply. Invalid or stale requests leave current
settings intact. Restore/rebind changed track in its normal editor first.

World marker coordinates and identity are removed from route exports. A TARGET
interval requires an explicitly entered, owned marker at **x y z** in the destination
world. Existing marker ownership/loading/range rules apply. Destination confirmation
does not copy authority, ownership, physical geometry or controller bindings.

The local library is `config/fourthdimension/presets.txt` inside the client game
directory. Names replace only an exact matching entry; names never become file paths.
It holds up to 64 entries and preserves a corrupt library rather than overwriting it.
The sharing code is limited to 12,000 characters. Parsing a code identifies the
preset; the server validates its settings against the actual destination on apply.

## Original upholstery colors

All 17 original vehicle styles expose **Original**, **Ocean**, **Crimson**,
**Forest** and **Sunset** for their original seat cushions. These reuse original
upholstery materials. They do not recolor structural metal, safety markings,
restraints, track or an optional provider's chassis.

Open **Original upholstery colors**, select a color to preview it on the selected
car in the world, then **Apply color** to save. Back/cancel discards the preview.
The train must be empty, stopped and captured. Color is saved per car and survives
world reopening. Older saves or unknown palette identifiers fall back to Original.
Mechanical identity, articulated meshes and seat ownership remain unchanged.

**Optional chassis appearance** retains the existing Coaster Skin choices and
Original fallback when the provider/resources are absent. Original cushion colors
remain independent of that optional integration. No arbitrary texture uploads or
branded replica liveries are included.

## Ride operator panel

**Ride operator panel** shows server-sourced whole-consist occupancy/capacity,
restraints, capture/retention, controller, available power, recovery state, faults
and the reason dispatch is denied. Hover truncated rows to read the full reason.
Unknown external propulsion power is explicitly unavailable rather than reported
as zero. Bound PAX winches and supported authority/governor paths provide their
existing power telemetry.

Capture, restraint, dispatch, retention, recovery braking and applicable resume/
shuttle-recovery buttons use the same guarded server operations as the existing
editors. The panel does not bypass ownership, distance, revision, boarding,
mechanical interlocks or authority. Dispatch is disabled while denied. Other
requests can return a specific refusal; inspect it before retrying. Controls are
disabled while a command awaits acknowledgement or state has timed out. State is
refreshed once per second while the panel is open.

Use **Vehicle / recovery controls** for existing family-specific configuration,
recovery attachment, alignment and authority binding. No park-wide controller,
automatic recovery or multi-train dispatcher is added.
