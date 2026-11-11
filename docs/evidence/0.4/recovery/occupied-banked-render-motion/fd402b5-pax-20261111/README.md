# FD402B5 PAX banked occupied render slice (2026-11-11)

This run records the current-source PAX slice of the occupied banked recovery fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

The banked recovery fixture and validation profile used 40 samples and an 8000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 3354 waited ticks with `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5001544288712036` rad, bearing bank `0.5235987367764138` rad, chassis motion `60.29742680059091` blocks, bearing motion `58.65754000235057` blocks, one mounted rider, and rendered bodies (`compiledBodies=4`, `nativeCartBodies=2`).

This is one bounded PAX slice. It does not close the full B5 visual matrix or FD402B.
