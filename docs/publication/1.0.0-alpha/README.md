# 1.0.0 Alpha submissions

The human authorized initial Modrinth and CurseForge pages and an alpha upload on
2026-11-16. No release tag or GitHub Release was created. Full moderation approval
is outside this task; these records establish submission, not approval.

- Modrinth: https://modrinth.com/mod/coasters-fourth-dimension
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/coasters-fourth-dimension
- CurseForge author file management: https://authors.curseforge.com/#/projects/1809752/files

See `submission.json` for project/version/file IDs and observed statuses,
`artifact.json` and `SHA256SUMS` for the exact CC0 build, and `description.md` /
`changelog.md` for listing content. The native icon was generated with the built-in
image tool; its source prompt and provenance are in `assets-source/publishing/`.

The Modrinth upload response hash matched the local SHA-512. Modrinth v3 version
metadata explicitly records the client-and-server environment. The project was
submitted for review and returned `processing`.

CurseForge used the author Upload API and `X-Api-Token`, not the downloader API.
The first request was rejected before upload because dependency project IDs were
strings; the corrected integer IDs returned HTTP 200 and file ID 9645712.
The successful request metadata and response are saved beside this record, without
authentication material.

The CurseForge license list did not offer CC0 by name. Its Custom License text
explicitly identifies CC0 1.0 Universal, links the canonical legal code and full
repository LICENSE, and preserves separately installed dependencies' licenses.
Third-party distribution is enabled.
