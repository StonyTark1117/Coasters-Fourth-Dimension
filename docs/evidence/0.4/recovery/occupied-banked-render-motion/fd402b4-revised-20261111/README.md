# FD402B4 Revised banked occupied render slice (2026-11-11)

This run records the current-source Revised slice of the occupied banked recovery fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

The banked recovery fixture and validation profile used 40 samples and an 8000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 3374 waited ticks with `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5002005189051474` rad, bearing bank `0.523598758593224` rad, chassis motion `60.221714473389845` blocks, bearing motion `58.23527297638994` blocks, one mounted rider, and rendered bodies (`compiledBodies=6`, `nativeCartBodies=2`).

This is one bounded Revised slice. It does not close the full B4 visual matrix or FD402B.
