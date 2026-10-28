package org.meshmind.app

import org.junit.Assert.*
import org.junit.Test
import org.meshmind.model.ModelManifest
import org.meshmind.model.RefusalAdapter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

class RefusalAdapterTest {
    private val bytes = byteArrayOf(0x47, 0x47, 0x55, 0x46, 1, 2, 3)
    private val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun spec(size: Long = bytes.size.toLong(), sha: String = hash) = RefusalAdapter(size, sha, "a".repeat(64), "PTQ1_0")
    private fun verify(data: ByteArray, adapter: RefusalAdapter = spec()): ByteArray {
        val output = ByteArrayOutputStream()
        adapter.copyVerified(ByteArrayInputStream(data), output)
        return output.toByteArray()
    }
    @Test fun validBytesSurviveVerification() { assertArrayEquals(bytes, verify(bytes)) }
    @Test fun truncatedAdapterIsRejected() { assertThrows(IllegalArgumentException::class.java) { verify(bytes.dropLast(1).toByteArray()) } }
    @Test fun oversizedAdapterIsRejected() { assertThrows(IllegalArgumentException::class.java) { verify(bytes + 0) } }
    @Test fun sameLengthCorruptionIsRejected() { assertThrows(IllegalArgumentException::class.java) { verify(bytes.copyOf().also { it[5] = 9 }) } }
    @Test fun officialQ2BaseIsRejected() {
        val model = ModelManifest.parse(File("../model-manager/manifests/bonsai-Q2_0.json").readText())
        val adapter = RefusalAdapter.parse(File("../model-manager/adapters/atomicchat-2b.json").readText())
        assertThrows(IllegalArgumentException::class.java) { adapter.requireBase(model) }
    }
    @Test fun pinnedPTQBaseIsAccepted() {
        val model = ModelManifest.parse(File("../model-manager/manifests/bonsai-PTQ1_0.json").readText())
        RefusalAdapter.parse(File("../model-manager/adapters/atomicchat-2b.json").readText()).requireBase(model)
    }
    @Test fun unboundedSizeIsRejected() {
        val text = File("../model-manager/adapters/atomicchat-2b.json").readText().replace("1187136", "999999999")
        assertThrows(IllegalArgumentException::class.java) { RefusalAdapter.parse(text) }
    }
}
