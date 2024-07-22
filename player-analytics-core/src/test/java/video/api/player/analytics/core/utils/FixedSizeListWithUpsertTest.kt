package video.api.player.analytics.core.utils

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class FixedSizeListWithUpsertTest {
    @Test
    fun `test upsert`() {
        val expected = listOf("a", "b")

        val actual = FixedSizeListWithUpsert<String>(3)
        actual.add("a")
        actual.add("b")
        actual.add("b")
        assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
    }

    @Test
    fun `test list size`() {
        val expected = listOf("b", "c")

        val actual = FixedSizeListWithUpsert<String>(2)
        actual.add("a")
        actual.add("b")
        actual.add("c")
        assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
    }
}