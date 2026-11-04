from pathlib import Path
import json,re,hashlib,gzip,math
base=Path(__file__).resolve().parent
meta=json.loads((base/'metadata.json').read_text())
assert 'exit_code' in meta, 'Runtime not terminal'
log=(base/'gradle.txt').read_text() if (base/'gradle.txt').exists() else gzip.decompress((base/'gradle.txt.gz').read_bytes()).decode()
pattern=r'FD_CHAIN_AVAILABILITY group=(\w+) case=(\d+) source=(\S+) state=(\S+) targetSpeed=(\S+) maxLinearDelta=(\S+) maxAngularDelta=(\S+) zeroVelocityBaseline=true velocityRestored=true'
probes=[{'group':g,'case':int(i),'source':source,'state':state,'target_speed':float(target),'linear_delta':float(linear),'angular_delta':float(angular)} for g,i,source,state,target,linear,angular in re.findall(pattern,log)]
recovery=[p for p in probes if p['group']=='recovery']
chassis=[p for p in probes if p['group']=='chassis']
expected_recovery={(i,'ordinary-bearing',state) for i in range(3) for state in ('braked','healthy','unloaded-before-tick','unloaded-after-tick')}
expected_chassis={(i,str(s),f'{state}-{i}') for i in range(6) for s in range(2) for state in ('held-before','healthy-before')}
expected_chassis|={(i,str(s),f'unloaded-{i}') for i in range(6) for s in range(2) if i%3!=2 or s!=i//3}
expected_chassis|={(i,str(s),f'restored-{i}') for i in (0,1,3,4) for s in range(2)}
keys=lambda rows:{(p['case'],p['source'],p['state']) for p in rows}
inputs=re.findall(r'FD_CHAIN_AVAILABILITY_INPUT group=(\w+) case=(\d+) source=(\S+) state=(\S+) bodies=([^\n]+)',log)
input_keys={(g,int(i),s,state) for g,i,s,state,_ in inputs}
input_values_present=all('originalLinear=' in values and 'originalAngular=' in values for *_,values in inputs)
marks={g:re.findall(r'FD_CHAIN_'+g.upper()+r'_AVAILABILITY complete[^\n]*',log) for g in ('recovery','chassis')}
evidence={
 'recovery_exact_cases_sources_states':len(recovery)==12 and keys(recovery)==expected_recovery,
 'chassis_exact_cases_sources_states':len(chassis)==42 and keys(chassis)==expected_chassis,
 'original_velocities_logged_for_every_call':len(inputs)==len(probes)==54 and input_values_present and input_keys=={(p['group'],p['case'],p['source'],p['state']) for p in probes},
 'all_powered_finite_targets':len(probes)==54 and all(math.isfinite(p['target_speed']) and abs(p['target_speed'])>.001 for p in probes),
 'recovery_three_unloads':re.findall(r'FD_CHAIN_RECOVERY_AVAILABILITY unload case=(\d+)',log)==['0','1','2'],
 'chassis_six_unloads':re.findall(r'FD_CHAIN_CHASSIS_AVAILABILITY unload case=(\d+)',log)==list(map(str,range(6))),
 'recovery_three_restores':re.findall(r'FD_CHAIN_RECOVERY_AVAILABILITY restored case=(\d+)',log)==['0','1','2'],
 'chassis_six_restores':re.findall(r'FD_CHAIN_CHASSIS_AVAILABILITY restored case=(\d+)',log)==list(map(str,range(6))),
 'recovery_actual_counters':any('nativeUnloads=3 brakedCalls=3 healthyCalls=3 unavailableCalls=3 afterTickCalls=3' in m for m in marks['recovery']),
 'chassis_actual_counters':any('nativeUnloads=6 unavailableCalls=10 positiveCalls=20 heldCalls=12' in m for m in marks['chassis']),
 'source_clean_after':meta['source_status_after']==[],
 'jar_matches_preflight':hashlib.sha256((base/'baseline.jar').read_bytes()).hexdigest()==meta['jar']['sha256'],
}
max_delta=lambda p:max(p['linear_delta'],p['angular_delta'])
finite=lambda p:math.isfinite(p['linear_delta']) and math.isfinite(p['angular_delta'])
select=lambda rows,prefix:[p for p in rows if p['state'].startswith(prefix)]
zero=lambda rows,n:len(rows)==n and all(finite(p) and max_delta(p)<1e-9 for p in rows)
positive=lambda rows,n:len(rows)==n and all(finite(p) and max_delta(p)>.001 for p in rows)
safety={'recovery_braked_zero':zero(select(recovery,'braked'),3),'recovery_healthy_nonzero':positive(select(recovery,'healthy'),3),'recovery_before_tick_zero':zero(select(recovery,'unloaded-before'),3),'recovery_after_tick_zero':zero(select(recovery,'unloaded-after'),3),'chassis_held_zero':zero(select(chassis,'held-before'),12),'chassis_healthy_nonzero':positive(select(chassis,'healthy-before'),12),'chassis_unavailable_zero':zero(select(chassis,'unloaded-'),10),'chassis_restored_positive':positive(select(chassis,'restored-'),8)}
result={'source':meta['source_revision'],'exit_code':meta['exit_code'],'evidence_checks':evidence,'safety_checks':safety,'probes':probes,'completion_markers':marks,'other_integration_markers':re.findall(r'FD_ENGINEERED_(?:RECOVERY_)?AVAILABILITY complete[^\n]*',log),'suite_markers':re.findall(r'[^\n]*(?:required tests (?:failed|passed)|GAME TESTS COMPLETE|Stopping server|BUILD FAILED|BUILD SUCCESSFUL)[^\n]*',log),'limits':meta['limits']}
(base/'review.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'evidence_checks':evidence,'safety_checks':safety,'completion_markers':marks,'suite_markers':result['suite_markers']},indent=2))
