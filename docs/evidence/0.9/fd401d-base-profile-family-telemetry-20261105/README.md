# FD401D base-profile family telemetry

One named headless GameTest passed on 2026-11-05 in the base profile, with no Track Styles, Engineered, or Create Addition FE network installed. The selected production JAR loaded with a test overlay replacing only `RideTelemetryGameTests.class`; all production entries were byte-identical to the selected JAR.

The test covered occupied snapshots for a two-car Arrow, Legacy (`ZACSPIN`), Free Spin, Revised (`ZACSPIN_REVISED`), and a four-car PAX consist. For each family it checked all six native Create Display Link formatters, goggles formatting, snapshot serialization and update-tag packet round-trip. It also exercised station dispatch authority, read-only telemetry, recovery guards, stale-snapshot rejection, and PAX BOARDING/LIFTING/FAULT authority and output.

This closes one base-profile server-side family formatter/goggle/update-tag slice. It does not show a sign rendering those values or a remote client's applied packet, and it does not establish the Styles-only, Engineered-only or combined client/profile matrix.

The selected clean JAR hash is `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`; the one-class overlay hash is `b959b57014f4ed51b86821693c7045c604a4f54a49f76b692145d08074b663e3`. The full server log, completion-marker excerpt, run command, and fresh server world are retained beside this file.
