# Additions station operations

Development profile `-PfdIntegrations=additions` selects file9243346 (published
0.2.5 beta, runtime `coasterfins`0.2.2), Aeronautics1.3.2 and Interiors0.6.1v3.
Four reviewed runtime class fingerprints must match. The base mod does not require
these addons. This adapter concerns station signals and validated requests;
rotating Additions seats remain FD911.

Run `/fd demo additions_station` in an empty area to prepare a native Wing loop,
car, platforms and a configured station span. This is a setup example, not a
survival-construction demonstration. To build your own, use the Additions station
item's four track selections (platform start/end, pre-station start/end), then
place the station beside the platform. Every car and its wheel contacts must fit
the platform span before that station can request dispatch.

Empty-hand right-click opens the native station menu. Set Redstone Link frequency
items before expecting output: an empty frequency transmits zero. Occupied is15
when the train occupies the platform; weight is the shared unique rider count,
saturated at15. Restraint bars follows the addon's native polarity:15 means bars
up/open,0 means closed. The native pre-station signal remains advisory; it never
grants dispatch authority. Goggles and Create display links expose the shared
whole-train telemetry and dispatch denial.

Enable Auto Dispatch explicitly, choose rider threshold, delay and arrival
cooldown, then board and close restraints with the Rotation Tool. Once the entire
train is captured and ready, the adapter waits cooldown plus delay and submits
the existing validated Dispatch request. Opening restraints resets that wait;
the adapter never closes or opens them. A moving arriving train is captured only
through the same validated operation interface. Use Stop on return and open
restraints after normal boarding readiness.

Another controller's ownership, missing native bodies, incomplete spans, unsafe
restraints, recovery, faults or unavailable support cannot grant dispatch. Span
changes and reload disable automatic dispatch until the operator explicitly
re-enables it. Changing a frequency does not arm a train. Native ordinary-cart
behavior remains upstream when the station has not encountered a Fourth Dimension
train. A managed station retains the adapter's guarded behavior thereafter.
