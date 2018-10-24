package sample

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTestsIOS {
    @Test
    fun test_whenPlatformNameIsInvoked_thenItReturnsIOS() {
        assertTrue("iOS" in Platform.name)
    }
}