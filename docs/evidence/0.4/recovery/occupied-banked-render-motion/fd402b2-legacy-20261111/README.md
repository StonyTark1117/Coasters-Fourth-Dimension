# FD402B2 Legacy banked occupied render slice (2026-11-11)

This run records the current-source Legacy-family slice of the occupied banked recovery fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope. The implementation identifies Legacy as `ZACSPIN` in the validator output.

The banked recovery fixture and validation profile used 40 samples and an 8000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 3373 waited ticks with `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.500157527711858` rad, bearing bank `0.5235987367764133` rad, chassis motion `60.107675919976614` blocks, bearing motion `58.49623317061756` blocks, one mounted rider, and rendered bodies (`compiledBodies=4`, `nativeCartBodies=2`).

This is one bounded Legacy/ZACSPIN slice. It does not close the full B2 visual matrix or FD402B.
