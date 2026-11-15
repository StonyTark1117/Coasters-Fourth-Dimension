#!/usr/bin/env python3
"""Generate original design-frame preparation assets; never native acceptance."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SEATS = [-1.5, -.5, .5, 1.5]
ENVELOPE = [[-2.25, -4.5, -1.75], [2.25, 1.1, 1.75]]
COLORS = {"seat": "#8558ac", "metal": "#a8bdc9", "harness": "#242d3c",
          "latch": "#f4a340", "mount": "#627e8d"}


def box(name, material, lo, hi):
    return dict(name=name, material=material, minimum=lo, maximum=hi)


def geometry(opened):
    shapes = []
    for i, x in enumerate(SEATS):
        def part(label, material, lo, hi):
            shapes.append(box(f"seat_{i}_{label}", material,
                              [lo[0]+x, lo[1], lo[2]], [hi[0]+x, hi[1], hi[2]]))
        part("cushion", "seat", [-.4375, -3.4375, -.375], [.4375, -3.25, .375])
        part("back", "seat", [-.4375, -3.25, -.5], [.4375, -1.875, -.3125])
        part("bracket", "mount", [-.125, -3.75, -1.375], [.125, -3.5, .125])
        part("foot", "metal", [-.375, -4.125, .375], [.375, -4, .875])
        for side in (-1, 1):
            sx = side * .3125
            part(f"foot_support_{side}", "metal", [sx-.0625,-4.0625,.3125],
                 [sx+.0625,-3.375,.4375])
            if opened:
                part(f"raised_harness_{side}", "harness", [sx-.0625,-1.875,-.5],
                     [sx+.0625,-1,-.375])
            else:
                part(f"harness_{side}", "harness", [sx-.0625,-3.125,.125],
                     [sx+.0625,-1.75,.25])
                part(f"shoulder_{side}", "harness", [sx-.0625,-1.875,-.375],
                     [sx+.0625,-1.75,.25])
        if opened:
            part("harness_bridge", "harness", [-.375,-1.125,-.5],[.375,-1,-.375])
        else:
            part("latch", "latch", [-.375,-3.125,.125],[.375,-3,.3125])
    for side in (-1,1):
        lo, hi = sorted([side*2, side*2.25])
        shapes.append(box(f"outer_rear_post_{side}", "metal", [lo,-3.75,-1.5], [hi,.75,-1.25]))
    shapes += [box("rear_row_mount", "mount", [-2.25,-3.75,-1.5],[2.25,-3.5,-1.25]),
               box("upper_bridge", "metal", [-2.25,.75,-1.5],[2.25,1,-1.25])]
    return shapes


def obj(shapes, name):
    lines = ["# Original Fixed Inverted preparation; graph-origin design coordinates.",
             "# Native N bogie and overhead provider mesh are integration inputs."]
    n = 1
    for s in shapes:
        lo, hi = s["minimum"], s["maximum"]
        lines += ["o " + s["name"], "g " + s["material"]]
        for x,y,z in [(0,0,0),(1,0,0),(1,1,0),(0,1,0),(0,0,1),(1,0,1),(1,1,1),(0,1,1)]:
            p=[(lo,hi)[v][i] for i,v in enumerate((x,y,z))]
            lines.append("v " + " ".join(str(v) for v in p))
        for face in [(0,3,2,1),(4,5,6,7),(0,1,5,4),(3,7,6,2),(0,4,7,3),(1,2,6,5)]:
            lines.append("f " + " ".join(str(n+i) for i in face))
        n += 8
    (ROOT/name).write_text("\n".join(lines)+"\n")


def svg_geometry(shapes):
    out=['<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="640" viewBox="0 0 1080 640">',
         '<rect width="1080" height="640" fill="#111b29"/>',
         '<g fill="#eff5ff" font-family="sans-serif"><text x="35" y="38" font-size="24">Fixed Inverted · four abreast</text>',
         '<text x="35" y="65" font-size="15">Original preparation mesh · graph origin O · no native acceptance claim</text></g>']
    for axis,cx,label in [(0,285,"Front / X–Y"),(2,825,"Side / Z–Y; forward →")]:
        scale=72
        def rect(lo,hi,color,stroke="none",dash=""):
            out.append(f'<rect x="{cx+lo[axis]*scale}" y="{190-hi[1]*scale}" width="{(hi[axis]-lo[axis])*scale}" height="{(hi[1]-lo[1])*scale}" fill="{color}" stroke="{stroke}" stroke-dasharray="{dash}"/>')
        rect(*ENVELOPE,"none","#e6b95a","7 5")
        for s in shapes: rect(s["minimum"],s["maximum"],COLORS[s["material"]],"#111b29")
        rect([-2,-4.25,-1],[2,-1.5,1],"none","#50dbc8","4 4")
        out += [f'<text x="{cx-150}" y="105" fill="#eff5ff" font-family="sans-serif" font-size="18">{label}</text>',
                f'<path d="M{cx-180} 190h360" stroke="#8e9aac" stroke-dasharray="2 6"/>',
                f'<text x="{cx+165}" y="184" fill="#eff5ff" font-family="sans-serif">O</text>']
    out += ['<g font-family="sans-serif" font-size="15"><text x="35" y="555" fill="#e6b95a">Gold: reserved envelope 4.50 wide × 5.60 high × 3.50 long</text>',
            '<text x="35" y="581" fill="#50dbc8">Teal: occupied closed bound · rear posts have 0.25 static Z clearance</text>',
            '<text x="35" y="607" fill="#eff5ff">Seat centers Y=-3.25 · native bogie / provider mesh supplied by integrated Suspended A</text></g></svg>']
    (ROOT/"geometry.svg").write_text("\n".join(out))


def main():
    closed, opened = geometry(False), geometry(True)
    # Position/tangent/bank are intended native anchor inputs, not a generated graph.
    points = [(0,12,0,0,0,1,0),(0,12,48,0,0,1,0),(0,36,112,0,0,1,0),
              (0,36,144,0,0,1,90),(0,36,176,0,0,1,180),(0,36,208,0,0,1,270),
              (0,36,240,0,0,1,360),(0,12,304,0,0,1,0),(48,12,352,1,0,0,0),
              (96,12,352,1,0,0,0),(144,12,304,0,0,-1,0),(144,12,0,0,0,-1,0),
              (96,12,-48,-1,0,0,0),(48,12,-48,-1,0,0,0)]
    waypoints=[dict(position=list(p[:3]), tangent=list(p[3:6]),bank_degrees=p[6]) for p in points]
    design=dict(schema="fixed-inverted-preparation-v1", status="NOT_NATIVE_ACCEPTANCE",
                baseline="d73d43b820b725fc9648c6a2daa9348dead16b22",
                family="fixed_inverted", variant="four_abreast_v1", frame="O graph; X=-right; Y=up; Z=forward",
                seat_centers=[[x,-3.25,0] for x in SEATS],
                bearing_relative_native_seat_centers=[[-x,-3.5625,0] for x in SEATS],
                occupied_closed_bound=[[-2,-4.25,-1],[2,-1.5,1]], envelope=ENVELOPE,
                native_contact_centers=[[-.772,.325],[.772,.325]],
                limits=dict(cars_min=1,cars_max=4,spacing=8,minimum_radius=16,maximum_speed=16,
                            maximum_roll_degrees_per_block=5,minimum_four_car_dock_length=36),
                route_waypoints=waypoints, closed_mesh=closed, open_mesh=opened)
    bounds={}
    for label,shapes in [("closed",closed),("open",opened)]:
        lo=[min(s["minimum"][i] for s in shapes) for i in range(3)]
        hi=[max(s["maximum"][i] for s in shapes) for i in range(3)]
        assert all(ENVELOPE[0][i] <= lo[i] <= hi[i] <= ENVELOPE[1][i] for i in range(3))
        bounds[label]=[lo,hi]
        obj(shapes,label+".obj")
    report=dict(status="PREPARATION_ONLY", mesh_bounds=bounds, mesh_within_envelope=True,
                rear_post_occupied_static_gap=.25, specified_underbrace_head_static_gap=.80,
                four_car_longitudinal_extent=3*8+2*1.75, dock_36_unused_length=36-(3*8+2*1.75),
                linear_roll_degrees_per_block=90/32, smoothstep_peak_roll_degrees_per_block=1.5*90/32,
                native_claims_proven=[], pending=["native seat plot/mount origin", "integrated N bogie/provider mesh",
                    "actual native interpolated radius/roll", "four-car continuous clearance",
                    "native mass and loaded COM", "occupied inversion", "dock and nonupright persistence"])
    for filename,data in [("design.json",design),("report.json",report)]:
        (ROOT/filename).write_text(json.dumps(data,indent=2)+"\n")
    svg_geometry(closed)
    coords=[(110+p[0]*1.5,630-(p[2]+48)*1.4) for p in points]
    path="M"+" L".join(f"{x},{y}" for x,y in coords)+" Z"
    out=['<svg xmlns="http://www.w3.org/2000/svg" width="860" height="700">',
         '<rect width="860" height="700" fill="#111b29"/>',
         '<g fill="#eff5ff" font-family="sans-serif"><text x="35" y="30" font-size="23">Fixed Inverted · lifted stadium proposal</text>',
         '<text x="410" y="100" font-size="17">Top view · anchor polyline only</text>',
         '<text x="410" y="135">48-block level dock</text><text x="410" y="165">64-block lift · rise 24</text>',
         '<text x="410" y="195">128-block roll · 0 → 360°</text><text x="410" y="225">64-block descent</text>',
         '<text x="410" y="255">48-radius return curves</text><text x="410" y="300">Native curve validation pending</text></g>',
         f'<path d="{path}" stroke="#a77bd2" stroke-width="4" fill="none"/>']
    for i,((x,y),p) in enumerate(zip(coords,points)):
        label = str(i) if i in (8,9,10,11,12,13) else f"{i}: y={p[1]} / {p[6]}°"
        out += [f'<circle cx="{x}" cy="{y}" r="5" fill="#f4a340"/>',
                f'<text x="{x+9}" y="{y-6}" fill="#eff5ff" font-family="sans-serif" font-size="13">{label}</text>']
    (ROOT/"route.svg").write_text("\n".join(out)+"\n</svg>\n")
    print(json.dumps(report,indent=2))


if __name__ == "__main__":
    main()
