from pathlib import Path
import datetime,hashlib,json,os,subprocess,sys,zipfile
out=Path(__file__).resolve().parent
expected='ec731dbdefe7b3adb6c4f21395993ce66a72872c'
head=lambda:subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip()
status=lambda:subprocess.check_output(['git','status','--porcelain'],text=True).splitlines()
assert head()==expected and status()==[], 'Source must be frozen and clean'
assert not (out/'compile.txt').exists(), 'Refusing to overwrite compile evidence'
for proc in Path('/proc').iterdir():
 if not proc.name.isdigit():continue
 try:
  if Path(os.readlink(proc/'exe')).name=='java' and b'GradleDaemon' not in (proc/'cmdline').read_bytes():
   raise RuntimeError('Existing non-daemon Java process: '+proc.name)
 except OSError:pass
plan=json.loads((out/'pending-validation.json').read_text())
compile_meta={'source':expected,'command':plan['compile_command'],'start_utc':datetime.datetime.now(datetime.timezone.utc).isoformat()}
with (out/'compile.txt').open('w') as log:
 result=subprocess.run(plan['compile_command'],stdout=log,stderr=subprocess.STDOUT)
compile_meta.update(exit_code=result.returncode,end_utc=datetime.datetime.now(datetime.timezone.utc).isoformat())
(out/'compile-metadata.json').write_text(json.dumps(compile_meta,indent=2)+'\n')
print('Compile/JAR exit',result.returncode,flush=True)
if result.returncode:sys.exit(result.returncode)
assert head()==expected and status()==[], 'Source changed during compile'
jar=Path('/tmp/fourthdimension-2aa0362f/build/libs/coasters-fourth-dimension-0.9.0-dev.27.jar')
classes=Path('/tmp/fourthdimension-2aa0362f/build/classes/java/main')
old=Path('/tmp/fd-native-chain-availability-0fc8a76/baseline.jar')
with zipfile.ZipFile(jar) as z, zipfile.ZipFile(old) as before:
 names=[n for n in z.namelist() if n.endswith('.class')]
 assert len(names)==233
 assert all((classes/n).is_file() and (classes/n).read_bytes()==z.read(n) for n in names), 'Runtime/JAR class mismatch'
 changed=[n for n in names if z.read(n)!=before.read(n)]
 assert changed==['dev/fourthdimension/integration/PropulsionInterlock.class'], changed
meta=json.loads(Path('/tmp/fd-native-chain-availability-0fc8a76/preflight.json').read_text())
meta.update(source_revision=expected,source_status=[],status='shared availability guard compiled; replay ready',captured_at_utc=datetime.datetime.now(datetime.timezone.utc).isoformat(),production_guard='PropulsionInterlock adds shared availability and attached-recovery coupling/removal holds',jar={'path':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest()},expected_required_failures=0,changed_classes_from_baseline=changed,baseline_source='0fc8a76',runner_invocation=['flock','-n','/tmp/fd-gametest-runtime.lock','python3',str(out/'prepare-and-run.py')])
meta.pop('production_guards_byte_identical_to',None)
(out/'preflight.json').write_text(json.dumps(meta,indent=2)+'\n')
(out/'source-preflight.json').write_text(json.dumps({'source':expected,'status':status(),'class_count':len(names),'all_packaged_classes_match_runtime':True,'changed_classes_from_baseline':changed},indent=2)+'\n')
sys.exit(subprocess.call([sys.executable,str(out/'run.py')]))
