# Preview validation

- JVM: 22 tests, zero failures/errors/skips.
- ARM64 debug and instrumentation APKs compile. Lint: zero errors, 10 warnings.
- Actual host PTQ1_0 inference with the pinned adapter: arithmetic at scale 0/1, deterministic restoration, cancellation, missing/duplicate adapter handling and unload/reload passed.
- Existing Q2_0 runtime integration: model format/special tokens, repeat/reset, cancellation, invalid settings, context overflow and unload passed.
- Both host integration suites passed ASan/UBSan and leak checking. Upstream libraries were uninstrumented; this is not a full native memory-safety audit.
- Final APK: no Internet permission, ARM64 ABI, 16 KiB ELF/ZIP alignment, model hash/size, adapter hash/size and debug signature checks passed. Packaged license assets match the source distribution. Exact sizes and SHA-256 values are in [VALIDATION.json](VALIDATION.json) and the release's SHA256SUMS.

Actual execution of this new adapter APK on Android is pending. No mobile performance numbers, broad quality-retention score or general refusal-removal guarantee is claimed. The publisher's benchmark results have not been independently replicated here.

The Android application and native runtime sources match the artifact build. The host CMake project contains only the benchmark and integration tests; Android build targets and flags are unchanged.
