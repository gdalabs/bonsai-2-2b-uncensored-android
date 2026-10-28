# Build

Use JDK 17, Python 3 and an Android SDK with platform 36, build tools 36.0.0, NDK 28.2.13676358 and CMake 3.31.6. Gradle 8.14.3, Android Gradle Plugin 8.13.2 and Kotlin 2.2.10 are pinned. Gradle dependency locks and strict checksum verification remain enabled.

```sh
export ANDROID_HOME=/path/to/android-sdk
export PATH="$ANDROID_HOME/cmake/3.31.6/bin:$PATH"
scripts/fetch-upstream.sh
python3 scripts/fetch-model.py PTQ1_0 artifacts/bonsai-PTQ1_0.gguf
python3 scripts/fetch-adapter.py artifacts/uncensored-adapter.gguf
./gradlew --no-daemon -PmeshmindUncensored=true \
  -PmeshmindBundledModel=../artifacts/bonsai-PTQ1_0.gguf \
  -PmeshmindBundledFormat=PTQ1_0 \
  -PmeshmindBundledAdapter=../artifacts/uncensored-adapter.gguf \
  :android-app:assembleDebug :android-app:assembleDebugAndroidTest \
  :android-app:testDebugUnitTest :android-app:lintDebug
python3 scripts/check-apk.py --bundled --uncensored \
  android-app/build/outputs/apk/debug/android-app-debug.apk
```

Gradle file properties are relative to `android-app`. The downloaded base and adapter must match the pinned sizes and SHA-256 values. Network access is used only to provision build dependencies and weights on the developer machine. The application has no Internet permission.

Debug APKs are installable test artifacts. Release builds are unsigned until a distributor supplies a signing process. Do not commit signing material. The build uses pinned source and toolchain versions; byte-identical APK reproducibility has not been established.

## Host integration

```sh
cmake -S inference -B build/runtime-host -DCMAKE_BUILD_TYPE=Release \
  -DMESHMIND_RUNTIME_SANITIZERS=ON
cmake --build build/runtime-host --parallel 2
ASAN_OPTIONS=detect_leaks=1 UBSAN_OPTIONS=halt_on_error=1 \
  build/runtime-host/meshmind-adapter-test \
  artifacts/bonsai-PTQ1_0.gguf artifacts/uncensored-adapter.gguf
```

The host adapter test runs actual inference and checks arithmetic with the adapter on/off, deterministic restoration, cancellation, missing/duplicate adapters and unload/reload. Sanitizers instrument the application runtime adapter; upstream libraries are uninstrumented.

## Phone test

Install the app and matching test APK on a consenting test phone, then run the inference test:

```sh
adb install -r android-app/build/outputs/apk/debug/android-app-debug.apk
adb install -r android-app/build/outputs/apk/androidTest/debug/android-app-debug-androidTest.apk
adb shell am instrument -w -e runModelTests true \
  -e class org.meshmind.app.RefusalAdapterDeviceTest \
  org.meshmind.app.uncensored.test/androidx.test.runner.AndroidJUnitRunner
```

The adapter test provisions the bundled model, verifies it and runs the Kotlin/JNI generation path. Running the entire instrumentation suite also includes a history roundtrip/deletion test; use an empty test installation for that suite. Cross-compilation and host output are separate from actual phone execution.
