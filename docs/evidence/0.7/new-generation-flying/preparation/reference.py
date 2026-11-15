"""Specification-only reference calculations. No game state or production code is exercised."""
import json, math
from pathlib import Path
P=Path(__file__).parent
TAU=math.tau
EPS=1e-9

def rx(p,a):
 x,y,z=p;c=math.cos(a);s=math.sin(a);return (x,c*y-s*z,s*y+c*z)
def ry(p,a):
 x,y,z=p;c=math.cos(a);s=math.sin(a);return (c*x+s*z,y,-s*x+c*z)
def rz(p,a):
 x,y,z=p;c=math.cos(a);s=math.sin(a);return (c*x-s*y,s*x+c*y,z)
def distance(a,b):return math.sqrt(sum((x-y)**2 for x,y in zip(a,b)))
def heading(phi,psi):return ry(rx((0,math.sin(phi),math.cos(phi)),phi),psi)
def smooth(a,b,s,length):
 u=min(1,max(0,s/length));return a+(b-a)*u*u*(3-2*u)
def candidate(theta,current,policy='NEAREST'):
 choices=[theta+k*TAU for k in range(-6,7) if -8*math.pi-EPS<=theta+k*TAU<=8*math.pi+EPS]
 if policy=='POSITIVE':choices=[a for a in choices if a>=current-EPS]
 if policy=='NEGATIVE':choices=[a for a in choices if a<=current+EPS]
 return min(choices,key=lambda a:(round(abs(a-current),12),-a)) if choices else None

corners=[(x,y,z) for x in [-1,1] for y in [-1.25,1.5] for z in [-.6,.7]]
radius=max(math.sqrt(sum(v*v for v in p)) for p in corners)
max_radius_error=0.;max_heading_error=0.;max_projection_error=0.;count=0
for bank_deg in [0,30,90,180,270]:
 bank=math.radians(bank_deg)
 for phi_deg in range(0,91,5):
  phi=math.radians(phi_deg)
  for psi_deg in range(-1440,1441,15):
   psi=math.radians(psi_deg);count+=1
   h=heading(phi,psi)
   max_heading_error=max(max_heading_error,distance(h,(math.sin(psi),0,math.cos(psi))))
   for p in corners:
    world=rz(ry(rx(p,phi),psi),bank)
    max_radius_error=max(max_radius_error,abs(math.sqrt(sum(v*v for v in world))-math.sqrt(sum(v*v for v in p))))
   # A marker expressed in the chassis design frame; arbitrary plane-normal height is discarded.
   world_marker_delta=rz((9*math.sin(psi),4,9*math.cos(psi)),bank)
   local=rz(world_marker_delta,-bank);theta=math.atan2(local[0],local[2])
   projected=(math.sin(theta),0,math.cos(theta))
   max_projection_error=max(max_projection_error,distance(h,projected))
assert max_radius_error<1e-12 and max_heading_error<1e-12 and max_projection_error<1e-12
assert candidate(math.pi,0)==math.pi
assert candidate(-math.pi,0)==math.pi
assert candidate(math.pi/2,8*math.pi,'POSITIVE') is None
assert candidate(-math.pi/2,-8*math.pi,'NEGATIVE') is None
assert candidate(0,7.75*math.pi,'POSITIVE')==8*math.pi
# A reference route program at2 blocks/s: simultaneous nonzero-yaw posture transition.
length=24.;speed=2.;phi_delta=math.pi/2;psi_delta=math.pi/2
rates={'phi_max_rate':1.5*phi_delta*speed/length,'phi_max_acceleration':6*phi_delta*(speed/length)**2,'yaw_max_rate':1.5*psi_delta*speed/length,'yaw_max_acceleration':6*psi_delta*(speed/length)**2}
assert rates['phi_max_rate']<=.25 and rates['phi_max_acceleration']<=.35
assert rates['yaw_max_rate']<=.6 and rates['yaw_max_acceleration']<=.8
profile=[{'distance':s,'phi':smooth(0,math.pi/2,s,length),'psi':smooth(math.pi/2,math.pi,s,length)} for s in range(25)]
assert list(reversed(profile))==[{'distance':s,'phi':smooth(0,math.pi/2,s,length),'psi':smooth(math.pi/2,math.pi,s,length)} for s in reversed(range(25))]
stop=[smooth(0,4*math.pi,64,128) for _ in range(200)]
assert len(set(stop))==1 and stop[0]==2*math.pi
# Continuous-time bound: rotations preserve norm, so the complete closed rider box
# under any simultaneous posture/yaw trajectory is enclosed by this exact sphere.
report={'scope':'specification-only math; not native dynamics, assets, synchronization or acceptance','samples':count,'occupied_sphere_radius':radius,'sphere_graph_top':-3+radius,'sphere_graph_bottom':-3-radius,'gap_to_original_native_underbrace_minus_0_70':-.7-(-3+radius),'reserved_half_width':2.75,'reserved_above_graph':1.10,'reserved_below_graph':5.25,'reserved_half_length':2.75,'sample_radius_error':max_radius_error,'continuous_bound':'Orthogonal Rx/Ry/B preserve Euclidean norm; max corner norm bounds every point of the convex occupied box for every combined trajectory. Does not bound yaw-deck forks/spindle/bogie: those need their own authored geometry and native sweep.','presentation_heading_error':max_heading_error,'banked_projected_target_heading_error':max_projection_error,'candidate_cases':{'pi_tie_positive':candidate(-math.pi,0),'positive_limit_unreachable':candidate(math.pi/2,8*math.pi,'POSITIVE'),'negative_limit_unreachable':candidate(-math.pi/2,-8*math.pi,'NEGATIVE'),'positive_last_half_turn':candidate(0,7.75*math.pi,'POSITIVE')},'transition_candidate':{'length':length,'speed':speed,'seconds':length/speed,**rates},'stopped_distance_profile_constant':stop[0],'limits':'Reference rate bounds assume constant travel speed. Native rate/acceleration, loading/solver response, endpoint lock dwell, blends, drive holds, marker validity and actual clearances remain required.'}
(P/'report.json').write_text(json.dumps(report,indent=2)+'\n')
(P/'combined-profile.json').write_text(json.dumps(profile,indent=2)+'\n')
# Compact code-native geometry reference; not a screenshot or game render.
scale=64;cx=300;cy=150
svg=['<svg xmlns="http://www.w3.org/2000/svg" width="900" height="650" viewBox="0 0 900 650">','<rect width="900" height="650" fill="#f5f7fa"/>','<g font-family="sans-serif" fill="#172a3b"><text x="30" y="35" font-size="24">New-generation Flying — combined occupied reservation</text><text x="30" y="61" font-size="14">Specification reference only • graph-origin design frame • native frame/solver still unproven</text>']
svg += [f'<rect x="{cx-2.75*scale}" y="{cy-1.1*scale}" width="{5.5*scale}" height="{6.35*scale}" fill="#ddeaf5" stroke="#6483a1"/>',f'<line x1="{cx-2.75*scale}" x2="{cx+2.75*scale}" y1="{cy}" y2="{cy}" stroke="#384b61"/>',f'<circle cx="{cx}" cy="{cy+3*scale}" r="{radius*scale}" fill="#bc9fe3" fill-opacity=".4" stroke="#7145a0"/>',f'<circle cx="{cx}" cy="{cy+3*scale}" r="4" fill="#7145a0"/>']
for text,x,y in [('graph O',cx+180,cy+5),('pivot P=(0,-3,0)',cx+140,cy+3*scale),('R=1.93391 occupied sphere',585,330),('Whole reservation: X/Z ±2.75, Y +1.10 / -5.25',95,520),('Outer chassis-Y yaw → inner yaw-deck-X posture',95,550),('Head-first presentation remains yaw-deck +Z through changeover',95,580),('Spindle/forks and running gear require separate measured geometry.',95,610)]:svg.append(f'<text x="{x}" y="{y}" font-size="15">{text}</text>')
svg += ['<text x="585" y="135" font-size="17">Candidate overlap section</text>','<text x="585" y="170" font-size="15">24 blocks at ≤2 blocks/s</text>','<text x="585" y="200" font-size="15">Posture: 0° → 90°</text>','<text x="585" y="230" font-size="15">Yaw: 90° → 180°</text>','<text x="585" y="260" font-size="15">Rate-limited physical servo</text>','<text x="585" y="290" font-size="15">Exit waits for actual endpoint lock</text>','</g></svg>']
(P/'combined-envelope.svg').write_text('\n'.join(svg)+'\n')
print(json.dumps(report,indent=2))
