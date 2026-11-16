#!/usr/bin/env python3
"""Use the proven private-display helper with a separately owned observer client."""
import importlib.util
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "private_client", Path(__file__).with_name("fd-private-client.py"))
private = importlib.util.module_from_spec(spec)
spec.loader.exec_module(private)
BaseRun = private.PrivateRun


class SharedServerRun(BaseRun):
    def __init__(self, path, owner):
        super().__init__(path, owner)
        parent = self.c.get("server_run")
        self.shared_server = BaseRun(parent, owner) if parent else None

    def state(self, part):
        if part == "server" and self.shared_server:
            return self.shared_server.state(part)
        return super().state(part)

    def start(self, part):
        if part == "server" and self.shared_server:
            raise RuntimeError("Start the shared server using its primary owner record")
        return super().start(part)


private.PrivateRun = SharedServerRun
if __name__ == "__main__":
    private.main()
