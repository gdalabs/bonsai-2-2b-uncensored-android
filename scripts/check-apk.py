#!/usr/bin/env python3
"""Check the packaged permission set and native ABI, not just source manifests."""
import argparse
import os
import pathlib
import re
import subprocess
import struct
import zipfile
import json
import hashlib

parser = argparse.ArgumentParser()
parser.add_argument("apk", type=pathlib.Path)
parser.add_argument("--bundled", action="store_true", help="Require and verify an official bundled model")
parser.add_argument("--uncensored", action="store_true", help="Require the separate package and pinned refusal adapter")
args = parser.parse_args()
sdk = pathlib.Path(os.environ["ANDROID_HOME"])
aapt = sdk / "build-tools/36.0.0/aapt2"
permissions = subprocess.check_output([str(aapt), "dump", "permissions", str(args.apk)], text=True)
assert "android.permission.INTERNET" not in permissions, permissions
if args.uncensored:
    assert args.bundled, "Uncensored preview must bundle its exact base model"
    badging = subprocess.check_output([str(aapt), "dump", "badging", str(args.apk)], text=True)
    assert "package: name='org.meshmind.app.uncensored'" in badging, badging
with zipfile.ZipFile(args.apk) as apk:
    libs = [n for n in apk.namelist() if n.startswith("lib/") and n.endswith(".so")]
    assert "lib/arm64-v8a/libmeshmind.so" in libs, libs
    assert all(n.startswith("lib/arm64-v8a/") for n in libs), libs
    models = [n for n in apk.namelist() if n.endswith(".gguf")]
    if args.bundled:
        expected = {"assets/bundled-model.gguf"}
        if args.uncensored:
            expected.add("assets/refusal-adapter.gguf")
        assert set(models) == expected, models
        metadata = json.loads(apk.read("assets/bundled-model.json"))
        official = json.loads((pathlib.Path(__file__).resolve().parents[1] / "model-manager" / "manifests" / ("bonsai-" + metadata["gguf_format"] + ".json")).read_text())
        assert metadata == official, "Bundled manifest differs from pinned official manifest"
        assert apk.getinfo("assets/bundled-model.gguf").file_size == metadata["size_bytes"]
        digest = hashlib.sha256()
        with apk.open("assets/bundled-model.gguf") as source:
            while chunk := source.read(1024 * 1024):
                digest.update(chunk)
        assert digest.hexdigest() == metadata["sha256"], "Bundled GGUF checksum mismatch"
        if args.uncensored:
            adapter = json.loads(apk.read("assets/refusal-adapter.json"))
            pinned = json.loads((pathlib.Path(__file__).resolve().parents[1] / "model-manager/adapters/atomicchat-2b.json").read_text())
            assert adapter == pinned and adapter["base_sha256"] == metadata["sha256"]
            data = apk.read("assets/refusal-adapter.gguf")
            assert len(data) == adapter["size_bytes"] and hashlib.sha256(data).hexdigest() == adapter["sha256"]
    else:
        assert not models, models
    for name in libs:
        elf = apk.read(name)
        assert elf[:4] == b"\x7fELF" and int.from_bytes(elf[18:20], "little") == 183, name
        offset = struct.unpack_from("<Q", elf, 32)[0]
        entry_size, count = struct.unpack_from("<HH", elf, 54)
        for index in range(count):
            header = offset + index * entry_size
            if struct.unpack_from("<I", elf, header)[0] == 1:
                assert struct.unpack_from("<Q", elf, header + 48)[0] >= 16384, name
subprocess.run([str(sdk / "build-tools/36.0.0/zipalign"), "-c", "-P", "16", "4", str(args.apk)], check=True)
print("PASS: no INTERNET permission; ARM64 ELF; 16 KiB ELF/ZIP alignment; " + ("verified bundled model" if args.bundled else "no bundled model"))
