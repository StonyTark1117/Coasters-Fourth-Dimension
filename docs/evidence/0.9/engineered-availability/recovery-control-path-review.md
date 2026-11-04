# Engineered recovery control paths — source inspection only

Inspected 2026-11-03 while the shared runtime was occupied. The exact Engineered
1.1.1 artifact is CurseForge file 8863559, SHA-256
`eea19ed769ed573727b4e15184c229f66aff85f20667464650724e1b2d657f5d`.
A Python classfile reader checked the installed artifact's constants and bytecode;
no JVM was launched and no upstream code/assets are redistributed by this note.
Local inspection files remain under `/tmp/fd-chain-inspection/`.

## What the artifact establishes

- `LinearMotorControllerBlockEntity.commandValue()` returns the active
  `MotorStep.value()` integer unchanged.
- `MotorDrive.launch()` converts that value directly to a double target for
  `MotorForce.accelerate()`. `CartSpeed.velocity()` reads native pipeline linear
  velocity without a unit conversion. The config describes controller speed as
  blocks/s. A LAUNCH target of 1 therefore must not be described as below Fourth
  Dimension recovery's 0.75 blocks/s cap.
- The controller also supports STOP, which reaches the positioning/creep path
  in `MotorDrive.park()`. This path reads `CoastersEngineeredConfig.CREEP_SPEED`.
- The provider defines `creepSpeed` with default **3.0** and supported range
  **0.5–20.0**. A separately declared configuration using **0.5** is a candidate
  for within-cap recovery positioning; default configuration is different.

The LAUNCH integer limit does not establish that every Engineered recovery
control path must be disabled. Conversely, a supported configuration range does
not prove that a loaded tow behaves correctly on that path. Keep the existing
healthy direct-callback controls and their bounded meaning; do not replace them
with rejection checks merely to obtain a passing FE test.

## Required runtime evidence

Before claiming a healthy FE recovery workflow, choose an actual provider
control sequence and declare its configuration. Require meaningful native
bearing/chassis motion, intact coupling and rider mount, active recovery state,
no fault and speed within the cap throughout the relevant interval. A displacement
threshold alone can accept a tow that already faulted. Preserve explicit brake,
empty-buffer starvation, supplied operation and operator-resume checks.

For a STOP/creep candidate, verify the position target, approach direction,
settling behavior and any reverse correction; do not infer these from the method
name. Restore modified provider config after the test. Energy observations must
distinguish net buffer depletion from gross consumption that could be offset by
regeneration.

This note does not certify STOP/creep recovery, change production policy, withdraw
an advertised capability or close FD401/FD402. It identifies a concrete alternative
to investigate before making such decisions. Natural FE operation and the merged
release regression remain pending at this inspection checkpoint.
