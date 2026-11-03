from pathlib import Path
import datetime,hashlib,json,os,re,shutil,subprocess,sys,zipfile
out=Path(sys.argv[1]).resolve();expected=sys.argv[2]
assert Path.cwd()==Path('/home/braydon/.codex/worktrees/pax-active-restart/Coasters: Fourth Dimension')
head=lambda:subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()
status=lambda:subprocess.check_output(['git','status','--porcelain'],text=True).splitlines()
assert head()==expected and status()==[], 'Source must be frozen and clean'
healthy_control='src/main/java/dev/fourthdimension/gametest/EngineeredInterlockGameTests.java'
assert Path(healthy_control).read_bytes()==subprocess.check_output(['git','show','e8c05ae:'+healthy_control]), 'Current release healthy recovery controls changed'
healthy_control_sha256=hashlib.sha256(Path(healthy_control).read_bytes()).hexdigest()
assert not out.exists(), 'Refusing to overwrite an existing run'
for proc in Path('/proc').iterdir():
 if not proc.name.isdigit():continue
 try:
  if Path(os.readlink(proc/'exe')).name=='java' and b'GradleDaemon' not in (proc/'cmdline').read_bytes():raise RuntimeError('Existing non-daemon Java process: '+proc.name)
 except OSError:pass
out.mkdir()
shutil.copy2(__file__,out/'prepare-and-run.py')
shutil.copy2('/tmp/fd-merged-availability-review.py',out/'review-release.py')
prior=Path('/tmp/fd-native-chain-availability-ec731db')
for name in ('run.py','observe.py','review-run.py','review-engineered.py'):
 text=(prior/name).read_text().replace('shared availability guard replay','isolated current-release availability regression')
 (out/name).write_text(text)
cmd=['./gradlew','compileJava','jar','--offline','--no-daemon','-PfdIntegrations=engineered']
compile_meta={'source':expected,'command':cmd,'start_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
with (out/'compile.txt').open('w') as log:result=subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT)
compile_meta.update(exit_code=result.returncode,end_utc=datetime.datetime.now(datetime.timezone.utc).isoformat())
(out/'compile-metadata.json').write_text(json.dumps(compile_meta,indent=2)+'\n')
print('Merged-release compile/JAR exit',result.returncode,flush=True)
if result.returncode:sys.exit(result.returncode)
assert head()==expected and status()==[], 'Source changed during compile'
version=re.search(r'^mod_version=(.+)$',Path('gradle.properties').read_text(),re.M).group(1)
jar=Path('/tmp/fourthdimension-2aa0362f/build/libs')/('coasters-fourth-dimension-'+version+'.jar')
classes=Path('/tmp/fourthdimension-2aa0362f/build/classes/java/main')
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(prior/'baseline.jar') as isolated:
 names=[n for n in z.namelist() if n.endswith('.class')]
 assert names and all((classes/n).is_file() and (classes/n).read_bytes()==z.read(n) for n in names), 'Runtime/JAR class mismatch'
 added=[n for n in names if n not in isolated.namelist()]
 changed=[n for n in names if n in isolated.namelist() and z.read(n)!=isolated.read(n)]
 focused=[n for n in isolated.namelist() if n.endswith('.class') and any(n.startswith(prefix) for prefix in ['dev/fourthdimension/integration/PropulsionInterlock','dev/fourthdimension/integration/EngineeredInterlock','dev/fourthdimension/gametest/ChainAvailabilityProbe','dev/fourthdimension/gametest/ChainChassisAvailabilityGameTests','dev/fourthdimension/gametest/ChainRecoveryAvailabilityGameTests','dev/fourthdimension/gametest/EngineeredRecoveryAvailabilityGameTests','dev/fourthdimension/gametest/EngineeredAvailabilityGameTests'])]
 assert all(n in names and z.read(n)==isolated.read(n) for n in focused), 'Guard/probe classes differ from reviewed isolated candidate'
count=sum(len(re.findall(r'@GameTest\s*\(',p.read_text())) for p in Path('src/main/java').rglob('*.java'))
meta=json.loads((prior/'preflight.json').read_text())
meta.update(source_revision=expected,source_status=[],status='merged release compiled; regression ready',captured_at_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),workspace=str(Path.cwd()),jar={'path':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},expected_suite_tests=count,all_packaged_class_parity_count=len(names),runtime_kind='isolated current-release development loose classes, not packaged-JAR runtime',runner_invocation=['flock','-n','/tmp/fd-gametest-runtime.lock','python3',str(out/'prepare-and-run.py'),str(out),expected],isolated_reference_source='ec731db',focused_classes_identical_to_isolated=focused,changed_classes_from_isolated=changed,added_classes_from_isolated=added)
meta.pop('changed_classes_from_baseline',None)
meta['healthy_control_source']={'path':healthy_control,'identical_to_source':'e8c05ae','sha256':healthy_control_sha256,'reason':'Current release adds positive explicit-start recovery control absent from isolated ec731db reference'}
meta['limits']=['Direct callback availability evidence','Current release Engineered-only profile; not Styles+Engineered','No natural scheduler, automatic relink or packaged client acceptance claim']
(out/'preflight.json').write_text(json.dumps(meta,indent=2)+'\n')
(out/'source-preflight.json').write_text(json.dumps({'source':expected,'status':status(),'class_count':len(names),'all_packaged_classes_match_runtime':True,'focused_classes_identical_to_isolated':focused,'changed_classes_from_isolated':changed,'added_classes_from_isolated':added,'source_GameTest_annotation_count':count},indent=2)+'\n')
print('Merged-release classes',len(names),'source GameTest annotations',count,flush=True)
sys.exit(subprocess.call([sys.executable,str(out/'run.py')]))
