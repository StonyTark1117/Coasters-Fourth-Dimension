# FD501A — accepted no-change disposition

Reviewed 2026-11-14 America/Phoenix by the Wing/runtime lead.

The [FD501 core decision](../dependencies-tooling/README.md#core-decision--ready-for-lead)
is accepted. Keep the pinned Minecraft 1.21.1 / NeoForge 21.1.248 / Create
6.0.10-280 / Coasters 0.1.5 / Sable 2.0.3 baseline and its client dependencies.
The native bearing, chassis pose, plot seats, rider loading and saved assignments
already expose the needed capabilities. Wing's fixed-seat eligibility/topology
is addon implementation in FD1101A, not a dependency upgrade.

Keep schema 6 and existing stable string IDs. Existing persisted fields can
represent a fixed seat group on its own chassis, with strict Wing-only topology
validation. Original rotary groups retain their separate UUID requirements.
Unknown providers continue to reject. Do not adopt the archived provider registry.
The current FoundationSync registrar remains protocol 7; the editor registrar
is already protocol 8. These distinct channels and the historical qualification
artifact must not be flattened into one new version claim.

No dependency, provider or format migration is selected, so FD501A closes by
review without a runtime replay. FD1101A will test its new family and fixed-seat
serialization; optional adapter migrations and their checks belong to B–H.
