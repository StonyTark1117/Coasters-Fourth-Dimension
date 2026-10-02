# FD902 two-client late-join snapshot replication

This run used a dedicated NeoForge server on `127.0.0.1:25581`, loading an
isolated copy of the `FD16Recovery` packaged world. The server and both clients
ran Fourth Dimension `0.9.0-dev.27` with Create `6.0.10`, Coasters Simulated,
Track Styles `1.2.0`, and Coasters: Engineered `1.1.0`.

`Dev` joined at 23:32:42. `Observer` was started separately and joined later at
23:38:54. Each client logged the same six recovered 4D body identifiers:

```
7b127e8a-ef8f-4435-a964-1d051ae712b2
ec4fc0c8-0a30-4a3f-b82f-f16fdedbeea8
99b53f5f-911a-40a5-a8e7-ea03288412a0
445b14dc-4fa1-449b-a12e-36f77295a085
8fd75f00-d093-44d3-8eee-76cfa7291d85
b4f95768-cd43-4797-906c-2a686d30ad53
```

On the late join, the client diagnostics showed every snapshot's reported
angle agreeing with its locally restored native angle to the printed
precision. All six were unoccupied (`occupants=[]`, `riding=false`), which is
the intended state for this recovery scene.

## Result

The dedicated server admitted both clients and a client that joined six minutes
after the first independently restored the complete six-body snapshot set.
This provides a live counterpart to the dimension-scoped packet admission
contract: a late join receives a full current snapshot rather than relying on
the first client's local state.

## Limits

The scene intentionally contains no boarded car, so this does not prove rider
ownership, duplicate-passenger prevention, or camera restoration across two
clients. It also does not exercise disconnect/reconnect, chunk unload, or an
actual dimension transition while a snapshot is queued. Those cases remain
required FD902 acceptance work.
