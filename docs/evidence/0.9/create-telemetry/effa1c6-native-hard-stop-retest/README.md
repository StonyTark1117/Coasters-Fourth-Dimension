# Native pending-realignment hard-stop retest

This is a **passing native regression**, scoped to the standalone development
profile. It does not replace the required packaged-client historical replay and
fresh support-loss recurrence.

Source: `effa1c63a668bf1856c567fc702105c01e9accd4`. Command:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runGameTestServer --offline --no-daemon --console=plain
```

The server completed **47 required GameTests** in **4.622 minutes**, with all
required tests passing. `OperationsGameTests` now injects native velocity after
exact support repair, while `alignmentRequired` remains true, and requires the
pre-realignment brake to clear every fixture chassis/carrier body using the
native hard stop. It retains fresh-path realignment for Legacy, Free Spin,
Revised, and PAX, while Arrow exercises the historical boundary-fault state.
The separate PAX support recovery test covers its four-car train.

The packaged artifact built from this source has SHA-256
`9014f8f27724536e088e3a3e546c20930dae45c0fa0b7e363fdb8e62c772de88`.
The raw GameTest log is retained. Run `python3 verify.py` from this directory to
verify its hash and the terminal pass record; it does not run Minecraft.
