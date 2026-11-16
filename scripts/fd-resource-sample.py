#!/usr/bin/env python3
"""Read one-second CPU/RSS/cgroup samples from an owned private Minecraft run.

Uses fd-private-client.py ownership checks; does not launch, attach to, or send
input to a JVM. Heap and GC are separate JVM collector measurements.
"""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import time


def read_text(path):
    try:
        return path.read_text().strip()
    except FileNotFoundError:
        return None


def key_values(text):
    fields = [line.split(None, 1) for line in (text or "").splitlines() if line.strip()]
    return {parts[0]: parts[1] if len(parts) > 1 else "" for parts in fields}


def cgroup_samples(group, root=Path("/sys/fs/cgroup")):
    path = root / group.lstrip("/")
    if not path.resolve().is_relative_to(root.resolve()):
        raise RuntimeError("Invalid cgroup path")
    samples = []
    while True:
        samples.append({"path": str(path.relative_to(root)),
                        **{name: read_text(path / name) for name in
                           ("memory.current", "memory.peak", "memory.max", "memory.events",
                            "cpu.max", "cpu.stat", "cpu.pressure", "memory.pressure")}})
        if path == root:
            break
        path = path.parent
    return samples


def process_sample(pid, expected_group, proc=Path("/proc")):
    path = proc / str(pid)
    # Parentheses can occur inside the process name; fields after the last ')'
    # start at field 3. Preserve starttime to detect PID reuse across a window.
    stat = (path / "stat").read_text().rsplit(")", 1)[1].split()
    groups = [line.split(":", 2)[-1] for line in (path / "cgroup").read_text().splitlines()]
    if expected_group not in groups:
        raise RuntimeError("Process left its recorded cgroup")
    status = key_values((path / "status").read_text())
    return {"pid": int(pid), "start_ticks": int(stat[19]),
            "cpu_user_ticks": int(stat[11]), "cpu_system_ticks": int(stat[12]),
            "rss_kib": int(status.get("VmRSS:", "0 kB").split()[0]),
            "rss_peak_kib": int(status.get("VmHWM:", "0 kB").split()[0]),
            "threads": int(status.get("Threads:", "0")), "cgroup": expected_group}


def game_pid(state):
    # Generated launch scripts may keep a shell as MainPID. Measure the actual
    # game JVM within the verified owned unit, not that shell's tiny RSS/CPU.
    group = state["ControlGroup"]
    candidates = []
    for value in (Path("/sys/fs/cgroup") / group.lstrip("/") / "cgroup.procs").read_text().split():
        if (Path("/proc") / value / "comm").read_text().strip() == "java":
            candidates.append(value)
    if len(candidates) != 1:
        raise RuntimeError("Expected exactly one game JVM in the owned unit")
    return candidates[0]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("run", type=Path)
    parser.add_argument("--owner", required=True)
    parser.add_argument("--seconds", type=int, required=True)
    parser.add_argument("--parts", nargs="+", choices=("client", "server"), default=["client", "server"])
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--stop-file", type=Path,
                        help="Optional persistent stop marker for a bounded variable-length segment")
    args = parser.parse_args()
    if not 1 <= args.seconds <= 3600:
        parser.error("Choose a bounded window of 1–3600 seconds")
    output = args.output.resolve()
    if output.is_relative_to("/tmp"):
        parser.error("Use persistent evidence storage")
    if args.stop_file is not None:
        args.stop_file = args.stop_file.resolve()
        if args.stop_file.is_relative_to("/tmp") or args.stop_file.exists():
            parser.error("Use a new persistent stop marker")
    spec = importlib.util.spec_from_file_location("private_client", Path(__file__).with_name("fd-private-client.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    private = module.PrivateRun(args.run, args.owner)
    initial = {part: private.state(part) for part in args.parts}
    game_pids = {part: game_pid(s) for part, s in initial.items()}
    identities = {part: process_sample(game_pids[part], s["ControlGroup"])["start_ticks"]
                  for part, s in initial.items()}
    output.parent.mkdir(parents=True, exist_ok=True)
    start = time.monotonic()
    with output.open("x") as stream:
        stream.write(json.dumps({"kind": "manifest", "owner": args.owner,
                                 "run": str(args.run.resolve()), "seconds": args.seconds,
                                 "clock_ticks_per_second": os.sysconf("SC_CLK_TCK"),
                                 "units": initial, "game_pids": game_pids, "collector": "fd-resource-sample-v3",
                                 "stop_file": str(args.stop_file) if args.stop_file else None,
                                 "scope": "process RSS/CPU and cgroup ancestors; no heap/GC/frame claim"}) + "\n")
        samples = 0
        reason = "duration"
        for index in range(args.seconds):
            time.sleep(max(0, start + index - time.monotonic()))
            if args.stop_file is not None and args.stop_file.exists():
                reason = "stop_file"
                break
            row = {"kind": "resource", "unix_ns": time.time_ns(), "monotonic_ns": time.monotonic_ns(),
                   "sample": index, "host_meminfo": key_values(read_text(Path("/proc/meminfo"))),
                   "host_loadavg": read_text(Path("/proc/loadavg")), "processes": {}}
            for part in args.parts:
                state = private.state(part)
                if any(state[key] != initial[part][key] for key in ("MainPID", "ControlGroup")):
                    raise RuntimeError("Runtime restarted during sampling; retain the partial record")
                if game_pid(state) != game_pids[part]:
                    raise RuntimeError("Game JVM restarted during sampling")
                sample = process_sample(game_pids[part], state["ControlGroup"])
                if sample["start_ticks"] != identities[part]:
                    raise RuntimeError("PID reused during sampling; retain the partial record")
                sample["groups"] = cgroup_samples(state["ControlGroup"])
                row["processes"][part] = sample
            stream.write(json.dumps(row) + "\n")
            stream.flush()
            samples += 1
        stream.write(json.dumps({"kind": "complete", "samples": samples, "reason": reason,
                                 "unix_ns": time.time_ns(), "elapsed_seconds": time.monotonic() - start}) + "\n")
    print(output)


if __name__ == "__main__":
    main()
