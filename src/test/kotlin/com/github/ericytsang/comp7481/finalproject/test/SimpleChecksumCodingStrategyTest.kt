package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.SimpleChecksumCodingStrategy
import org.junit.Test

class SimpleChecksumCodingStrategyTest:CodingStrategyTest(SimpleChecksumCodingStrategy())
{
    @Test
    fun encodingTest()
    {
        val dataBlock = listOf(byteArrayOf(0,0,0,0,0,0,0,0x04,0,0,0,0,0,0,0,0x60)).iterator()
        val codeBlock = codingStrategy.encoder.transform(dataBlock).next()!!.toList()
        check(codeBlock ==
            listOf(0,0,0,0,0,0,0,0x04,0,0,0,0,0,0,0,0x60,0,0,0,0,-4,-27,123,1).map(Int::toByte))
        {
            codeBlock
        }
    }
}
