#!/usr/bin/env python3
"""Provision the pinned research adapter on the build host, never in the APK."""
import argparse
import hashlib
import json
import pathlib
import urllib.request

parser = argparse.ArgumentParser()
parser.add_argument("destination", type=pathlib.Path)
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parent.parent
spec = json.loads((root / "model-manager/adapters/atomicchat-2b.json").read_text())
url = f"https://huggingface.co/{spec['repository']}/resolve/{spec['revision']}/{spec['filename']}"
args.destination.parent.mkdir(parents=True, exist_ok=True)
partial = args.destination.with_suffix(".partial")
digest = hashlib.sha256()
count = 0
try:
    with urllib.request.urlopen(url, timeout=60) as source, partial.open("wb") as output:
        while chunk := source.read(65536):
            count += len(chunk)
            if count > spec["size_bytes"]:
                raise ValueError("Adapter exceeds expected size")
            digest.update(chunk)
            output.write(chunk)
    if count != spec["size_bytes"] or digest.hexdigest() != spec["sha256"]:
        raise ValueError("Adapter checksum/size mismatch")
    partial.replace(args.destination)
finally:
    partial.unlink(missing_ok=True)
print(f"Verified adapter: {count} bytes, SHA-256 {digest.hexdigest()}")
