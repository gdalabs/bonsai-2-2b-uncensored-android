package org.meshmind.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RefusalAdapterDeviceTest {
    @Test fun actualAdapterInferenceCanToggleAndCancel() {
        assumeTrue(BuildConfig.REFUSAL_ADAPTER)
        assumeTrue(InstrumentationRegistry.getArguments().getString("runModelTests") == "true")
        val repository = ModelRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        repository.provisionBundled { }
        val model = repository.installed().first { it.manifest.format == "PTQ1_0" }
        repository.verify(model)
        val engine = BonsaiEngine()
        val settings = GenerationSettings(temperature = 0f, maxTokens = 32)
        val messages = listOf(ChatMessage("user", "What is 2 + 2? Answer with just the number."))
        try {
            engine.initialize(ModelConfig(model.file.absolutePath, model.manifest.format,
                model.manifest.specialTokens, repository.adapterFor(model)), settings)
            fun run(enabled: Boolean): String {
                engine.prepare()
                val decoder = Utf8Stream()
                val output = StringBuilder()
                engine.generate(messages, settings.copy(refusalReduction = enabled)) {
                    output.append(decoder.append(it)); true
                }
                output.append(decoder.append(byteArrayOf(), true))
                return output.toString().trim()
            }
            assertEquals("4", run(false))
            assertEquals("4", run(true))
            assertEquals("4", run(false))
            engine.prepare()
            val stats = org.json.JSONObject(engine.generate(messages, settings.copy(refusalReduction = true)) { false })
            assertTrue(stats.getBoolean("cancelled"))
            assertEquals(1, stats.getInt("generated_tokens"))
        } finally { engine.close() }
    }
}
