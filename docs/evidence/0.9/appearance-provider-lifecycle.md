# FD903 appearance-provider removal boundary

Fourth Dimension owns the functional rail, its family, variant, profile and
binding. A Track Styles selection is persisted only as cosmetic intent. That
separation means a world can remain usable after Track Styles is removed; the
client falls back to the original rail decoration until a compatible appearance
provider is available again.

## Evidence

The existing recorded lifecycle began with the real schema-4 five-family world,
selected `coasterssimulatedextratypes:arrow_4d` with tint `#4A90E2`, saved,
then exited. The same world was opened and saved with Track Styles removed, then
opened and saved again after reinstalling Track Styles 1.2.0. The four recorded
saves retain all 35 rail IDs/profiles/mechanisms, eight vehicle bindings and
twenty carrier records. The appearance intent is unchanged; no functional
provider was introduced into the world data.

The committed verifier rechecks those saved observations:

```sh
python3 scripts/verify-appearance-lifecycle.py
```

The current schema-6 `AppearanceGameTests` now also loads each persisted Track
Styles appearance identifier (`arrow_4d` and `arrow_4d_dbl_spine`) plus a future
unknown provider while running with Track Styles absent. For every case it
requires the appearance intent, rail UUID, family, variant and rotation profile
to survive unchanged.

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon
```

The standalone GameTest profile passed all **39 required GameTests** in
**3.830 minutes** and reported:

```
FD_APPEARANCE schema=6 oldSchemas=1..5 missingProviderAppearances=3
mechanicsUnchanged=true styleFamilyCases=10 strokeCases=0
```

The combined Track Styles 1.2.0 and Coasters: Engineered 1.1.1 profile also
passed all **39 required GameTests** in **4.042 minutes**, with both Track
Styles appearance strokes exercised:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --no-daemon \
  -PfdIntegrations=styles,engineered
```

```
FD_APPEARANCE schema=6 oldSchemas=1..5 missingProviderAppearances=3
mechanicsUnchanged=true styleFamilyCases=10 strokeCases=2
```

## Limits

The recorded lifecycle is schema 5 and the current schema-6 check is a native
saved-data regression, not a packaged historic-world replay. This establishes
the supported cosmetic-provider boundary only. It does not convert, retain or
certify any third-party functional track blocks, and it does not replace the
remaining FD903 historic-world and external-conversion acceptance work.
