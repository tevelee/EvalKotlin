package sample

import kotlin.test.Test
import kotlin.test.assertEquals

class DataTypeTest {
    @Test
    fun whenCallingConvert_thenUsesDataType() {
        val literal = Literal("test") { 1 }

        val dataType = DataType(listOf(literal))

        val value = dataType.convert("test", TypedInterpreter(listOf(dataType)))

        assertEquals(value, 1)
    }
}