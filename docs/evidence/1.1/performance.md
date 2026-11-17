# 1.1 representative tools performance

Three predeclared 60-second windows in one stationary scene: two FD chassis, six carrier bodies, all captured and empty. This is an affected-tools comparison; the accepted 1.0 expanded-park result remains its own evidence. It does not measure GPU presentation or establish new universal FPS budgets.

Java 21; native Radeon RX 7900 XTX through private VirtualGL; Ryzen 9 7900X3D; 854×480, GUI 2, render 8/simulation 5, 60 FPS cap, no shader. Client heap 1536M/scope 6 GiB; server heap 2048M/scope 4 GiB. No competing graphical jobs. More than 60 seconds of warmup preceded the first window. UI transitions are excluded.

| View | Frames | Frame median / p95 / p99 (ms) | Ticks | Tick median / p95 / p99 (ms) | >50 ms frames / ticks |
| --- | ---: | --- | ---: | --- | --- |
| original | 3579 | 16.761 / 19.133 / 19.581 | 1200 | 4.036 / 4.993 / 5.727 | 0 / 0 |
| ocean | 3580 | 16.758 / 18.826 / 19.221 | 1200 | 3.802 / 4.878 / 5.506 | 0 / 0 |
| operator | 3586 | 16.729 / 18.812 / 19.200 | 1200 | 3.780 / 4.665 / 5.159 | 0 / 0 |

Each window maintained 20 server ticks/second. Colored and operator-panel p95/p99 CPU frame intervals and server work ticks were no higher than the Original window. Maximum frame interval was 23.797 ms; maximum server tick was 41.402 ms. This supports bounded overhead acceptance for these tools, not equivalence with the much larger fixed park.

| View | Client / server peak RSS (MiB) | Client / server median post-GC heap (MiB) |
| --- | --- | --- |
| original | 2286.0 / 1271.0 | 598.9 / 382.2 |
| ocean | 2319.8 / 1307.6 | 581.2 / 365.2 |
| operator | 2303.9 / 1307.1 | 579.7 / 364.3 |

Maximum observed sampled client/server cgroup usage stayed below 2.15/1.28 GiB. Median post-GC heap did not rise with the tools; these short samples do not replace the accepted sustained-window memory claim. All four raw streams have complete footers, no collector errors and zero reported JVM event drops. The resource sampler expired after its original 420-second duration during inspection; a second sampler covered both later windows. Every window has 60 resource samples per process.

[Full distributions and collector footers](performance.json), [protocol](protocol.json), [exact windows](windows.jsonl), [client events](client-performance.jsonl.gz), [server events](server-performance.jsonl.gz), [initial resources](resources.jsonl.gz), [later resources](resources-final.jsonl.gz). Raw events include heap, JFR GC/post-GC, RSS and cgroup data.
