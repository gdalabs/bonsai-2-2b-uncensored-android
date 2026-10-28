# MeshMind Uncensored for Android

Experimental offline Android chat with PrismML's **Bonsai 2 2B** and AtomicChat's optional refusal-reduction LoRA. The official model weights remain unchanged. The app runs inference directly through JNI on the CPU and requests no Internet permission.

[Download the preview APK](https://github.com/gdalabs/meshmind-uncensored-android/releases/tag/v0.1.1-uncensored-preview) · [Build from source](BUILD.md) · [Validation](VALIDATION.md)

## Try it

1. Install the model-bundled **ARM64** APK on Android 9 or newer.
2. Open **Settings**, wait for bundled-model verification, then select **Load**.
3. Chat in the **AI** tab. In Settings, switch **Experimental refusal reduction** on or off. Use **Reset** before comparing the same prompt in both modes.

The preview uses `org.meshmind.app.uncensored`, so it can be installed alongside the original MeshMind and Bonsai Local apps. APK and private model copy both occupy storage. This is a debug-signed test build; production signing and real-device validation of this preview are pending.

## Models

| Component | Pinned source | Bytes |
| --- | --- | ---: |
| Base | [PrismML Bonsai 2 2B PTQ1_0](https://huggingface.co/prism-ml/Ternary-Bonsai-2-2B-gguf/tree/683f7be2629342a50987627b40881e54761a2430) | 429,183,456 |
| Adapter | [AtomicChat 2B refusal-reduction LoRA](https://huggingface.co/AtomicChat/Ternary-Bonsai-2-2B-Uncensored-LoRA-GGUF/tree/3ddae94b35c44b7a07e927aea82618ce7d447eef) | 1,187,136 |

Checksums and exact-base binding are enforced during packaging and loading. The adapter is applied at inference time at strength 1; disabling it removes the adapter from the context. The ternary base is never requantized or overwritten. Only the exact pinned PTQ1_0 base is supported in this preview. Model provisioning occurs on the build host or by local file transfer; the app never downloads weights.

## Scope and evidence

- Text chat, streaming, cancellation, context reset and encrypted local chat history.
- No network permission, telemetry, inference server or action-execution tools. Backgrounding stops generation.
- Bluetooth mesh messaging is an unfinished future component; the Mesh tab explicitly reports that it is unavailable.
- JVM tests and real host inference passed. This new adapter APK still needs an actual phone test. Reduced refusals do not establish correctness or broad capability retention. Effectiveness outside the publisher's evaluated English prompts is unmeasured.

The `Uncensored` label refers to the experimental refusal-reduction adapter, and does not promise that every request will receive an answer. See the [publisher's evaluation and limitations](https://huggingface.co/AtomicChat/Ternary-Bonsai-2-2B-Uncensored-LoRA-GGUF).

## License

Application source: **GPL-3.0-only**. Official model and AtomicChat adapter: **Apache-2.0**. Runtime libraries retain their own licenses. Full texts, copyright notices and a runtime component index are included under [docs/notices](docs/notices) and in the APK. See [LICENSES.md](LICENSES.md).
