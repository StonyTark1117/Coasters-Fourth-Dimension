# Wing construction and restore — current results

Same owned run as ../occupied, after clean server/client restart on cd949ca.
Server restored chassis d6a512ad-de6e-42a3-a119-1bec039c0c04 and player seat offset
(4,1,0); FD_BOARDING_RESTORE mounted at gameTick7975. Local comfort K toggled
successfully with cameraUp(0,1,0), exact rider still mounted, then normal Shift
unloaded and removed its camera binding.

Supplied upstream ingredients (2 Create Red Seats,1 Iron Bars,4 Iron Ingots,
1 native Coaster Track,1 native Coaster Cart) and a Crafting Table are disclosed
fixture setup, together with recipe unlock and position/aim teleports. In Survival,
normal crafting-table and recipe-book clicks consumed all9 ingredients; server
Inventory contained only one fourthdimension:wing_car in slot8. Normal item use
placed that crafted car on ordinary track at center(.5,80.625,45.29), six blocks
behind the restored car; Inventory then empty. One supplied Lime Dye used on
its empty, stopped open seat recolored all four seat blocks and was consumed.
Source identity and geometry are unchanged by dye. Screenshots accompany record.

Normal Chain linking passed at20:44:41 with both native client/server events:
`linked carts f34eafef-78d8-4294-b737-fc43158de676 <->
d6a512ad-de6e-42a3-a119-1bec039c0c04 at distance 6.000`.
The Chain remains, matching native tool behavior. Client consist eligibility true,
both bodies/records loaded. Normal outer-seat boarding and tool Close restraints passed. Releasing the
powered station started both linked cars; the already-open editor rejected a
stale Dispatch revision, so that click is not credited as Dispatch evidence
(the earlier single-car normal Dispatch remains accepted). the comfort bank samples show continued native
links and no fault through bends and the sustained30° bank.

The earlier below-track attempt was an obstructed interaction ray: F3's separate
ray saw the bearing, but native AnchorPeerCurvePick replaced the interaction hit
with MISS for the closer track curve. No block event arrived. At side footing
(2.5,79,z), yaw90 pitch0, the actual hit was BLOCK/native bearing; one click per
car succeeded. This is a setup correction, not a production or mouse-input defect.
Temporary footing/position commands are disclosed and are not survival building
claims. The player guide now explains clear side access to the central bearing.
