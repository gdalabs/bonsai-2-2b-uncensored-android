// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 GDA Labs
package dev.gdalabs.uipreview

import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import dev.gdalabs.localui.*
import org.junit.Rule
import org.junit.Test

class Screenshots {
    @get:Rule val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar", showSystemUi = true)
    private fun capture(kind: String, family: String, variant: String, version: String, format: String, bytes: Long, page: Int, dark: Boolean, name: String) {
        paparazzi.unsafeUpdateConfig(deviceConfig = DeviceConfig.PIXEL_5.copy(nightMode = if (dark) NightMode.NIGHT else NightMode.NOTNIGHT, locale = "en"), theme = if (dark) "android:Theme.Material.NoActionBar" else "android:Theme.Material.Light.NoActionBar")
        val identity = AppIdentity(family, variant, version, "model.json", "Apache-2.0.txt", "Apache-2.0.txt", format)
        val state = ModelAppState(
            listOf(UiMessage("user", "What can I use an offline AI for?"), UiMessage("assistant", "### A few everyday ideas\n\n- **Write:** draft messages and notes.\n- **Learn:** get a simple explanation.\n- **Explore:** brainstorm a new idea.\n\nYour conversation stays on this device.")),
            listOf(UiModel("sample", family + " " + variant, format, bytes)),
            true, false, false, "sample", "Offline AI", null, "", "",
            UiTuning(2, 2048, 512, .1f, 1f, 50, false, true, kind == "bonsai"))
        val actions = ModelAppActions({}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {}, if (kind == "bonsai") ({ _ -> }) else null)
        paparazzi.snapshot(name) { ModelAppTheme(forceDark = dark) { ModelAppScreen(identity, state, actions, emptyList(), { "Sample model" }, initialPage = page) } }
    }
    @Test fun bonsai_chat_light() = capture("bonsai", "Bonsai 2 2B", "Uncensored", "0.1.2-uncensored-preview", "PTQ1_0", 429183456L, 0, false, "bonsai-chat-light")
    @Test fun bonsai_chat_dark() = capture("bonsai", "Bonsai 2 2B", "Uncensored", "0.1.2-uncensored-preview", "PTQ1_0", 429183456L, 0, true, "bonsai-chat-dark")
    @Test fun bonsai_models() = capture("bonsai", "Bonsai 2 2B", "Uncensored", "0.1.2-uncensored-preview", "PTQ1_0", 429183456L, 1, false, "bonsai-models")
    @Test fun bonsai_settings() = capture("bonsai", "Bonsai 2 2B", "Uncensored", "0.1.2-uncensored-preview", "PTQ1_0", 429183456L, 2, false, "bonsai-settings")
}
