# FD904 packaged five-family fixture render checkpoint

This bounded FD904 checkpoint exercises the original-track render gate in the
same fresh fixture used for five-family carrier-transform validation. It
records cumulative original-track submission from a client world containing all
current functional families. It does not inspect vehicle meshes or passengers.

## Method

The packaged JAR SHA-256 was
`9afa33c1ce44b21959db4b99595a2155a5f913ef1609a4352c907a7d4557de12`.
A fresh disposable `FDCarrierRenderMatrix` quick-play world was launched under
Xvfb:

```sh
xvfb-run -a env JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
  ./gradlew runClient --no-daemon -PfdPackaged \
  -PfdWorld=FDCarrierRenderMatrix -PfdCarrierFixture \
  -PfdRenderValidation -PfdRenderValidationExit \
  -PfdRenderValidationStages=100
```

The opt-in fixture rejects a non-empty world and creates Arrow, Legacy, Free
Spin, Revised, and PAX examples: eight vehicles, 20 carrier bodies, and 35
original rails. The version of `RenderValidation` used here required at least
100 live original-track render stages, a mesh build, and cumulative in-range
rail and procedural-quad totals of at least 100 each before requesting normal
client shutdown.

## Result

The packaged client passed after 101 stages. It recorded 34 mesh builds,
3,422 in-range rail observations, and 9,939,376 procedural quads, with average
mesh-build and render-stage times of 1,061,793 ns and 27,253,422 ns respectively.
The integrated server then saved `FDCarrierRenderMatrix` and Gradle completed
successfully. The selected source-log lines are retained in
[the five-family fixture render record](client-render-five-family-fixture.txt).

The count of 3,422 observations across 101 stages shows that more than 33 rails
were in range per stage on average. The default 12-chunk camera/frustum did not
make this an all-35-rails-per-stage assertion.

## Limits

This is llvmpipe/Xvfb instrumentation of cumulative original-track geometry
submission. The counters do not establish nonempty output on every stage. A
later `geometryStages` counter and stronger gate require a fresh packaged-client
replay for evidence of at least 100 stages with both an original rail and a
procedural quad. This run does not establish that stronger condition. It
does not prove a full camera sweep, visual quality, vehicle/rider/linkage
meshes, rendered clearance, frame rate, shaders, decoration rendering, or a
hardware target. Those remain FD402 and FD904 work.
