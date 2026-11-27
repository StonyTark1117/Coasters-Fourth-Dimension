# 1.5.0 publication

The human authorized GitHub, Modrinth and CurseForge publication on 2026-11-26,
including required and optional dependency relationships. Release channel: release.

Release tag `v1.5.0` targets source commit `177a8ddd1f427aa1d7764bfb336ce2cf93f30bd9`.

Accepted gameplay source: `5575438aa3eb87b3d4c184c05508654aeb2dddd6`.
[Artifact provenance](artifact.json) verifies two byte-identical Java21 builds;
only version/description metadata differs from the accepted development JAR.
All compiled classes and gameplay assets are identical. No runtime replay is needed.

- GitHub: [1.5.0 published](https://github.com/StonyTark1117/Coasters-Fourth-Dimension/releases/tag/v1.5.0).
  Published release channel, both JAR/checksum assets uploaded; downloaded JAR SHA-256 matches.
  [Receipt](github-release.json) records source tag and verification.
- Modrinth: [1.5.0 uploaded](https://modrinth.com/mod/coasters-fourth-dimension/version/81HExJHB)
  to existing project `4eicZbtp`. Authenticated readback verifies release/listed status,
  four required and five optional exact-version dependency links, and the downloaded
  JAR SHA-256. The project remains `processing` (awaiting moderation), so the public
  page may return 404. [Verification](modrinth-verification.json).
- CurseForge: [1.5.0 uploaded](https://www.curseforge.com/minecraft/mc-mods/coasters-fourth-dimension/files/9839671)
  to existing project `1809752`. The Upload API returned HTTP 200 and file ID
  `9839671`, accepting release metadata with three required and nine optional
  dependency relationships. Public-page readback returned HTTP 403; approval,
  public availability and stored relationship readback are not independently
  confirmed. [Receipt](curseforge-upload-response.json) and
  [verification limits](curseforge-verification.json).

Both API uploads used the existing service-specific credential files in the user's
home directory; credentials are not copied into these records. No browser login
was needed. Uploads completed on 2026-11-27 UTC (2026-11-26 local).

[Release notes](changelog.md), [dependency map](dependencies.json) and exact upload
metadata are retained here. Modrinth supports exact version references; CurseForge
file relations use project IDs, with qualified versions documented in the notes.
Sable bundles Companion and Veil; Create supplies its support stack. Optional
addons are not converted into hard requirements. No project replacement or duplicate
upload is authorized by an authentication or moderation failure.

Pre-existing `logs/` and all accepted worlds/evidence remain preserved. Platform
moderation status is distinct from successful upload and public availability.
