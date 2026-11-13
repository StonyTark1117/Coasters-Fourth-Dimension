#!/usr/bin/env python3
"""Own one private Minecraft run; record single XTest actions, never host input.

The run JSON is a local resource record, not roadmap status or acceptance.
See docs/FD403-RUN-PREPARATION.md. Standard library + Xvfb, xdotool, ImageMagick.
"""
import argparse
import ctypes
import ctypes.util
import datetime
import json
import os
from pathlib import Path
import re
import socket
import struct
import subprocess
import time


def run(*args, env=None):
    return subprocess.check_output(args, text=True, env=env).strip()


def unit_state(unit):
    fields = run("systemctl", "--user", "show", unit, "--property=ActiveState,MainPID,ControlGroup,WorkingDirectory")
    return dict(line.split("=", 1) for line in fields.splitlines())


def owns_group(actual, expected):
    if not expected or expected == "/":
        return False
    groups = [line.split(":", 2)[-1] for line in actual.splitlines()]
    return any(g == expected or g.startswith(expected + "/") for g in groups)


def properties(path):
    return dict(line.split("=", 1) for line in path.read_text().splitlines()
                if "=" in line and not line.startswith("#"))


def receive(sock, length):
    out = b""
    while len(out) < length:
        part = sock.recv(length - len(out))
        if not part:
            raise RuntimeError("RCON disconnected")
        out += part
    return out


def rcon(config, command):
    props = properties(Path(config["root"]) / "server/server.properties")
    if props.get("server-ip") != "127.0.0.1":
        raise RuntimeError("This helper only addresses its loopback server")
    with socket.create_connection(("127.0.0.1", int(props["rcon.port"])), timeout=30) as sock:
        def send(kind, text, request):
            packet = struct.pack("<ii", request, kind) + text.encode() + b"\0\0"
            sock.sendall(struct.pack("<i", len(packet)) + packet)
        def read():
            length, = struct.unpack("<i", receive(sock, 4))
            if not 10 <= length <= 16 * 1024 * 1024:
                raise RuntimeError("Invalid RCON response")
            packet = receive(sock, length)
            request, kind = struct.unpack("<ii", packet[:8])
            return request, kind, packet[8:-2].decode(errors="replace")
        send(3, props["rcon.password"], 1)
        request, kind, _ = read()
        if kind == 0:  # Some servers send an empty response before authentication.
            request, kind, _ = read()
        if request != 1 or kind != 2:
            raise RuntimeError("RCON authentication failed")
        send(2, command, 2)
        send(2, "", 3)  # Ordered end marker: command output may span several packets.
        chunks = []
        while True:
            request, kind, answer = read()
            if request == 3:
                return "".join(chunks)
            if request != 2:
                raise RuntimeError("Unexpected RCON request identity")
            chunks.append(answer)
            if sum(map(len, chunks)) > 1024 * 1024:
                raise RuntimeError("RCON response exceeds the local inspection limit")


class PrivateRun:
    def __init__(self, path, owner):
        self.path = Path(path).resolve()
        self.c = json.loads(self.path.read_text())
        if self.c["owner"] != owner:
            raise RuntimeError("Owner does not match this run; obtain a recorded handoff")
        self.root = Path(self.c["root"]).resolve()
        if not self.path.is_relative_to(self.root) or self.root.is_relative_to("/tmp"):
            raise RuntimeError("Keep the run record and world on persistent task storage")
        self.display = ":" + str(int(self.c["display"]))
        if int(self.c["display"]) < 100:
            raise RuntimeError("Refusing a desktop display")
        if not re.fullmatch(r"fd[0-9a-z-]+", self.c["unit_prefix"]):
            raise RuntimeError("Invalid task unit prefix")
        self.env = dict(os.environ, DISPLAY=self.display)
        self.env.pop("WAYLAND_DISPLAY", None)
        self.evidence = self.root / "evidence"
        self.evidence.mkdir(exist_ok=True)

    def unit(self, part):
        return self.c["unit_prefix"] + "-" + part + ".service"

    def state(self, part):
        state = unit_state(self.unit(part))
        if state["ActiveState"] != "active" or int(state["MainPID"]) <= 0:
            raise RuntimeError(part + " is not active: " + str(state))
        pid = state["MainPID"]
        actual_group = Path("/proc", pid, "cgroup").read_text()
        if not owns_group(actual_group, state["ControlGroup"]) or state["WorkingDirectory"] != str(self.root):
            raise RuntimeError("Runtime ownership does not match the run record")
        if part == "xvfb":
            cmd = Path("/proc", pid, "cmdline").read_bytes().split(b"\0")
            if self.display.encode() not in cmd or b"Xvfb" not in Path(os.fsdecode(cmd[0])).name.encode():
                raise RuntimeError("Display PID is not the owned Xvfb")
        return state

    def options(self):
        data = dict(line.split(":", 1) for line in (self.root / "client/options.txt").read_text().splitlines() if ":" in line)
        required = {"key_key.use": "key.mouse.right", "key_key.attack": "key.mouse.left", "rawMouseInput": "true"}
        if any(data.get(k) != v for k, v in required.items()):
            raise RuntimeError("Client options differ from the retained input baseline")
        return {k: data[k] for k in required}

    def window(self):
        self.state("xvfb")
        client = self.state("client")
        windows = run("xdotool", "search", "--onlyvisible", "--class", "Minecraft", env=self.env).splitlines()
        owned = []
        for window in windows:
            pid = run("xdotool", "getwindowpid", window, env=self.env)
            if owns_group(Path("/proc", pid, "cgroup").read_text(), client["ControlGroup"]):
                owned.append(window)
        if len(owned) != 1:
            raise RuntimeError("Expected one Minecraft window in the owned client unit")
        return owned[0]

    def connected(self):
        self.state("server")
        answer = rcon(self.c, "list")
        players = answer.partition(":")[2].strip().split(", ")
        if self.c["player"] not in players:
            raise RuntimeError("Player is not connected: " + answer)
        return answer

    def snapshot(self, name):
        self.state("xvfb")
        target = self.evidence / (name + ".png")
        subprocess.run(["import", "-display", self.display, "-window", "root", str(target)], check=True, env=self.env)
        return str(target)

    def status(self):
        window = self.window()
        return {"owner": self.c["owner"], "display": self.display, "window": window,
                "geometry": run("xdotool", "getwindowgeometry", "--shell", window, env=self.env),
                "options": self.options(), "connection": self.connected(),
                "client": self.state("client"), "server": self.state("server")}

    def start(self, part):
        unit = self.unit(part)
        if unit_state(unit).get("ActiveState") in ("active", "activating", "deactivating"):
            raise RuntimeError("Unit already running; inspect it instead of restarting")
        env = ["env", "-u", "WAYLAND_DISPLAY", "JAVA_HOME=/usr/lib/jvm/java-21-openjdk",
               "PATH=/usr/lib/jvm/java-21-openjdk/bin:" + os.environ["PATH"], "ALSOFT_DRIVERS=null"]
        if part == "xvfb":
            if Path("/tmp/.X11-unix/X" + str(self.c["display"])).exists():
                raise RuntimeError("Display is already reserved")
            command = ["Xvfb", self.display, "-screen", "0", "854x480x24", "-nolisten", "tcp", "-ac"]
            limit = "256M"
        else:
            script = Path(self.c[part + "_script"])
            text = script.read_text()
            if "/usr/lib/jvm/java-21-openjdk/bin/java" not in text or "gradlew" in text:
                raise RuntimeError("Use a generated direct Java 21 launch script")
            if str(self.root / part) not in text:
                raise RuntimeError("Launch script uses another task's game directory")
            if part == "client":
                self.state("xvfb")
                self.state("server")
                self.options()
                # ModDev's generated shell script does not inherit JavaExec.jvmArgs.
                # Set the heap on the direct Java process too; verify with VM.flags.
                env += ["DISPLAY=" + self.display, "JAVA_TOOL_OPTIONS=-Xmx1536M"]
                limit = self.c.get("client_limit", "6G")
            else:
                env += ["JAVA_TOOL_OPTIONS=-Xmx2048M"]
                limit = self.c.get("server_limit", "4G")
            command = env + ["sh", str(script)]
        subprocess.run(["systemd-run", "--user", "--unit=" + unit, "--property=MemoryMax=" + limit,
                        "--property=WorkingDirectory=" + str(self.root),
                        "--property=StandardOutput=append:" + str(self.evidence / (part + ".log")),
                        "--property=StandardError=inherit", "--", *command], check=True)
        return {"started": unit, "memory_limit": limit}

    def action(self, args):
        window = self.window()
        self.options()
        connection = "menu action; no gameplay claim" if args.menu else self.connected()
        stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%S.%fZ")
        record = {"at": stamp, "action": args.action, "values": args.values,
                  "expected": args.expect, "connection": connection, "window": window,
                  "geometry": run("xdotool", "getwindowgeometry", "--shell", window, env=self.env),
                  "before": self.snapshot(stamp + "-before")}
        record["modifier"] = args.modifier
        record["state"] = "sending"
        self.log_input(record)
        try:
            if args.modifier:
                subprocess.run(["xdotool", "keydown", "--window", window, args.modifier],
                               env=self.env, check=True)
                time.sleep(.18)
            self._input(window, args, record)
            time.sleep(.35)
            record["after"] = self.snapshot(stamp + "-after")
            record["state"] = "sent; inspect readback"
        except Exception as error:
            record.update(state="uncertain", error=str(error))
            raise
        finally:
            try:
                if args.modifier:
                    subprocess.run(["xdotool", "keyup", "--window", window, args.modifier],
                                   env=self.env, check=True)
            finally:
                self.log_input(record)
        return record

    def log_input(self, record):
        with (self.evidence / "input.jsonl").open("a") as f:
            f.write(json.dumps(record) + "\n")

    def _input(self, window, args, record):
        subprocess.run(["xdotool", "windowfocus", "--sync", window], check=True, env=self.env)
        if args.action in ("move", "turn", "click"):
            x = ctypes.CDLL(ctypes.util.find_library("X11"))
            t = ctypes.CDLL(ctypes.util.find_library("Xtst"))
            x.XOpenDisplay.argtypes = [ctypes.c_char_p]
            x.XOpenDisplay.restype = ctypes.c_void_p
            x.XSync.argtypes = [ctypes.c_void_p, ctypes.c_int]
            x.XCloseDisplay.argtypes = [ctypes.c_void_p]
            d = x.XOpenDisplay(self.display.encode())
            if not d:
                raise RuntimeError("Cannot open owned Xvfb")
            try:
                if args.action == "turn":
                    # XI device events reach GLFW's raw-input path. Enumerate the
                    # private server's XTEST pointer; device IDs vary by display.
                    class DeviceInfo(ctypes.Structure):
                        _fields_ = [("id", ctypes.c_ulong), ("type", ctypes.c_ulong),
                                    ("name", ctypes.c_char_p), ("num_classes", ctypes.c_int),
                                    ("use", ctypes.c_int), ("inputclassinfo", ctypes.c_void_p)]
                    xi = ctypes.CDLL(ctypes.util.find_library("Xi"))
                    xi.XListInputDevices.argtypes = [ctypes.c_void_p, ctypes.POINTER(ctypes.c_int)]
                    xi.XListInputDevices.restype = ctypes.POINTER(DeviceInfo)
                    xi.XFreeDeviceList.argtypes = [ctypes.POINTER(DeviceInfo)]
                    xi.XOpenDevice.argtypes = [ctypes.c_void_p, ctypes.c_ulong]
                    xi.XOpenDevice.restype = ctypes.c_void_p
                    xi.XCloseDevice.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
                    count = ctypes.c_int()
                    devices = xi.XListInputDevices(d, ctypes.byref(count))
                    try:
                        ids = [devices[i].id for i in range(count.value)
                               if devices[i].name == b"Virtual core XTEST pointer"]
                    finally:
                        xi.XFreeDeviceList(devices)
                    if len(ids) != 1:
                        raise RuntimeError("Owned display has no unique XTEST pointer")
                    device = xi.XOpenDevice(d, ids[0])
                    if not device:
                        raise RuntimeError("Cannot open private XTEST pointer")
                    try:
                        dx, dy = map(int, args.values)
                        axes = (ctypes.c_int * 2)(dx, dy)
                        t.XTestFakeDeviceMotionEvent.argtypes = [ctypes.c_void_p, ctypes.c_void_p,
                            ctypes.c_int, ctypes.c_int, ctypes.POINTER(ctypes.c_int), ctypes.c_int, ctypes.c_ulong]
                        t.XTestFakeDeviceMotionEvent(d, device, 1, 0, axes, 2, 0)
                        record["device"] = ids[0]
                    finally:
                        xi.XCloseDevice(d, device)
                elif args.action == "move":
                    px, py = map(int, args.values)
                    t.XTestFakeMotionEvent.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_ulong]
                    t.XTestFakeMotionEvent(d, -1, px, py, 0)
                else:
                    button, = map(int, args.values)
                    if button not in (1, 3):
                        raise RuntimeError("Use left (1) or right (3) button")
                    # Do not warp the pointer before clicking: that changes game aim.
                    t.XTestFakeButtonEvent.argtypes = [ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong]
                    t.XTestFakeButtonEvent(d, button, 1, 0)
                    x.XSync(d, 0)
                    time.sleep(.08)
                    t.XTestFakeButtonEvent(d, button, 0, 0)
                x.XSync(d, 0)
            finally:
                x.XCloseDisplay(d)
        elif args.action == "key":
            # A 12 ms default tap can fall entirely between Minecraft input polls.
            # Keep a normal press visible for several frames, always releasing it.
            for key in args.values:
                subprocess.run(["xdotool", "keydown", "--window", window, key], env=self.env, check=True)
                try:
                    time.sleep(.18)
                finally:
                    subprocess.run(["xdotool", "keyup", "--window", window, key], env=self.env, check=True)
        elif args.action == "type":
            subprocess.run(["xdotool", "type", "--window", window, "--clearmodifiers", "--", " ".join(args.values)], env=self.env, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("run", help="Absolute path to task-owned run.json")
    parser.add_argument("--owner", required=True, help="Thread ID recorded in run.json")
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("status")
    sub.add_parser("snapshot")
    start = sub.add_parser("start")
    start.add_argument("part", choices=("server", "xvfb", "client"))
    console = sub.add_parser("console", help="Owned server command; setup mutations earn no player acceptance")
    console.add_argument("text")
    action = sub.add_parser("input")
    action.add_argument("action", choices=("move", "turn", "click", "key", "type"))
    action.add_argument("values", nargs="+")
    action.add_argument("--modifier", choices=("Shift_L", "Control_L"), help="Hold this key during one action; always release afterward")
    action.add_argument("--expect", required=True, help="Expected visible or authoritative result; inspect it before next action")
    action.add_argument("--menu", action="store_true", help="Explicit menu/connection action, not an in-world interaction")
    args = parser.parse_args()
    private = PrivateRun(args.run, args.owner)
    if args.command == "start":
        result = private.start(args.part)
    elif args.command == "status":
        result = private.status()
    elif args.command == "snapshot":
        result = private.snapshot(datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%S.%fZ") + "-state")
    elif args.command == "console":
        private.state("server")
        record = {"at": time.time(), "command": args.text, "state": "sending"}
        with (private.evidence / "console.jsonl").open("a") as f:
            f.write(json.dumps(record) + "\n")
        try:
            result = rcon(private.c, args.text)
            record.update(state="responded", result=result)
        except (OSError, RuntimeError) as error:
            record.update(state="uncertain", error=str(error))
            raise
        finally:
            with (private.evidence / "console.jsonl").open("a") as f:
                f.write(json.dumps(record) + "\n")
    else:
        result = private.action(args)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
