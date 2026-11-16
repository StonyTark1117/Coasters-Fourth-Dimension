# Private native-GPU path — verified setup

Owner lead, primary main, 2026-11-15. This is display/tool readiness only, not
FD904B park acceptance. Default owned Xvfb322 uses llvmpipe. A forced Zink probe
identified the RX7900XTX but failed swapchain presentation (no DRI3), so that
path is rejected. No Minecraft benchmark was launched through it.

Official [VirtualGL3.1.5](https://github.com/VirtualGL/virtualgl/releases/tag/3.1.5)
was extracted to persistent task-local storage; no system package, desktop,
permission or display configuration changed. The documented
[EGL device backend](https://github.com/VirtualGL/virtualgl/blob/main/doc/advancedconfig.txt)
uses `/dev/dri/card1` (PCI0000:03:00.0) while the X11 target stays owned Xvfb322.
The initial extracted-library lookup failed; explicit `-ld <root>/usr/lib`
corrected it. `glxinfo` then reports AMD Radeon RX7900XTX/radeonsi/Mesa26.2.6.
The [rendered spheres image](gpu-spheres.png) proves actual drawing into the
private display. No user desktop/window/input was accessed.

Command wrapper: `<tools>/virtualgl/opt/VirtualGL/bin/vglrun -ld
<tools>/virtualgl/usr/lib -c proxy -d /dev/dri/card1 <application>`, with explicit
`DISPLAY=:322`; tools root is `/mnt/NVME4/fourthdimension-runs/fd10-lead-20261115/tools`.
VirtualGL copies GPU-rendered images into Xvfb; report transport overhead and CPU
frame-start intervals, not physical monitor presentation latency. Minecraft must
still confirm its own GL renderer/backend and successful frames before measurement.
An absent optional `hostname` utility emits a shell warning; explicit display and
transport prevent automatic host selection. The probe units
`fd10-lead-gpu-gears.service` and `fd10-lead-gpu-probe.service` were stopped and
verified inactive/MainPID0 after the screenshot.
