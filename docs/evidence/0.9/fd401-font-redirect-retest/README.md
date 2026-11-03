# FD401 stale-font redirect retest

On 2026-11-02, the release worktree built the packaged combined Styles and
Engineered profile after `2be219e` and opened a clone of `FD16Recovery` under
Xvfb/llvmpipe. Three real `F3+T` reload inputs reached font generations 2, 3,
and 4. After generation 3 closed the old default set, pause-menu rendering
logged `FD_FONT_STALE_SET_REDIRECT` to the live set; it did not log
`FD_FONT_IMAGE_UNALLOCATED` or `Image is not allocated`.

The [pause capture](pause-after-reloads.png) shows the rendered Game Menu after
the third reload. The player then selected Save and Quit; the log records the
integrated server saving all dimensions. [events.txt](events.txt) is the
unique relevant log subset and [checksums.txt](checksums.txt) pins the complete
local log, tested JAR, and screenshot.

This is a targeted reproduction/retest of the observed `FontSet.getGlyph`
path. It does not establish every stale `FontSet` call path, all graphics modes,
or complete FD401 rendering acceptance.
