package sample

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTestsJVM {
    @Test
    fun test_whenPlatformNameIsInvoked_thenItReturnsJVM() {
        assertTrue("JVM" in Platform.name)
    }
}