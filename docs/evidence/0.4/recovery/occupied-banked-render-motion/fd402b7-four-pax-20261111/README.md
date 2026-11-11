# FD402B7 four-car PAX banked occupied render slice (2026-11-11)

This run records the current-source four-car PAX slice of the occupied banked recovery train fixture under a private Xvfb display with a 1.5 GiB client heap and 6 GiB client scope.

The train fixture and validation profile used 40 samples and a 10000-tick timeout, with recovery-cycle flags disabled so the route could traverse the bank. The validator passed after 3769 waited ticks with `recoveryTrainRequired=true`, `recoveryTrainSeen=true`, `recoveryTrainMotionSeen=true`, `recoveryLinkSeen=true`, `recoveryMotionSeen=true`, `recoveryBankRequired=true`, chassis bank `0.5001287198427979` rad, bearing bank `0.5235987367764136` rad, four moved PAX chassis, five rendered native cart bodies, and motion distances of `62.099436995242975`, `60.35851633808838`, `58.56138376439452`, `57.0859220997591`, and `63.62173872024077` blocks.

This is one bounded four-car PAX slice. It does not close the full B7 visual matrix or FD402B.
