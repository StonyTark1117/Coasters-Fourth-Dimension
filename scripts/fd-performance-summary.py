#!/usr/bin/env python3
"""Summarize FD502 JSONL samples; missing sources stay explicit, never pass a gate.

JVM event contract: kind=frame|tick|heap|gc, unix_ns, process=client|server;
frame/tick/gc carry duration_ns; heap carries used_bytes; gc carries after_bytes
(post-GC heap only); separate kind=post_gc uses after_bytes. Resource input is fd-resource-sample.py JSONL.
"""
import argparse
import json
import math
from pathlib import Path
import statistics


def distribution(values):
    if not values:
        return None
    ordered = sorted(values)
    return {"count": len(values), "median": statistics.median(ordered),
            "p95": ordered[math.ceil(.95 * len(ordered)) - 1],
            "p99": ordered[math.ceil(.99 * len(ordered)) - 1], "max": ordered[-1],
            "percentile_method": "nearest-rank"}


def summarize(rows, start_ns=None, end_ns=None):
    timed = [r for r in rows if "unix_ns" in r and
             (start_ns is None or r["unix_ns"] >= start_ns) and
             (end_ns is None or r["unix_ns"] < end_ns)]
    out = {"collector_errors": [r for r in rows if r.get("kind") == "collector_error"], "scope": "measurement summary only; no workload, GPU or acceptance certification",
           "window": {"start_unix_ns": start_ns, "end_unix_ns": end_ns},
           "sources": {}, "missing": []}
    for kind in ("frame", "tick", "gc"):
        processes = sorted({r.get("process", "unspecified") for r in timed if r.get("kind") == kind})
        if not processes:
            out["missing"].append(kind)
        for process in processes:
            events = [r for r in timed if r.get("kind") == kind and r.get("process", "unspecified") == process]
            values = [r["duration_ns"] / 1e6 for r in events]
            if any(not math.isfinite(v) or v < 0 for v in values):
                raise ValueError("Invalid duration; preserve and diagnose raw collector data")
            stats = distribution(values)
            stats["units"] = "ms"
            if kind in ("frame", "tick"):
                stats["over_50_ms"] = sum(v > 50 for v in values)
            if kind == "frame":
                stats["over_100_ms"] = sum(v > 100 for v in values)
                stats["measurement"] = "CPU frame-start intervals; not GPU presentation"
            if kind == "gc":
                stats["post_gc_heap_bytes"] = distribution([r["after_bytes"] for r in events if "after_bytes" in r])
            out["sources"][kind + ":" + process] = stats
    post_gc = [r for r in timed if r.get("kind") == "post_gc"]
    if not post_gc and not any(r.get("kind") == "gc" and "after_bytes" in r for r in timed):
        out["missing"].append("post_gc")
    for process in sorted({r.get("process", "unspecified") for r in post_gc}):
        out["sources"]["post_gc:" + process] = distribution([r["after_bytes"] for r in post_gc if r.get("process", "unspecified") == process])
    heaps = [r for r in timed if r.get("kind") == "heap"]
    if not heaps:
        out["missing"].append("heap")
    for process in sorted({r.get("process", "unspecified") for r in heaps}):
        out["sources"]["heap:" + process] = distribution([r["used_bytes"] for r in heaps if r.get("process", "unspecified") == process])
    resources = [r for r in timed if r.get("kind") == "resource"]
    if not resources:
        out["missing"].append("resource")
    for process in sorted({p for r in resources for p in r["processes"]}):
        values = [r["processes"][process] for r in resources if process in r["processes"]]
        out["sources"]["resource:" + process] = {
            "rss_bytes": distribution([v["rss_kib"] * 1024 for v in values]),
            "cgroup_bytes": distribution([int(v["groups"][0]["memory.current"]) for v in values
                                          if v["groups"][0]["memory.current"] is not None]),
            "lifetime_cgroup_peak_bytes": max((int(v["groups"][0]["memory.peak"]) for v in values
                                               if v["groups"][0]["memory.peak"] is not None), default=None)}
    # Final three five-minute bins are descriptive; no automated 'no leak' claim.
    if timed and end_ns is not None:
        bins = []
        for index in range(3):
            lower = end_ns - (3 - index) * 300_000_000_000
            upper = lower + 300_000_000_000
            segment = [r for r in timed if lower <= r["unix_ns"] < upper]
            processes = sorted({p for r in segment if r.get("kind") == "resource" for p in r["processes"]} |
                               {r.get("process", "unspecified") for r in segment if r.get("kind") in ("gc", "post_gc")})
            bins.append({"start_unix_ns": lower, "end_unix_ns": upper, "processes": {
                p: {"rss_bytes": distribution([r["processes"][p]["rss_kib"] * 1024 for r in segment
                                               if r.get("kind") == "resource" and p in r["processes"]]),
                    "post_gc_heap_bytes": distribution([r["after_bytes"] for r in segment
                        if r.get("kind") in ("gc", "post_gc") and r.get("process", "unspecified") == p and "after_bytes" in r])}
                for p in processes}})
        out["final_three_five_minute_periods"] = bins
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("inputs", nargs="+", type=Path)
    parser.add_argument("--start-unix-ns", type=int)
    parser.add_argument("--end-unix-ns", type=int)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    if args.start_unix_ns is not None and args.end_unix_ns is not None and args.start_unix_ns >= args.end_unix_ns:
        parser.error("Sample window must end after it starts")
    rows = []
    inputs = []
    for path in args.inputs:
        batch = [json.loads(line) for line in path.read_text().splitlines() if line.strip()]
        rows.extend(batch)
        inputs.append({"path": str(path.resolve()), "rows": len(batch),
                       "completed_footer": bool(batch and batch[-1].get("kind") == "complete"),
                       "dropped_events": batch[-1].get("dropped_events") if batch else None})
    result = summarize(rows, args.start_unix_ns, args.end_unix_ns)
    result["inputs"] = inputs
    args.output.write_text(json.dumps(result, indent=2) + "\n")


if __name__ == "__main__":
    main()
