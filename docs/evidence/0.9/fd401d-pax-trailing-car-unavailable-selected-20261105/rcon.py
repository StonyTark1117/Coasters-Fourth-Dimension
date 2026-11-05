import socket
import struct
import sys

host, port, password = "127.0.0.1", 25723, "fd401d-pax-trailing-car-local"
command = " ".join(sys.argv[1:])
with socket.create_connection((host, port), timeout=10) as sock:
    def send(packet_id, packet_type, body):
        payload = struct.pack("<ii", packet_id, packet_type) + body.encode() + b"\0\0"
        sock.sendall(struct.pack("<i", len(payload)) + payload)

    def receive():
        header = sock.recv(4)
        if len(header) != 4:
            raise RuntimeError("short RCON header")
        size = struct.unpack("<i", header)[0]
        payload = bytearray()
        while len(payload) < size:
            part = sock.recv(size - len(payload))
            if not part:
                raise RuntimeError("short RCON body")
            payload.extend(part)
        packet_id, packet_type = struct.unpack("<ii", payload[:8])
        return packet_id, packet_type, bytes(payload[8:-2]).decode(errors="replace")

    send(1, 3, password)
    auth = receive()
    if auth[0] == -1:
        raise RuntimeError("RCON authentication failed")
    send(2, 2, command)
    print(receive()[2])
