# FD402B3 Free Spin banked occupied render slice (2026-11-11)

This run records the current-source Free Spin slice of the occupied banked recovery fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

The banked recovery fixture and validation profile used 40 samples and an 8000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 3382 waited ticks with `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5000706599630573` rad, bearing bank `0.5235987571317782` rad, chassis motion `60.26500828992018` blocks, bearing motion `58.12480581300492` blocks, one mounted rider, and rendered bodies (`compiledBodies=6`, `nativeCartBodies=2`).

This is one bounded Free Spin slice. It does not close the full B3 visual matrix or FD402B.
