#!/usr/bin/env python3
"""Check preserved native-run observations; does not certify client/lifecycle gates."""
from pathlib import Path
import json,re,sys,hashlib,gzip
folder=Path(sys.argv[1]); m=json.loads((folder/'metadata.json').read_text()); text=(folder/'console.log').read_text() if (folder/'console.log').exists() else gzip.decompress((folder/'console.log.gz').read_bytes()).decode()
checks=[]
def check(label,condition):
 checks.append((label,bool(condition)))
profile=m['profile']; engineered=profile in ('engineered','combined'); styles=profile in ('styles','combined')
check('actual process exit',m['exit_code']==0)
check('native summary',m['required_test_summaries']==['49'])
check('Gradle success','BUILD SUCCESSFUL' in text and 'BUILD FAILED' not in text)
check('server shutdown','Stopping server' in text and 'All dimensions are saved' in text)
check('source stable',not m['working_tree_after'] and m['source_after']==m['source'])
check('five-family directed coupling','FD_FAMILY_MATRIX decisions=25 factoryRejects=20 lockedNativeInitialSnapRejects=20 mixedConsistRejects=20 bindingPreserved=true' in text)
check('PAX exact-curve repair',bool(re.search(r'FD_PAX_CURVE_RESTORE geometry=-?\d+ repairedCars=4',text)))
check('Engineered installation',('Coasters: Engineered 1.1.1 (coastersengineered)' in text)==engineered)
check('Styles installation',('Coasters Simulated: Track Styles 1.2.0 (coasterssimulatedextratypes)' in text)==styles)
check('Engineered exercise',('FD_ENGINEERED_INTERLOCK complete blocked=18 positive=4 nativeCalls=22' in text)==engineered)
check('Engineered energy exercise',('FD_ENGINEERED_FE installed=1.1.1 nativeCapability=true unloaded=true' in text)==engineered)
for name,marker in [('four-family motor','FD_ENGINEERED installed=1.1.1 fourFamilies=launch/stop/capture/retention PAX=held'),('ordinary-cart STOP','FD_ENGINEERED ordinary STOP speed='),('powered controlled train','FD_ENGINEERED_TRAIN fullTurns=')]:
 check(name+' exercise',(marker in text)==engineered)
if not engineered:
 check('five explicit addon-absent returns',text.count('not exercised')==5)
check('Styles stroke cases',f'mechanicsUnchanged=true styleFamilyCases=10 strokeCases={2 if styles else 0}' in text)
check('performance property','-PfdPerformanceTargets' in m['command'])
check('runtime performance property','-Dfourthdimension.performanceTargetValidation=true' in (folder/'gameTestServerRunVmArgs.txt').read_text())
for scene in ('CONTROLLED','SHUTTLE','FREE_SPIN'):
 line=next((s for s in text.splitlines() if 'FD_PERFORMANCE_'+scene in s),'')
 values=dict(re.findall(r'(\w+)=(\d+)',line))
 check(scene+' invocation target',0<int(values.get('averageNanosPerInvocation','0'))<=120000)
 check(scene+' carrier target',0<int(values.get('averageNanosPerCarrier','0'))<=6000)
check('runtime dependency hashes',len(m['runtime_classpath'])>0)
check('compiled class hashes',any(k.endswith('.class') for k in m['compiled_files']))
for label,ok in checks: print(('PASS' if ok else 'FAIL')+': '+label)
sys.exit(0 if all(ok for _,ok in checks) else 1)
