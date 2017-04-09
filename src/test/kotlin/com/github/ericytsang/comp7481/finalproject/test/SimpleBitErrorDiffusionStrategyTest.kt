package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.SimpleBitErrorDiffusionStrategy
import com.github.ericytsang.comp7481.finalproject.model.bits
import org.junit.Test

class SimpleBitErrorDiffusionStrategyTest:CodingStrategyTest(SimpleBitErrorDiffusionStrategy(3))
{
    @Test
    fun encodeTest()
    {
        val dataBlock = listOf(byteArrayOf(1),byteArrayOf(-1),byteArrayOf(0)).iterator()
        val codeBlock = codingStrategy.encoder.transform(dataBlock).next()!!
        check(codeBlock.flatMap {it.bits}.take(24) == listOf(
            false,true,false,
            false,true,false,
            false,true,false,
            false,true,false,
            false,true,false,
            false,true,false,
            false,true,false,
            true,true,false))
        {codeBlock.flatMap {it.bits}}
    }
}
