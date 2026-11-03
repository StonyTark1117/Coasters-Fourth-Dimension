from pathlib import Path
import json, re, hashlib, zipfile, gzip
base=Path(__file__).resolve().parent
meta=json.loads((base/'metadata.json').read_text())
assert 'exit_code' in meta, 'Runtime has not exited'
# GameTest System.out diagnostics are captured in Gradle stdout, not latest.log.
log=(base/'gradle.txt').read_text() if (base/'gradle.txt').exists() else gzip.decompress((base/'gradle.txt.gz').read_bytes()).decode()
probes=[{'source':int(s),'state':state,'max_queued_delta':float(delta)} for s,state,delta in re.findall(r'FD_ENGINEERED_AVAILABILITY source=(\d+) state=(\S+) maxQueuedDelta=(\S+)',log)]
unavailable=[p for p in probes if p['state'].startswith('unloaded-')]
positive=[p for p in probes if not p['state'].startswith('unloaded-')]
unloads=[int(i) for i in re.findall(r'FD_ENGINEERED_AVAILABILITY unload case=(\d+)',log)]
restored=[int(i) for i in re.findall(r'FD_ENGINEERED_AVAILABILITY restored case=(\d+)',log)]
links=[{'case':int(i),'sizes':[int(v.strip()) for v in sizes.split(',')],'partial_chassis_restore':chassis=='true'} for i,sizes,chassis in re.findall(r'FD_ENGINEERED_AVAILABILITY restoredLinks case=(\d+) sizes=\[([^]]+)\] partialChassisRestore=(true|false)',log)]
expected_unavailable={(i,s) for i in range(6) for s in range(2) if i%3!=2 or s!=i//3}
expected_positive={(f'healthy-before-{i}',s) for i in range(6) for s in range(2)}|{(f'restored-{i}',s) for i in (0,1,3,4) for s in range(2)}
checks={
 'six_distinct_unloads':unloads==list(range(6)),
 'six_same_id_restore_assertions':restored==list(range(6)),
 'ten_required_unavailable_sources':len(unavailable)==10 and {(int(p['state'].split('-')[-1]),p['source']) for p in unavailable}==expected_unavailable,
 'all_unavailable_force_torque_deltas_zero':len(unavailable)==10 and all(p['max_queued_delta']==0 for p in unavailable),
 'twenty_required_positive_controls':len(positive)==20 and {(p['state'],p['source']) for p in positive}==expected_positive,
 'positive_controls_nonzero':len(positive)==20 and all(p['max_queued_delta']>.001 for p in positive),
 'carrier_link_continuity':len(links)==6 and all(p['sizes']==[2,2] for p in links if not p['partial_chassis_restore']),
 'complete_without_recorded_failures':'FD_ENGINEERED_AVAILABILITY complete nativeUnloads=6 unavailableCalls=10 positiveCalls=20 failures=[]' in log,
 'all_51_required_tests_passed':'All 51 required tests passed' in log,
 'gradle_exit_zero':meta['exit_code']==0,
 'jar_hash_matches_preflight':hashlib.sha256((base/'baseline.jar').read_bytes()).hexdigest()==meta['jar']['sha256'],
 'source_clean_after':meta['source_status_after']==[],
}
with zipfile.ZipFile(base/'baseline.jar') as jar: checks['jar_crc_valid']=jar.testzip() is None
result={'checks':checks,'source':meta['source_revision'],'unloads':unloads,'restored':restored,'link_states':links,'unavailable_probes':unavailable,'positive_controls':positive,'completion_markers':re.findall(r'[^\n]*(?:tests are now done|required tests failed|All \d+ required tests passed|Stopping server)[^\n]*',log),'limits':meta['limits']}
(base/'review.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'checks':checks,'completion_markers':result['completion_markers']},indent=2))
