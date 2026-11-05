#!/usr/bin/env bash
set -u
TASK_TMP=/tmp/fd401-stale-font-current
ROOT='/home/braydon/Documents/Coasters: Fourth Dimension'
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
export ALSOFT_DRIVERS=null
python3 "$ROOT/docs/evidence/0.9/fd401-removed-stale-probe-c31d08a/disable-stale-font-after-load.py" \
  "$TASK_TMP/game/logs/latest.log" \
  "$TASK_TMP/game/resourcepacks/fd-font-probe/assets/fd_font_probe/font/transient.json" \
  > "$TASK_TMP/watcher.json" 2> "$TASK_TMP/watcher.stderr" &
WATCHER_PID=$!
timeout --signal=INT --kill-after=10s 420s xvfb-run -a ./gradlew runClient \
  --offline --no-daemon --console=plain \
  -PfdBuildDirectory="$TASK_TMP/build" \
  -PfdClientGameDirectory="$TASK_TMP/game" \
  -PfdClientMaxHeap=1536M \
  -PfdPackaged -PfdValidation -PfdIntegrations=styles,engineered \
  -PfdWorld=FD401OccupiedReloadStrict1a91416 \
  -PfdFontValidation -PfdFontValidationReload -PfdFontValidationChat \
  -PfdFontValidationExit -PfdStaleRemovedFontProbe=fd_font_probe:transient \
  > "$TASK_TMP/gradle-console.log" 2>&1
CLIENT_STATUS=$?
wait "$WATCHER_PID"
WATCHER_STATUS=$?
printf 'client_exit=%s watcher_exit=%s\n' "$CLIENT_STATUS" "$WATCHER_STATUS" | tee "$TASK_TMP/exit-status.txt"
if (( CLIENT_STATUS != 0 || WATCHER_STATUS != 0 )); then exit 1; fi
