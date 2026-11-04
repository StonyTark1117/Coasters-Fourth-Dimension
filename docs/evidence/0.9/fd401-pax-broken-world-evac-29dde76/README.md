# FD401 saved broken PAX world: normal rider exit

On 2026-11-04, the saved [broken-consist world](../fd401-candidate-broken-consist-2e12e9d/README.md)
(archive SHA-256 `93250fd6d01e24ed58e2bbdc776d6411b8de035b63d1d4e7454ab0caeb38eb67`)
was copied into an isolated dedicated server and opened by the Dev client.
The combined Styles + Engineered packaged JAR was built from committed
`29dde76` with `fdFeNetwork`; its SHA-256 was
`ca4e3b3285ae29a47989cbdebc5050adb375d150cd66303c0b474887f230080d`.
The copied run never altered the archived world.

Dev restored mounted on the displaced trailing PAX carrier. The client used
the normal in-ride editor to **Open restraints**. The [initial editor](client-editor-open.png)
showed closed restraints; the [next editor view](client-restraints-status.png)
showed them open. Dev then pressed Shift to dismount. The [client F3 view](client-after-dismount-f3.png)
shows Dev standing at approximately `(900.2, -64, 499.0)` on the superflat
floor. The [server log](server.log.gz) records the restored mount and
`FD_BOARDING_EVENT ... action=dismount ... restraints=false`, followed by
`FD_BOARDING_DISMOUNT_CLEAR`. The [client log](client.log.gz) and
[SHA-256 inventory](SHA256SUMS) retain the run. The client exited through its
menus, and the server stopped normally; both processes exited 0.

The archived winch still names four cars, but the saved native bodies are at
approximately `(900.5,87.7,73.5)`, `(900.5,87.7,95.2)`,
`(899.6,-59.6,488.7)` and `(896.5,-59.5,500.8)`. The last two are roughly
400 blocks from the station and 147 blocks below the first two. This
observation explains why the stationary source fixture's native recoupling
does not demonstrate recovery of this world. Normal rider evacuation is
proven here; retrieving or rebuilding the displaced consist, rebind and a
fresh occupied Dispatch are still open. Because a local floor was available,
the `29dde76` safe-spawn fallback for a floorless dismount was not exercised.
