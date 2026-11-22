# Standing and Surf cars

These are two original two-place standing cars. Standard has a rigid foot platform; Surf translates its shared passenger carriage up to 0.40 blocks along its guides. The older seated Pipeline car is a separate design.

Craft Standing Track from native Coaster Track plus green dye; Surf Track uses lime dye. Craft either car with `BIB / ITI / ICI`: B is iron bars, I iron ingots, T its matching track item, C a native Coaster Cart. Standard and Surf tracks are not interchangeable.

Use fixed-world matching rails, curves of radius at least 16, pitch and bank no greater than 45 degrees, and native Chain propulsion. Keep the full occupied envelope clear: 1.75 blocks sideways and longitudinally, 4 above the rail origin, 0.95 below. Leave at least eight blocks between cars; these configurations do not support couplers or multiple-car consists. Speed is limited to 12 blocks per second.

Provide a level matching dock at least 24 blocks long and dry, clear platforms on both sides. Example layouts place platform blocks one block above the anchor-block origin, outside the car's swept width. Bind the boarding station to the stopped car, capture it at home and open restraints before normal boarding. Close restraints before Dispatch. Surf passengers stand on the actual moving child; the camera follows their native standing eye position.

Use Stop as the car returns along its bound level dock. Stop brakes travel; at that valid dock Surf then lowers its carriage to the home position. Resume to dock authorizes continued travel after repair; it does not teleport or automatically stop the car at the station. Opening is denied while Surf is away from home. A lost drive, changed track, missing support or interrupted guide retains the car and closes restraints. Repair the cause, validate and explicitly Resume; power restoration alone does not release it. After a cold restart the car remains retained until explicit validated continuation. Use the ordinary station and collision-tested dismount when home capture is ready.

For a prepared teaching course, use `/fd demo standing_standard` or `/fd demo standing_surf`; `_track` variants provide the course without a car. These are setup commands, not evidence of player construction. The configuration contracts and bounds are in [FD2201-standing](specs/FD2201-standing.md).
