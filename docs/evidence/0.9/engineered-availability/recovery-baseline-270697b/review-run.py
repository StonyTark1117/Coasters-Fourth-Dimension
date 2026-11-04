from pathlib import Path
import json,re,hashlib,gzip
base=Path(__file__).resolve().parent
meta=json.loads((base/'metadata.json').read_text())
assert 'exit_code' in meta, 'Runtime not terminal'
log=(base/'gradle.txt').read_text() if (base/'gradle.txt').exists() else gzip.decompress((base/'gradle.txt.gz').read_bytes()).decode()
probes=[{'case':int(i),'state':state,'max_queued_delta':float(delta)} for i,state,delta in re.findall(r'FD_ENGINEERED_RECOVERY_AVAILABILITY case=(\d+) state=(\S+) source=ordinary-bearing maxQueuedDelta=(\S+)',log)]
states={state:[p for p in probes if p['state']==state] for state in ('braked','healthy','unloaded-before-tick','unloaded-after-tick')}
unloads=[int(i) for i in re.findall(r'FD_ENGINEERED_RECOVERY_AVAILABILITY unload case=(\d+)',log)]
restores=[int(i) for i in re.findall(r'FD_ENGINEERED_RECOVERY_AVAILABILITY restored case=(\d+)',log)]
complete=re.findall(r'FD_ENGINEERED_RECOVERY_AVAILABILITY complete[^\n]*',log)
evidence_checks={'three_distinct_unloads':unloads==[0,1,2],'three_same_id_restores':restores==[0,1,2],'all_four_states_three_cases':all(len(ps)==3 and {p['case'] for p in ps}=={0,1,2} for ps in states.values()),'actual_completion_counts':any('nativeUnloads=3 brakedCalls=3 healthyCalls=3 unavailableCalls=3 afterTickCalls=3' in line for line in complete),'source_clean_after':meta['source_status_after']==[],'jar_matches_preflight':hashlib.sha256((base/'baseline.jar').read_bytes()).hexdigest()==meta['jar']['sha256']}
safety_checks={'braked_zero':len(states['braked'])==3 and all(p['max_queued_delta']<1e-9 for p in states['braked']),'healthy_nonzero':len(states['healthy'])==3 and all(p['max_queued_delta']>.001 for p in states['healthy']),'before_tick_zero':len(states['unloaded-before-tick'])==3 and all(p['max_queued_delta']<1e-9 for p in states['unloaded-before-tick']),'after_tick_zero':len(states['unloaded-after-tick'])==3 and all(p['max_queued_delta']<1e-9 for p in states['unloaded-after-tick'])}
result={'source':meta['source_revision'],'exit_code':meta['exit_code'],'evidence_checks':evidence_checks,'safety_checks':safety_checks,'probes':probes,'completion_markers':complete,'suite_markers':re.findall(r'[^\n]*(?:required tests (?:failed|passed)|GAME TESTS COMPLETE|Stopping server|BUILD FAILED|BUILD SUCCESSFUL)[^\n]*',log),'limits':meta['limits']}
(base/'review.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
