"""Check the retained FD402E1–E5 evidence without launching Minecraft."""
import gzip
import io
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def nbt(path):
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))

    def number(fmt):
        return struct.unpack(">" + fmt, stream.read(struct.calcsize(">" + fmt)))[0]

    def string():
        return stream.read(number("H")).decode("utf-8")

    def payload(kind):
        if kind in range(1, 7):
            return number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind])
        if kind == 7:
            return list(stream.read(number("i")))
        if kind == 8:
            return string()
        if kind == 9:
            child, count = number("B"), number("i")
            return [payload(child) for _ in range(count)]
        if kind == 10:
            result = {}
            while (child := number("B")) != 0:
                name = string()
                result[name] = payload(child)
            return result
        if kind in (11, 12):
            return [number("i" if kind == 11 else "q") for _ in range(number("i"))]
        raise AssertionError(kind)

    kind = number("B")
    string()
    result = payload(kind)
    assert not stream.read()
    return result["data"]


def identity(data):
    assert len(data["vehicles"]) == 1
    car = data["vehicles"][0]
    assert all(car[key] for key in ("stopping", "maintenance", "restraints"))
    assert not car["recovery_cart_removed"]
    assignments = sorted(
        (tuple(c["body"]), tuple(s["player"]), s["seat"])
        for c in car["carriers"] for s in c["rider_seats"]
    )
    assert len(assignments) == len({s[1] for s in assignments}) == 2
    assert len({s[2] for s in assignments}) == 2
    return car["chassis"], car["recovery_cart"], sorted(
        tuple(c["body"]) for c in car["carriers"]
    ), assignments


before = identity(nbt(ROOT / "before-foundation.dat"))
assert identity(nbt(ROOT / "after-foundation.dat")) == before
assert identity(nbt(ROOT / "e3-e5-final-foundation.dat")) == before


def log(name):
    return gzip.decompress((ROOT / name).read_bytes()).decode()


cold = log("e1-e2-after.log.gz")
for marker in (
    "FD_BOARDING_RESTORE mounted player=Dev gameTick=1026",
    "FD_BOARDING_RESTORE mounted player=Observer gameTick=1054",
    "There are 1 of a max of 20 players online: Observer",
    "FD_BOARDING_RESTORE mounted player=Dev gameTick=2038",
    "ThreadedAnvilChunkStorage: All dimensions are saved",
):
    assert marker in cold, marker

interruptions = log("e3-e5-server.log.gz")
for tick, fields in {
    4471: ("loadedIds=[]", "heldIds=[", "recoveryCartRemoved=false"),
    4649: ("stopping=true", "mounted=true", "heldIds=[]"),
    4857: ("stopping=false", "faults=[]"),
    5186: ("stopping=true", "faults=[Recovery equipment"),
    5371: ("stopping=true", "faults=[Recovery equipment"),
    5396: ("stopping=true", "nativeLinks=["),
    5620: ("stopping=true", "mounted=true"),
    5801: ("stopping=false", "faults=[]"),
    5953: ("stopping=true", "mounted=true", "faults=[]"),
}.items():
    lines = [line for line in interruptions.splitlines() if f"FD402_INTERRUPTION tick={tick} " in line]
    assert lines and all(field in lines[0] for field in fields), (tick, fields)
for name in ("e3-e5-dev.log.gz", "e3-e5-observer.log.gz"):
    assert "Stopping!" in log(name)
assert "ThreadedAnvilChunkStorage: All dimensions are saved" in interruptions
assert all("CarrierValidationFixture" in entry for entry in json.loads(
    (ROOT / "client-server-entry-diff.json").read_text()
))
print("PASS: original group and distinct seats retained across E1–E5; lifecycle markers verified")
