# FD401D selected-JAR Legacy approach retry — blocked at seat targeting

Run date: 2026-11-05. This was a targeted follow-up to the prior bounded replay.
It used a fresh server/game directory, port, display, and Java 21 client scope,
leaving the earlier evidence bundle unchanged.

## Identity and run setup

- Selected JAR: `coasters-fourth-dimension-0.9.0-dev.27.jar`, SHA-256
  `656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`.
- Input world: the committed prior bundle's original `input-world.tar.gz`, SHA-256
  `7d5b707ab3a83f440efe0ba3ba70516b7f72efa970709fac578aca430154442a`.
- Server used private port 25715 and RCON 25716 in a 4 GiB scope; no RCON secret
  is included. Dev used the isolated game directory, Java 21, `-Xmx1536M`, its
  own 6 GiB systemd scope, and the owned private Xvfb display :344.
- Before Dev launch, `free -h` reported 20 GiB available and the user.slice
  cgroup was unlimited at about 50.7 GB current usage. The other Minecraft
  process was FD402's client (about 2.66 GB RSS). Dev's owned scope was verified
  active at 6 GiB cap (peak 1.8 GiB). The client options file records render
  distance 6 and simulation distance 5.
- The server log confirms the exact selected artifact loaded from this fresh
  build path. Launch argument files and direct Java wrappers are retained under
  `launch/`.

## Targeting result

Dev joined and was teleported to the historical successful approach coordinates
`(131.5,87,31.5)`. The older accepted log records this position at 05:30:52 and a
normal Legacy mount 22 seconds later, at seat
`BlockPos{x=20481032,y=127,z=20489225}`. The old record does not provide a view
rotation at the successful click. The copied input world's `playerdata` entry
for Dev (UUID mapping in `server-usercache.json`) records a saved pre-run
rotation of yaw `0.4547°`, pitch `89.9843°`; this does not establish whether the
earlier operator changed view after teleport. For this replay, the player was
oriented toward the visible Legacy platform and the target was read from F3
before any interaction:

- At yaw 87°, pitch 24°, F3 reported `Targeted Block: 120 83 32 minecraft:stone`;
  `RootVehicle` was absent.
- One bounded aim correction at the same position (yaw 85°, pitch 28°) reported
  `Targeted Block: 122 84 32 create:display_link`; no interaction was sent.
- Neither screenshot identifies the visible car piece as a `create:seat` entity.
  No right-click was sent at either block, and the server log contains no
  `FD_BOARDING_EVENT ... action=mount` for Dev.

This fails to reproduce the historical client aim. The run stopped at the
requested probe boundary. No Observer was launched and no Legacy occupancy,
Free Spin, Revised, goggles/snapshot, or trailing Arrow occupancy transition was
attempted.

Dev disconnected to the title screen and quit normally. The server received
`stop`; the final log records all dimensions saved and the RCON listener stopped.
`final-saved-world.tar.gz`, full server/client logs, the target screenshots,
options and launch metadata are retained here. This is blocker evidence, not a
passing FD401D matrix result.
