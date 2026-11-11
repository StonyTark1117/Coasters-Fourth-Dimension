# FD402B current banked render matrix (2026-11-11)

Seven current-source client runs now cover the declared occupied banked fixture matrix. Each run used Java 21, a private Xvfb display, a 1536 MiB client heap inside a 6 GiB client scope, 40 rendered samples, and the production recovery attach/start path. Recovery-cycle automation was disabled so the train could complete the sustained bank traversal.

| Case | Fixture family | Result | Rendered motion evidence |
| --- | --- | --- | --- |
| B1 | Arrow single | validator passed | chassis 60.30 blocks; bearing 58.66; chassis/bearing bank 0.50013/0.52360 rad |
| B2 | Legacy (`ZACSPIN`) single | validator passed | chassis 60.11; bearing 58.50; bank 0.50016/0.52360 rad |
| B3 | Free Spin single | validator passed | chassis 60.27; bearing 58.12; bank 0.50007/0.52360 rad |
| B4 | Revised (`ZACSPIN_REVISED`) single | validator passed | chassis 60.22; bearing 58.24; bank 0.50020/0.52360 rad |
| B5 | PAX single | validator passed | chassis 60.30; bearing 58.66; bank 0.50015/0.52360 rad |
| B6 | two-car Arrow | validator passed; train seen and moved | two Arrow chassis moved 71.55 and 69.18 blocks; bearing 66.88; bank 0.50011/0.52360 rad |
| B7 | four-car PAX | validator passed; train seen and moved | four PAX chassis moved 62.10, 60.36, 58.56, 57.09 blocks; bearing 63.62; bank 0.50013/0.52360 rad |

Per-run logs and commands are in the case directories beside this file. Existing Arrow and PAX fresh-process/two-client evidence supplies normal release, unload, and rider identity observations for representative trains. The validator passes alone do not close FD402B; combined with the retained release, substep, model-clearance, and two-client lifecycle records linked above, this cumulative package closes the FD402B roadmap criterion.
