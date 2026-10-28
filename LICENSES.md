# Licenses and attribution

- MeshMind application source: GPL-3.0-only, [LICENSE](LICENSE).
- PrismML llama.cpp / ggml: MIT, pinned to `5ae2e169eac202d2f065606539c3ad6ab8b51333`. Source is provisioned by `scripts/fetch-upstream.sh` and retains upstream notices.
- Official PrismML Bonsai 2 2B weights: Apache-2.0. Unmodified, with exact provenance in `model-manager/manifests/`.
- AtomicChat refusal-reduction LoRA: Apache-2.0. Unmodified, with exact provenance in `model-manager/adapters/atomicchat-2b.json`.
- Android runtime dependencies: 60 resolved components declare Apache-2.0. The component/license index and embedded notices are in `docs/notices/runtime-components.json` and `RUNTIME-LICENSES.txt`.
- Native dependencies: upstream and vendored notices, plus Android NDK LLVM/libc++ notices, are in `docs/notices/NATIVE-LICENSES.txt`. Toolchain notices may cover components beyond those linked into the APK.
- Gradle wrapper: Apache-2.0. The Apache license text is included in `docs/notices/Apache-2.0.txt`.

The full GPL, Apache and native/runtime notices are bundled as APK assets. This repository provides the application sources, pinned dependency references and build instructions corresponding to the preview APK. Source for pinned third-party dependencies remains available from their referenced upstream repositories.
