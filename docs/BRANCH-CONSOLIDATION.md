# Branch consolidation — 2026-11-04

`main` is the only current development branch. The prior `codex/*` tips are
preserved by the `archive/branch-consolidation-2026-11-04` tag. Its marker commit
has each old tip as a parent, so deleting the old branch names does not discard
commits. This table records the exact names and object IDs for recovery.

`integrated` means the tip is an ancestor of `main`. `patch-equivalent` means
Git finds no unique patch against `main`; the exact old tip still remains in the
archive. `archived-only` means unique work was reviewed but was not brought
into the current release tree.

The archived-only lines are:

- `codex/coaster-integrations`: broad optional addon work beyond the current
  release scope, on an older core. It needs a separate compatibility review before
  any feature is recovered.
- `codex/integration-reconcile`: the formerly dirty worktree was committed as
  snapshot `f3303c7`. Its schema-7 functional provider registry and supporting
  fixtures are unfinished against the current schema-6 core. Existing ride
  telemetry and Create diagnostics are already present on `main`.
- `codex/engineered-release`: earlier Engineered presence and passenger restore
  changes use older callback and restoration paths. The current core has a
  different passenger restoration flow. Reintroducing these changes requires a
  focused review of current Engineered behavior.

The active primary checkout also has three preexisting uncommitted Engineered
motor-bound changes. They were preserved in place and are not part of the
published `main` commit.

| Former branch | Tip | Disposition |
| --- | --- | --- |
| `codex/client-repair-order` | `36c1760c36b44dc105e9d6cbec03387ea8559e75` | integrated |
| `codex/coaster-integrations` | `8f97fc211fc61d807100c9fadafc5f0b2439469e` | archived-only |
| `codex/create-telemetry-release` | `871a2a83d3f363ab83a3c4772f08fbe614540a41` | patch-equivalent |
| `codex/curve-provenance-negative` | `6aa3ddfc903ac2edb80a27ba7bd59874a5534487` | patch-equivalent |
| `codex/deterministic-historical-fixture` | `86ec166d83b5afa878ef4779f3760702ba53c3a2` | integrated |
| `codex/engineered-fe-fixture-isolation` | `f56c5c20d99eed011f5901c2a9ebf2db82fb656c` | integrated |
| `codex/engineered-physical-availability` | `392a6573a4228fddd5ba8d45880a67320e8982ab` | integrated |
| `codex/engineered-release` | `b0d894b68a312bfc0883d3e1f6b097fcb826a3c4` | archived-only |
| `codex/fd-0-4-rider-coverage` | `24a71a6f10a9a8a88ef0fa28883e0337cdf382b3` | integrated |
| `codex/fd04-arrow-client-candidate` | `5097c3c22658e5573d72c78cecd0cee32d0c0cc7` | integrated |
| `codex/fd04-arrow-integration` | `d9d24c25dbbd583ef59626ebe9d2330d333fd132` | patch-equivalent |
| `codex/fd401-engineered-client-operator` | `455e478af09a435e4c5817a7c56f57689579e20e` | integrated |
| `codex/fd401-engineered-fe-reload` | `90814da0f8361cbb81577398ea9152f228f1e7c7` | integrated |
| `codex/fd401-engineered-loop-fixture` | `5ac4162526290207abcc4469788b0d5f8b72d41f` | patch-equivalent |
| `codex/fd401-fe-network` | `218c294e98ee45bb2d0aa3ca2aa2ab44bd958600` | patch-equivalent |
| `codex/fd401-final-retest` | `55730f21e3c6f2314644fbcb52314286143998be` | patch-equivalent |
| `codex/fd401-handoff-sync` | `a6bdf210798b756654046a87d244bf218b59693b` | patch-equivalent |
| `codex/fd401-missing-font-final` | `b9a0053b5e58faee281b0c694f46bada9d832dcc` | patch-equivalent |
| `codex/fd401-removed-font-retest` | `159aa6ce1608cfdd9e18b28c3616947b2e223810` | integrated |
| `codex/fd402-rider-rack-audit` | `9cbbec22cff8be7aa5572486c918aab3928c7ceb` | patch-equivalent |
| `codex/fd403-client-acceptance-plan` | `0796426c070deb3140f63b749be230547f5cf4da` | patch-equivalent |
| `codex/fd901-ordinary-coupling-direction` | `d25105ab3b00c0820a0a09fc44feddd9f91c61d0` | patch-equivalent |
| `codex/fd901-second-binding-assertion` | `148d90d2a10bdddfbc12aef0aaf53f3ae5e8411a` | patch-equivalent |
| `codex/fd902-dirty-seat-cleanup` | `9cdcbc3f7515908771ab2e545fb137df6fc75948` | patch-equivalent |
| `codex/fd902-unique-rider-assignments` | `622760435c46cc5a08a7f3141c1d0f713b080641` | patch-equivalent |
| `codex/fd903-duplicate-rail-id-guard` | `fa5b63ed82c7525924e641b029a8000f5b988e86` | patch-equivalent |
| `codex/fd903-historical-client-ride` | `0769ba0335422edd07fcbc26bf8ecf4cb8f7474c` | patch-equivalent |
| `codex/fd903-migration-doc-accuracy` | `3de381f568dca4447068814c072429a92b2b3236` | patch-equivalent |
| `codex/fd903-native-body-identity-guard` | `9358585a4ca3de0f285b09956d4d279ee7eee1e3` | patch-equivalent |
| `codex/fd903-native-historical-replay` | `d1e72469ad8a6f32e20d61a6fd3a74d7ba5345c7` | patch-equivalent |
| `codex/fd904-render-evidence-accuracy` | `111e5962e4f81ce87df7f7b7f4ea013bdbc85a64` | patch-equivalent |
| `codex/fd904-render-stages` | `f47f342f8e8536cae6369f591bc39c09de57feb5` | integrated |
| `codex/foreign-material-guard-test` | `1117740efdcb600ef0656aeabc032e9d4fd3141c` | patch-equivalent |
| `codex/foundation-fd001-fd008` | `9273358ac4117d14f5350689bce830f6e1044053` | integrated |
| `codex/gzip-fixture-portability` | `78a639b312f6fd5a8ad3199030c62d4f14dc241a` | patch-equivalent |
| `codex/integration-plan-sync` | `4794bd05f846f518c33136443f80267bf0e0ab32` | patch-equivalent |
| `codex/integration-reconcile` | `f3303c75f9486af7faa1c970bd32722cc3e3655c` | archived-only |
| `codex/legacy-backfill-307ec35` | `18f5575ff55268b1f7370cfa7ecc28ee9a576208` | patch-equivalent |
| `codex/legacy-curve-backfill` | `02541fbcfc59d4d5cf73404707807945c48342e3` | patch-equivalent |
| `codex/legacy-material-guard` | `e31b8455dda41f10ceca762b96135bb6fbf6cf17` | integrated |
| `codex/local-0-9-integration-preflight` | `873de544f71eb87e49e23a047d97c5ef909065a0` | integrated |
| `codex/magnetic-fd201-fd204` | `d97ea010e842b931dc006aad7e9002122dfdc8e1` | integrated |
| `codex/pax-active-restart-7958cdd` | `580b453cedd8f868100c33716a57040b3bbec774` | patch-equivalent |
| `codex/pax-active-restart-a1aeb1a` | `a1aeb1a6fc1a1739efc03cede059b0c118879bf7` | integrated |
| `codex/pax-curve-restore-8a7ffdb` | `2f876731b27b9da116c32e6988103bad7931b0b2` | patch-equivalent |
| `codex/pax-normal-restore-probe` | `a1aeb1a6fc1a1739efc03cede059b0c118879bf7` | integrated |
| `codex/pax-repair-lifecycle-311078e` | `c90678c32ffca445e0e089a0b364a48f7ea40427` | patch-equivalent |
| `codex/playable-fd101-fd109` | `81895e5b7dc59918fcad869804048c5669e90d0e` | integrated |
| `codex/profile-matrix-60a3abf` | `e88e323f937eced4f6d337b37aa0918bd36f1741` | patch-equivalent |
| `codex/profile-matrix-abc4a9a` | `d4e144b0c8ae4dc7a96c7d4f418e3189be0b838f` | integrated |
| `codex/release-0-9-0` | `e8c05aed170c3389013f22536be7419da4eeefb3` | integrated |
| `codex/repair-toast` | `b6485e572547ca1422da6ce337b8d61eb7f2a9b7` | patch-equivalent |
| `codex/roadmap-matrix-docs` | `fb92838945bbe68ffe4db6e8f252e0644e479fe4` | patch-equivalent |
| `codex/support-repair-guards` | `e024692934c37473ec901580d7ac551fec3a0e0c` | integrated |
| `codex/survival-client-gate` | `dddd2d008607429fd8472fe444e55dc17bc142ec` | integrated |
| `codex/telemetry-historical-311078e` | `e2d64eedfb75f62c664163813b4de26ec462f5ee` | patch-equivalent |
| `codex/telemetry-integrated-acceptance` | `d61c6d6098abaec82d7f02cd4e9e68b193087a69` | patch-equivalent |
| `codex/telemetry-realignment-09690c1` | `cb2935fc3dc19ae2fbcabc88d08d8ac5db30337f` | patch-equivalent |
| `codex/telemetry-realignment-effa1c6` | `80a43a688551d98f842720ae00a5f6e273847c74` | patch-equivalent |
| `codex/telemetry-realignment-retest` | `3aae41553b33a36c99758a215a16fbc79a4f3b3f` | integrated |
