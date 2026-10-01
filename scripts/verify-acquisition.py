#!/usr/bin/env python3
"""Audit native recipes, real-player discovery, material use and evidence provenance."""
import gzip,hashlib,json,re,subprocess,sys
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(sys.argv[1])
def text(name):
 p=root/name
 return gzip.decompress(p.read_bytes()).decode() if p.suffix=='.gz' else p.read_text()
def data(name):return json.loads(text(name))
m=data('manifest.json')
for name,h in m['files'].items():assert hashlib.sha256((root/name).read_bytes()).hexdigest()==h,name
expected={f'{f}_{part}':16 if part=='track' else 1 for f in ['arrow','legacy_zacspin','free_spin','revised_zacspin','pax'] for part in ['track','car']}
expected.update(boarding_station=1,rotation_tool=1,shuttle_winch=1,magnetic_inducer=2,magnetic_damper=2)
profiles={}
for profile,skips in [('base',7),('combined',0)]:
 log=text('native-'+profile+'.log.gz')
 assert text('native-'+profile+'.exit').strip()=='0' and 'All 36 required tests passed' in log and 'BUILD SUCCESSFUL' in log
 assert len(re.findall(r'^FD_\w+_SKIPPED:',log,re.M))==skips
 rows=[dict(re.findall(r'(\w+)=(\S+)',line)) for line in log.splitlines() if line.startswith('FD_ACQUISITION ')]
 assert len(rows)==15 and {r['recipe'].split(':')[1]:int(r['count']) for r in rows}==expected
 assert all(r[k]=='true' for r in rows for k in ['missingRejected','unrelatedRejected','discovered'])
 versions={mod:version for version,mod in re.findall(r'^\t\t.+ ([^ ]+) \(([^)]+)\)$',log,re.M)}
 assert versions==m['loaded_mod_versions'][profile]
 for addon in ['coastersengineered','coasterssimulatedextratypes','coasterfins','coastersmagnetized','coasters_immersion']:assert (addon in versions)==(profile=='combined')
 for marker in ['FD_DEMO family=ARROW','FD_DEMO family=ZACSPIN','FD02_NATIVE applications=','FD_REVISED_NATIVE angles=','FD_SHUTTLE_TALL cycles=2']:
  assert marker in log,marker
 for phase in ['LIFTING','OUTBOUND','RETURNING','CAPTURING']:assert 'FD_PAX_PHASE_COMPLETE target='+phase in log
 profiles[profile]={'registered':36,'optional_skips':skips,'recipes':15}
assert data('native-status.json')['status']=='complete' and text('native.exit').strip()=='0'
live=data('live-discovery.json');assert len(live)==15 and {r['recipe'].split(':')[1] for r in live}==set(expected)
assert all(not r['initiallyKnown'] and not r['dirtUnlocked'] and r['ingredientUnlocked'] for r in live)
commands=text('commands.txt.gz')
for r in live:
 assert 'recipe take Dev '+r['recipe'] in commands and 'give Dev '+r['trigger'] in commands
saved=json.loads(subprocess.check_output([sys.executable,str(Path(__file__).with_name('inspect-foundation-save.py')),str(root/'player-state.dat')],text=True))
assert saved['playerGameType']==0 and saved['Inventory']==[{'count':16,'Slot':8,'id':'fourthdimension:arrow_track'}]
assert {k for k in saved['recipeBook']['recipes'] if k.startswith('fourthdimension:')}=={'fourthdimension:'+k for k in expected}
adv=data('player-advancements.json')
assert all(adv['fourthdimension:recipes/'+name]['done'] for name in expected)
craft=data('live-craft.json');assert craft['output_count']==16 and craft['remaining_materials']==0 and craft['materials']=={'minecraft:iron_ingot':6,'minecraft:copper_ingot':2,'minecraft:red_dye':1}
captures=data('tooltip-captures.json');assert set(captures['items'])==set(expected)
assert all((root/('tooltip-'+name+'.png')).is_file() for name in expected)
assert data('visual-review.json')['all_descriptions_readable'] and data('visual-review.json')['ponder_prompts_present']
diff=data('artifact-diff.json');assert diff['production_classes_and_resources_identical']
assert all(n.startswith('dev/fourthdimension/gametest/AcquisitionGameTests') for n in diff['only_changed_entries'])
for side in ['visual','native']:
 assert m['artifacts'][side]==diff[side]
assert data('teaching-reuse.json')['ponder_classes_and_structures_identical'] and data('teaching-reuse.json')['prior_language_entries_preserved']
for name in ['dev','server']:assert text(name+'.exit').strip()=='0' and 'BUILD SUCCESSFUL' in text(name+'.log.gz')
cleanup=data('cleanup.json');assert not cleanup['live'] and cleanup['closed_loopback_ports']==[25585,25586]
restore=data('settings-restored.json');assert restore['before']==restore['after']
suites=[ET.parse(p).getroot() for p in (root/'unit-tests').glob('TEST-*.xml')]
assert sum(int(s.attrib['tests']) for s in suites)==19 and all(int(s.attrib[k])==0 for s in suites for k in ['errors','failures','skipped'])
print(json.dumps({'profiles':profiles,'unit_tests':19,'live_discovery':15,'reviewed_tooltips':15,'live_craft_output':16,'saved_survival_inventory_verified':True,'cleanup_verified':True},indent=2))
