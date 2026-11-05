# FD401D Engineered-only family telemetry

One named headless GameTest passed on 2026-11-05 with Engineered 1.1.1 installed and Track Styles/Create Addition absent. The selected production JAR loaded with a focused test overlay replacing only `RideTelemetryGameTests.class`; every production entry was byte-identical to the selected clean JAR.

The test covered occupied snapshots for a two-car Arrow, Legacy, Free Spin, Revised and four-car PAX consist. It checked all six native Create Display Link formatters, goggles formatting, telemetry serialization and station update-tag packet round-trip for each family. It also exercised station dispatch authority, read-only and recovery guards, and PAX BOARDING/LIFTING/FAULT authority and output.

This closes one Engineered-only server-side family formatter/goggle/update-tag slice. It does not show a rendered sign or remote client's applied packet and does not establish the full Engineered-only client profile acceptance.

Selected clean JAR SHA-256: `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`. One-class overlay SHA-256: `b959b57014f4ed51b86821693c7045c604a4f54a49f76b692145d08074b663e3`. The complete run command, mod list, completion markers and fresh server world are retained in this folder.
