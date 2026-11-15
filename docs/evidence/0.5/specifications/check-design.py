#!/usr/bin/env python3
"""Finite analytic design checks, not Minecraft/native vehicle acceptance.
Run from any working directory; emits JSON to stdout and does not mutate inputs.
"""
from pathlib import Path
from math import sin, cos, sqrt, pi, atan2, hypot
from itertools import product
import json, re

ROOT = Path(__file__).resolve().parents[4]
DESIGN = ROOT / 'docs/design/0.5'
files = ['FD1401-pipeline.md','FD1502-roller-ball.md','FD1501-spinning.md',
         'FD1302-axis.md','FD1301-suspended.md','FD1601S-fixed-inverted.md',
         'FD1702S-dive.md','FD1801S-conventional-flying.md',
         'FD1802S-new-generation-flying.md','FD1901S-powered-inverted-yaw.md',
         'specification-conventions.md','shared-yaw-contract.md']

def rx(p,a):
    x,y,z=p; return (x,y*cos(a)-z*sin(a),y*sin(a)+z*cos(a))
def ry(p,a):
    x,y,z=p; return (x*cos(a)+z*sin(a),y,-x*sin(a)+z*cos(a))
def rz(p,a):
    x,y,z=p; return (x*cos(a)-y*sin(a),x*sin(a)+y*cos(a),z)
def norm(p): return sqrt(sum(x*x for x in p))
def dist(a,b): return norm(tuple(x-y for x,y in zip(a,b)))
def require(c,msg):
    if not c: raise AssertionError(msg)

# Native quaternion columns are (right,up,-forward): using Ry(pi) gives
# (-right,up,forward), a proper rotation instead of a handedness reflection.
require(dist(ry((0,0,1),pi),(0,0,-1))<1e-12,'native forward conversion')
require(dist(ry((1,0,0),pi),(-1,0,0))<1e-12,'native side conversion')

margins = {
 'pipeline_inner_rail_to_rider': 1.5-.13125-1.0,
 'spinning_lower_body_to_native_top_wheel':1.10-(.325+.075+2*.2),
 'suspended_cabin_to_native_underbrace':-.70-(-1.50+(1-.25)/sqrt(2)),
 'axis_reserved_sweep_to_native_running_outboard':1.50-1.12,
 'fixed_inverted_head_to_underbrace':-.70-(-1.50),
 'conventional_flying_to_original_underbrace_REJECTED':-.70-(-2.5+hypot(1.5,.6)),
 'conventional_flying_to_relocated_brace':-.55-(-2.5+hypot(1.5,.6)),
 'new_flying_sphere_to_native_underbrace':-.70-(-3+sqrt(1+2.25+.49)),
 'powered_inverted_head_to_underbrace':-.70-(-1.50),
}
for name,value in margins.items():
    require(value < .25 if 'REJECTED' in name else value >= .25,name)

# Conservative analytic sphere for all compound yaw/posture orientations.
r=sqrt(1+2.25+.49)
require(r<2.25 and -3-r>-5.25,'compound passenger reservation')
require(hypot(1,2.25)+.125<2.75,'yaw-deck fork circular reservation')
# Validate the declared box sweep and physical heading across posture/yaw.
max_heading_error=0.; samples=0
for i in range(19):
    phi=(pi/2)*i/18
    h=(0,sin(phi),cos(phi))
    for j in range(33):
        psi=-4*pi+8*pi*j/32
        actual=ry(rx(h,phi),psi)
        expected=(sin(psi),0,cos(psi))
        max_heading_error=max(max_heading_error,dist(actual,expected))
        for p in product((-1,1),(-1.25,1.5),(-.6,.7)):
            q=ry(rx(p,phi),psi)
            require(norm(q)<=r+1e-12,'compound sphere')
            require(abs(q[0])<2.75 and abs(q[2])<2.75,'compound frame reservation')
            require(-3+q[1]>-5.25 and -3+q[1]<-.95,'below-track rider reservation')
            samples+=1
require(max_heading_error<1e-12,'prone heading degeneracy')
wrong=rx(ry((0,1,0),pi/2),pi/2)
right=ry(rx((0,1,0),pi/2),pi/2)
require(dist(wrong,right)>1,'yaw/posture order must be observable')
# Projection behaves in an arbitrary rolled frame; actual body heading transforms
# with B, while out-of-plane target content must not create an extra pitch DOF.
for bank in (0,pi/3,pi):
    target_local=(3,7,-4)
    theta=atan2(target_local[0],target_local[2])
    physical=rz((sin(theta),0,cos(theta)),bank)
    projection=rz((.6,0,-.8),bank)
    require(dist(physical,projection)<1e-12,'bank/inversion projected heading')
require(hypot(0,.0)<.05,'declared degenerate target threshold')

# Bounds cover all rotating pod geometry before external scenery margin.
require(-1.5-hypot(1,2.75)>-4.7,'suspended bottom reservation')
require((1+2.75)/sqrt(2)<3,'suspended lateral reservation')
require(hypot(2.25,1)<2.5,'roller ball seated pod corner')
require(2+2.5<4.75 and 2-2.5>-.75,'roller ball Y sweep')
require(hypot(1,.7)<1.5,'powered yaw radius')

# Local links include companion docs and evidence, not another owner's draft.
links=0
for name in files:
    p=DESIGN/name
    require(p.exists(),f'missing package {name}')
    for dest in re.findall(r'\]\(([^)]+)\)',p.read_text()):
        if '://' in dest or dest.startswith('#'):continue
        target=dest.split('#')[0]
        require((p.parent/target).exists(),f'broken link {p.name}: {target}')
        links+=1
print(json.dumps({
 'status':'PASS',
 'scope':'Analytic dimensions, coordinate/heading composition and local links only; no native/client run',
 'owned_design_files':len(files),'local_links_checked':links,
 'compound_corner_samples':samples,'maximum_heading_error':max_heading_error,
 'compound_radius':r,'clearance_margins_blocks':margins,
 'later_required':'Actual original meshes, native solver stability, coupled curves, occupied transforms and lifecycle remain their existing A/B/C claims'
},indent=2))
