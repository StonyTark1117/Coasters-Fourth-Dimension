# FD917 bounded remote observer

The shared occupied restraint transition and PAX remote lift/release plus
fresh-process late-join passed on candidate03. This is not whole-package closure.

Owner: lead chat `01a1dd1d-1468-7f81-8f31-5532ad7a178d`; own client/Xvfb318 under
`/mnt/NVME4/fourthdimension-runs/fd917-lead-observer-20261115`, units
`fd917-lead-observer-20261115-{client,xvfb}.service`. Server/world and Dev remain
Mechanisms-owned at127.0.0.1:25656. The local observer-control.py reuses the project
private XTest helper, validates the remote owner's unit/root/cgroup and only reads
its player list. It cannot launch or mutate that server.

Exact JAR SHA256 `1aaf423428c6138218867a391a394e6b6b39b354faad1080ee282ed4dec16506`.
Java21, actual MaxHeap1610612736, client-only6GiB cgroup,854x480,60fps cap,
render distance2. Requested simulation distance2 was rejected by MC's [5:33]
option schema; no simulation-distance2 claim is made. Dedicated-server watched
chunks are controlled by view distance. Initial host available memory25GiB;
observed client cgroup peak2.53GB. Evidence files include VM.flags and scope readback.

## Remote occupied restraint

Mechanisms positioned spectator Observer at(-391.5,108,467.5),yaw53.13/pitch15.
Both client world join and dedicated server `list` confirmed Observer and Dev.
Dev normally occupied the outer right lead PAX seat before recording. This is a
view of another real player; the Observer did not board, teleport a rider, or
claim normal-use input proof. The four-car fixture and camera are disclosed setup.

[Open](pax-occupied-open.png) and [closed](pax-occupied-closed.png) views show the
rider retained in the same green shell with a single bar owner. Normal Close was
sent at19:23:35.562UTC; the server recorded action11 success at12:23:35.630MST for
lead `e0cfa460-0d88-4620-a11a-3f9eac351bbd`. The [transition detail](pax-restraint-transition.png)
shows the bars lowering in the remote client while the rider stays attached;
there is no leftover raised bar at the endpoint. This supplies the new shared
restraint interpolation/ownership observation without repeating all families.

Full75-second15fps `evidence/occupied-restraint.mp4` remains in the persistent
observer root. Its bounded detail covers16.8–18.8seconds at5fps in reading order.
The old copper controller casing in this candidate is explicitly excluded from
final-art credit; Art separately corrected that resource. Green shell, seat,
receiver reserve and bar geometry are current for this claim.

The observer runtime has stopped; see the final cleanup below. No server handoff
or permission to stop Mechanisms is implied.


## PAX remote lift/release

Observer moved to(-390.5,130,413.5),yaw47/pitch4, requested render distance2.
The winch at(-406,105,473) is about60blocks away and four chunk indices behind
this camera, outside that requested watch range. The native server forced the
fixture corridor; that does not make those chunks watched by this client.

Normal Dispatch succeeded at19:29:10.651UTC (server action13). The continuous
[sequence](pax-first-spike-sequence.png) shows the occupied linked train reaching
the first spike with its guide/catch assembly, then travelling away separately
while the carriage remains. [Full-resolution approach](pax-first-spike.png).
The source reads projected actual constraint state; the owner's NBT trace covers
LIFTING, OUTBOUND, CAPTURING and BOARDING with cycles1/no fault by19:29:43.
`catchEngaged` is transient presentation state, not an NBT field, so the NBT
record alone is not claimed as a direct catch-boolean measurement.

Full108.267-second15fps video `evidence/pax-far-spike-cycle.mp4` remains in the
observer root; the sequence uses seconds26–41 at1fps. The first empty120-second
recording preceded Dispatch and contributes no motion claim. The successful
recording was stopped with SIGINT after the completed cycle; ffmpeg finalized
the MP4, verified by ffprobe and decoded frames. No recorder remains active.


## Fresh-process late-join and cleanup

After the full cycle, Mechanisms held the same occupied train closed in BOARDING,
cycles1/no fault, and positioned Observer at(-390.5,108,467.5),yaw47/pitch4.
The [before image](pax-retained-catch-before.png) shows the retained carriage and
catch at the lead mast. Lead saved its log and stopped only its own client.
Simulation distance was corrected to5; render distance remained2. A fresh Java21
process used the identical candidate03 JAR and joined the still-running server.
The [fresh-join image](pax-retained-catch-rejoin.png), after client world-join and
server player-list confirmation, shows the same retained catch, closed bars and
real rider. No old client cache or synthetic passenger supplied that view.

The owner subsequently moved guide contacts from longitudinal±.75 to±1.25 to
clear native axle wheels and added a distinct RPM input-shaft coupling. These
geometry corrections require its one updated station view; this protocol/state
observation does not claim final contact credit for the superseded geometry.
The controller's tiny texture aliases are likewise covered by Art's later view.

Both owned observer units are inactive, MainPID0; [cleanup readback](runtime-cleanup.txt).
No ffmpeg process remains. Client logs, videos, exact launch files, VM flags,
cgroup readbacks and screenshots remain in the persistent observer root. Its
player state is in Mechanisms' retained world; that owner continues to control
and ultimately save/stop the server. No runtime was left idle or handed off.
