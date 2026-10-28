# Model compatibility

| Format | Exact official bytes | Official SHA-256 | Backend status |
| --- | ---: | --- | --- |
| Q2_0 | 546689504 | c2893fa6f951daee8b7263d5f49b455e57989ef56417a22125b03566dc560650 | Initial validation candidate |
| PQ2_0 | 517312992 | d5f01a4ecaf49239653308ee732ad1a0c5f802c0ddb997549fc7b694c8b45928 | Fork CPU implementation present |
| PTQ1_0 | 429183456 | 36061ad406df8a4903a6db005be350c50fabfaf717f9ef0acf4b76adcd9929fa | Fork CPU implementation present |

Model revision: `683f7be2629342a50987627b40881e54761a2430`. Runtime: `5ae2e169eac202d2f065606539c3ad6ab8b51333`. See PROGRESS.md for executed tests; kernel availability alone does not establish phone support.

Only qwen35 with the supported ternary tensor types and floating-point auxiliary tensors is accepted. Legacy Q1_0, arbitrary Q2_K, MLX, vision mmproj, adapters, model directories and unrelated GGUF architectures are not supported. Vision input is outside Phase 1.

A future abliterated derivative needs its own checksum/size manifest and must preserve compatible operators, Hadamard basis, tokenizer, special tokens and embedded chat template. A shared base-model name does not prove compatibility. A LoRA adapter is not a replacement full GGUF and is not accepted through model import. The separate experimental uncensored build bundles one pinned AtomicChat adapter with the exact official PTQ1_0 base; see BUILD.md and `model-manager/adapters/atomicchat-2b.json`. Arbitrary adapters and their operation on other quantizations remain unsupported. Signatures are reserved manifest metadata; this version does not verify publisher signatures. Custom manifests are user-trusted integrity statements.
