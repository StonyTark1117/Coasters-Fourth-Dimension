# FD402C occupied Arrow operator workflow

**Result:** the finite FD402C Arrow interaction gap is closed when this record is
combined with the already accepted normal bearing-selection, physical-lever,
linked-train and PAX-handover records. A real survival player remained mounted
while the normal editor attached, started and braked recovery before the route
boundary. A fresh selected-JAR process restored the same occupied, braked link
without automatic towing, then the real player used **Detach recovery**, **Open
restraints** and held Shift to unload. Both client processes exited normally.

This is cumulative operator acceptance, not a claim that every input happened in
one pristine take. The current roadmap explicitly reuses established player
actions with the distinct family/native mechanics rather than replaying a family
matrix.

## Artifact and isolation

The motion/brake process used development JAR
`811090454924d86c234b4d72efa19c568bbea108b55b84fb3b82e697586288aa`.
All seven production entries that implement the editor, recovery, vehicle state
and synchronization are byte-identical to selected qualification JAR
`656725bcbacc8f491de274c6c058027d0aaa67cfc6f893ba6d7e4a9eb24abbc7`;
[production-class-equivalence.txt](production-class-equivalence.txt) retains the
per-class hashes. The cleanup continuation loaded that exact selected JAR.

Every Fourth Dimension validation and fixture flag was false. The continuation
used Java 21, a 1536 MiB game heap and a Minecraft-only systemd scope with
`MemoryMax=6G`. The highest sampled `MemoryCurrent` was 2,644,135,936 bytes.
It ran on task-owned Xvfb display `:237`; the only other live Xvfb was another
agent's `:252`, which was neither addressed nor stopped. The selected client and
owned display both ended with systemd result `success`; the client exit status
was zero.

The exact selected launch material is retained as [classpath](client-classpath.txt),
[VM arguments](client-vm-args.txt) and [program arguments](client-program-args.txt).
The classpath points at the selected JAR copy and the VM arguments show all
fixture/validation switches disabled.
The [first-process log](first-process-client.log.gz) and
[selected continuation log](selected-continuation-client.log.gz) are retained in
full. Run `python3 verify.py` in this directory to verify every checksum, both
world archives, the focused before/after state and shutdown markers.

## Player workflow

The cumulative sequence is:

1. The existing normal-input records establish empty-hand boarding, normal-range
   sneak-selection of the certified bare ordinary bearing, **Attach recovery**,
   an eight-RPM Create motor, and a physical vanilla lever supplying/cutting
   clutch power.
2. In this occupied continuation, the real mounted player retained the same
   Arrow chassis and certified bearing, invoked **Start tow**, observed native
   motion, and invoked **Brake tow** before the boundary. Screenshots
   [01](01-occupied.png)–[08](08-brake-applied.png) retain the occupied,
   attached, motor, start, propulsion, movement and brake states.
3. The client saved the occupied braked link and exited. The input archive
   [input-world-before-cleanup.tar.gz](input-world-before-cleanup.tar.gz) preserves
   that state.
4. A new process on the selected JAR restored player `Dev` into the same carrier
   on chassis `3fe570d5-8669-4931-b104-2bc59599e2dd`, still linked to bearing
   `8caae637-b310-4b4b-b857-ec8bf24389d3`. No tow resumed automatically; see
   [09-selected-jar-reopen-occupied.png](09-selected-jar-reopen-occupied.png) and
   [10-selected-jar-braked-editor.png](10-selected-jar-braked-editor.png).
5. The player invoked **Detach recovery**, reopened the ordinary editor, invoked
   **Open restraints**, and held Shift. Screenshots [11](11-detached.png)–
   [15](15-rootvehicle-cleared.png) show the resulting ordinary-ready state and
   the read-only `RootVehicle` query returning no element.
6. **Save and Quit to Title** and **Quit Game** ended the selected process
   normally; [16-title-after-save.png](16-title-after-save.png) and the
   [continuation log](selected-continuation-client.log.gz) retain the shutdown.

The earlier occupied process temporarily raised block interaction range while
diagnosing an obstructed mounted targeting angle and used a disclosed daylight
detector to make the installed clutch accessible. Those actions are not counted
as normal bearing-selection or physical-lever acceptance. The separate
[manual bearing](../manual-bearing-selection/acceptance.md),
[manual lever](../manual-lever-propulsion/acceptance.md) and
[pre-boundary brake](../power-control/acceptance.md) records establish those
normal inputs. The selected continuation restored block interaction range to
`5.0` before cleanup; the initial invalid short attribute identifier and the
successful namespaced command both remain visible in the raw log.

## Saved-state audit

[foundation-input.snbt](foundation-input.snbt) and
[player-input.snbt](player-input.snbt) decode the archived input. They retain the
same chassis/bearing IDs, closed restraints, recovery link, occupied carrier and
player `RootVehicle`. The final [foundation](foundation-final.snbt) and
[player](player-final.snbt) decodes retain the same chassis with:

- no `recovery_cart` field;
- open restraints;
- empty carrier occupant and rider-seat lists; and
- no player `RootVehicle`.

[final-state-summary.txt](final-state-summary.txt) records the focused before/after
fields. The final full world is retained as
[final-world-after-cleanup.tar.gz](final-world-after-cleanup.tar.gz), and the
binary Foundation/player saves accompany their decodes. The last equipment
warning text remains stored after detachment because the Rotation Tool was
reselected for the two editor actions; it is disclosed rather than hidden. The
ordinary editor reported `Ready`, the recovery link is absent, and it did not
block restraint opening or unload.

## Cumulative FD402C conclusion

This record supplies the previously missing interaction between an occupied
Arrow recovery, production **Start/Brake**, a fresh-process braked restore, and
normal release. The existing [linked-train mechanics](../banked-trains/acceptance.md)
establish the same native linked-train operations. The distinct stationary PAX
path remains established by the occupied sixteen-rider
[handover record](../occupied-handover/acceptance.md); the later PAX station-
restore change does not modify its recovery selection, handover, tow, brake,
detach or unload paths, as recorded in the
[FD402A current-input review](../current-input-review-95301062/README.md).

No other family has a different ordinary-bearing operator path. Under the
current cumulative validation policy, FD402C is complete without another family
or all-in-one replay. FD402B rendering, FD402D persistence coverage, FD402E
multiplayer interruptions and FD402F optional power remain separate.
