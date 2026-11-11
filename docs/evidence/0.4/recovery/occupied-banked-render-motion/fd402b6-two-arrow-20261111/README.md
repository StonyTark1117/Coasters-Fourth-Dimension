# FD402B6 two-car Arrow banked occupied render slice (2026-11-11)

This run records the current-source two-car Arrow slice of the occupied banked recovery train fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

The train fixture and validation profile used 40 samples and a 10000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 4201 waited ticks with `recoveryTrainRequired=true`, `recoveryTrainSeen=true`, `recoveryTrainMotionSeen=true`, `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5001137402397517` rad, bearing bank `0.5235987367764142` rad, two moved Arrow chassis, three rendered native cart bodies, and motion distances of `71.5476346585566`, `69.17776387525612`, and `66.87923000699706` blocks.

This is one bounded two-car Arrow slice. It does not close the full B6 visual matrix or FD402B.
