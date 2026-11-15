# FD904C1 world-stable rider view — complete

Owner01a1dd1d-1468-7f81-8f31-5532ad7a178d, primary main. Required occupied Wing
seat/pose handoff is integrated in cefd4fae; see ../wing/occupied/README.md.

K toggles a saved client preference, configurable in normal Controls. The camera
inherits one world-upright heading captured at activation/boarding. It preserves
ordinary mouse look and native eye position but intentionally does not follow
ride yaw, pitch or roll. It applies only to the local camera while mounted to a
known Fourth Dimension carrier. Dismount, death, world exit and a new mount clear
the cached frame. Server motion, rider/body rendering and remote players are
untouched. No camera state is persisted or sent to the server.

Pinned Sable2.0.3 inspection shows that CameraMixin composes the inherited CAMERA
quaternion once with local view rotation, updates forward/up/left and view angles,
and independently transforms eye position. The bounded client-only injection
changes only getEntityOrientation's CAMERA result for the local mounted player.
Other entity/body/remote transforms keep the original quaternion. All original
five families and Wing resolve through the same Boarding carrier body binding;
there is no family-specific camera override. Their accepted normal view/body/sync
proof remains valid; this task checks the new selected mode.

The actual shared ComfortFrame function passed16 combinations of upright,
vertical and inverted pitch/bank: finite normalized world-up frame, preserved
seat quaternion and distinct left/right mouse-look compositions. First standalone
JUnit attempt loaded a Minecraft client class absent from this project's test
runtime; extracting only the presentation math removed that test harness dependency.
Corrected targeted test and packaged build passed once (math-build.txt).

## Connected-client acceptance

Same owned Java21/private :310 run as [Wing](../wing/README.md); artifact identity
is in [final-artifact.txt](../wing/final-artifact.txt). No production behavior changed
after cd949caa. Normal K toggling worked upright; the saved true preference survived
a full client/server restart and applied on the next normal seat mount.

[Wing samples](wing/) cover the linked two-car train through bends and its30° bank.
Native seat quaternion changes while cameraUp stays(0,1,0) at level mouse pitch.
A100-pixel horizontal gesture changes view/yaw while keeping world up. First-person
item/arm and third-person physical rider/seat rendering are retained in images.
Both cars stayed linked/eligible with no fault and returned to the powered station.
Normal Open restraints and Shift removed the seat assignment and comfort binding;
[unloaded.json](wing/unloaded.json) retains passenger=false/nativeSeatOrientation=null.

[Arrow samples](arrow/) exercise the original independent rotary carrier path on
`/fd demo arrow_bank` at origin(300,80,12), with normal boarding, Close restraints
and Dispatch. Native seat-up Y reaches−0.9048 while camera-up Y remains0.99336
(cosine of the user's−6.608° view pitch), with constant heading until mouse input.
The first inversion image has the pause menu over the rendered world: Dispatch
had already closed the editor, and a subsequent Escape opened that menu. These
pose samples prove frame isolation, not live mouse input. After closing the menu,
an80-pixel mouse gesture changed yaw89.998→102.148° and camera quaternion with
no seat mutation. Separate active first/third-person captures show arms and the
physical rider on the moving vehicle. Station capture and normal open/unload
completed; [unloaded.json](arrow/unloaded.json) shows no passenger/native seat frame.
No extra lap is required to repeat independently covered controls.

The initial Arrow aim missed the short clickable cushion. Native `sable info`,
plot block checks and a temporary world marker measured the actual carrier/seat;
Create's shape is half a block high. The calculated−6.61° aim yielded an actual
BLOCK/arrow_seat hit before the successful click. Marker removed. Setup commands
and example generation establish this camera fixture, not new construction proof.

## Coverage and carry-forward

The new code is local CAMERA-only, family-independent and does not alter server,
ENTITY, eye-origin, remote rider or carrier transforms. Arrow covers independent
rotary bodies (also used by Legacy, Free Spin, Revised and PAX); Wing covers the
new chassis/body alias. Existing [five-family banked riders](../../0.4/recovery/occupied-banked-render-motion/current-matrix-20261111.md)
and [two-client lifecycle](../../0.4/recovery/two-client-recovery-lifecycle/acceptance.md)
remain the normal-camera/body/network evidence for every original family. No
changed remote synchronization path requires another observer client. Unit math
adds vertical/full inversion and immutable-input coverage. This closes C1 only;
later family/effects cases remain FD904C2/FD912.

All owned units stopped successfully after normal disconnect and clean world save;
[cleanup](../wing/final-units.txt). The complete world, inputs, logs and screenshots
remain under the persistent run directory. No runtime is handed off live.
