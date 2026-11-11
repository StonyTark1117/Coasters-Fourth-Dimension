# FD402B1 Arrow banked occupied render slice (2026-11-11)

This run records the current-source Arrow slice of the occupied banked recovery fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

Command profile used the banked recovery fixture and validation flags from the parent acceptance command, with `-PfdCarrierFixtureRecoveryDesign=ARROW`, `-PfdCarrierValidationSamples=40`, and `-PfdCarrierValidationTimeout=8000`; recovery-cycle flags were disabled so the bank traversal could complete.

The validator passed at 40 samples after 3354 waited ticks. It recorded `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5001305974919313` rad, bearing bank `0.523598736776414` rad, chassis motion `60.29566241145146` blocks, bearing motion `58.65607755204405` blocks, one mounted Arrow rider, and rendered bodies (`compiledBodies=4`, `nativeCartBodies=2`). The client exited automatically after `FD_CARRIER_VALIDATION passed`.

This is one Arrow slice only. It does not close the all-family B1 matrix or any other FD402 gate.
