package sample

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTestsJS {
    @Test
    fun test_whenPlatformNameIsInvoked_thenItReturnsJS() {
        assertTrue("JS" in Platform.name)
    }
}