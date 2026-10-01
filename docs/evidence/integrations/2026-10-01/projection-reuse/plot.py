from pathlib import Path
import json,math,statistics
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
b=Path(__file__).resolve().parent
assert (b/'benchmark.exit').read_text().strip()=='0'
plt.rcParams.update({'font.size':10,'axes.spines.top':False,'axes.spines.right':False})
fig,axes=plt.subplots(2,4,figsize=(13,6.6))
colors=['#176c8e','#c16620']
for col,(pack,mode) in enumerate([('base','parked'),('base','operating'),('combined','parked'),('combined','operating')]):
 for repeat in range(2):
  points=[]
  for version in ['old','new']:
   d=json.loads((b/f'{pack}-{repeat}-{version}-{mode}.json').read_text());t=sorted(d['tickNanos'])
   points.append((t[math.ceil(.95*len(t))-1]/1e6,statistics.mean(d['carrierNanos'])/1e6))
  for row in range(2):
   axes[row,col].plot([0,1],[p[row] for p in points],marker='o',color=colors[repeat],label=f'Round {repeat+1}')
 for row in range(2):
  ax=axes[row,col];ax.set_xticks([0,1],['Old','New']);ax.set_xlim(-.2,1.2);ax.set_ylim(bottom=0);ax.grid(axis='y',alpha=.2)
 axes[0,col].set_title(pack.title()+' · '+mode)
 axes[0,col].axhline(50,color='#9d3442',linestyle='--',linewidth=1)
axes[0,0].set_ylabel('Server event P95 (ms)')
axes[1,0].set_ylabel('Mean carrier callback / tick (ms)')
axes[1,0].legend(loc='lower left')
fig.suptitle('Scoped geometry reuse · identical occupied-world copies',fontsize=16,y=.98)
fig.text(.5,.035,'21 cars · 48 carriers · 96 native passengers · 600 ticks/window · shared Ryzen 9 7900X3D\nOld → new in round 1; new → old in round 2. Dashed line: 50 ms development budget. No clients.',ha='center',fontsize=10)
fig.tight_layout(rect=[0,.095,1,.94])
fig.savefig(b/'comparison.png',dpi=150)
fig.savefig(b/'comparison.svg')
