from pathlib import Path
import gzip,json,re,math,hashlib,subprocess,sys
base=Path(__file__).resolve().parent
meta=json.loads((base/'metadata.json').read_text())
assert 'exit_code' in meta, 'Runtime has not exited'
for name in ('review-run.py','review-engineered.py'):
 subprocess.run([sys.executable,str(base/name)],check=True,stdout=subprocess.DEVNULL)
log=(base/'gradle.txt').read_text() if (base/'gradle.txt').exists() else gzip.decompress((base/'gradle.txt.gz').read_bytes()).decode()
chain=json.loads((base/'review.json').read_text())
engineered=json.loads((base/'engineered-review.json').read_text())
counts=[int(x) for x in re.findall(r'(\d+) GAME TESTS COMPLETE',log)]
passed=[int(x) for x in re.findall(r'All (\d+) required tests passed',log)]
failed=[int(x) for x in re.findall(r'(\d+) required tests failed',log)]
capture_path=base/'native-exports/manifest.json'
if not capture_path.exists():capture_path=base/'native-export-capture.json'
capture=json.loads(capture_path.read_text()) if capture_path.exists() else {}
checks={
 'compile_exit_zero':json.loads((base/'compile-metadata.json').read_text())['exit_code']==0,
 'native_export_capture_complete':capture.get('capture_complete') is True,
 'single_complete_suite_count_matches_source':counts==[meta['expected_suite_tests']],
 'all_required_tests_passed':passed==[meta['expected_suite_tests']] and not failed,
 'gradle_exit_zero':meta['exit_code']==0,
 'normal_server_stop':'Stopping server' in log,
 'source_clean_after':meta['source_status_after']==[],
 'jar_matches_preflight':hashlib.sha256((base/'baseline.jar').read_bytes()).hexdigest()==meta['jar']['sha256'],
 'chain_evidence_complete':all(chain['evidence_checks'].values()),
 'chain_safety_passed':all(chain['safety_checks'].values()),
 'engineered_evidence_complete':all(engineered['evidence_checks'].values()),
 'engineered_safety_passed':all(engineered['safety_checks'].values()),
 'engineered_deltas_finite':len(engineered['probes'])==12 and all(math.isfinite(p['max_queued_delta']) for p in engineered['probes']),
}
result={'source':meta['source_revision'],'profile':meta['profile'],'expected_source_test_count':meta['expected_suite_tests'],'actual_complete_test_counts':counts,'actual_passed_required_counts':passed,'actual_failed_required_counts':failed,'checks':checks,'bounded_regression_passed':all(checks.values()),'failures':re.findall(r'[^\n]*failed at[^\n]*',log),'fe_recovery_markers':re.findall(r'FD_ENGINEERED_RECOVERY_FE[^\n]*',log),'jar_sha256':meta['jar']['sha256'],'runtime_kind':meta['runtime_kind'],'limits':meta['limits']}
(base/'acceptance.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2))
