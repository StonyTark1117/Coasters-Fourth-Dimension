"""Check FD402E6/E7 raw identities, client mounts and final save without a run."""
import gzip
import io
import json
import struct
import re
import uuid
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


def log(name):
    return gzip.decompress((ROOT / name).read_bytes()).decode()


def field(line, name):
    return line.split(name + "=[", 1)[1].split("]", 1)[0]


def uuid_text(parts):
    return str(uuid.UUID(bytes=b"".join((n & 0xffffffff).to_bytes(4, "big") for n in parts)))


e6 = log("e6-server.log.gz")
rows = {int(re.search(r"tick=(\d+)", line)[1]): line for line in e6.splitlines()
        if "FD402_INTERRUPTION tick=" in line}
assert "unlinked 4e52e45a-2a47-4349-b1e8-2756b46794fb <-> 0f627947-e096-4cf5-9043-f41e71a7b796" in e6
for key in ("riderSeats", "loadedIds"):
    assert field(rows[1410], key) == field(rows[1883], key)
assert len(field(rows[1883], "loadedIds").split(", ")) == 13
assert "riders=2 mounted=true stopping=true maintenance=true recoveryCartRemoved=false" in rows[1883]
assert "unavailable" in rows[1883]
assert "0f627947-e096-4cf5-9043-f41e71a7b796" not in field(rows[1883], "nativeLinks")
assert "started occupied recovery" not in e6.split("tick=1410", 1)[1].split("tick=1883", 1)[0]

e7 = log("e7-server-final.log.gz")
assert "permanently removed recovery bearing" in e7
assert "started occupied recovery" not in e7
assert "ThreadedAnvilChunkStorage: All dimensions are saved" in e7
removed = next(line for line in e7.splitlines() if "FD402_INTERRUPTION tick=883 " in line)
assert "riders=2 mounted=true stopping=true maintenance=true recoveryCartRemoved=true" in removed
assert len(field(removed, "nativeLinks").split(", ")) == 4
assert len(field(removed, "loadedIds").split(", ")) == 12
for mod in ("coasterssimulatedextratypes", "coastersengineered", "createaddition"):
    assert mod in e7

cars = nbt(ROOT / "e7-final-foundation.dat")["vehicles"]
assert len(cars) == 4
assert all(all(v[k] for k in ("stopping", "maintenance", "restraints", "recovery_cart_removed")) for v in cars)
assert {uuid_text(v["recovery_cart"]) for v in cars} == {"0a7b8957-62ca-42c9-9eee-1492e0d56372"}
ids = {uuid_text(v["chassis"]) for v in cars} | {uuid_text(c["body"]) for v in cars for c in v["carriers"]}
assert ids == set(field(removed, "loadedIds").split(", "))
seats = [(uuid_text(c["body"]), uuid_text(s["player"]), s["seat"])
         for v in cars for c in v["carriers"] for s in c["rider_seats"]]
assert len(seats) == 2 and len({s[1] for s in seats}) == 2 and len({s[2] for s in seats}) == 2
assert {s[0] for s in seats} == {"2b47c92d-b1f9-4f6d-af07-3e5a4fb35b19"}
for case, carrier, start, end in (
    ("e6", "4728d0c6-dca8-4b33-aa5c-5d8089ddae18", "08:28:58", "08:29:22"),
    ("e7", "2b47c92d-b1f9-4f6d-af07-3e5a4fb35b19", "08:39:17", "08:40:00"),
):
    for role, player in (("dev", "Dev"), ("observer", "Observer")):
        text = log(f"{case}-{role}-accepted.log.gz")
        assert carrier in text
        assert any(f"FD_CLIENT player={player}" in line and "riding=true" in line
                   and start <= line[11:19] <= end for line in text.splitlines()), (case, role)
print("PASS: E6 deleted edge/retained IDs; E7 durable removal/final 12 bodies/two seats; both live client mounts and combined profile verified")
