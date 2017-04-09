package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.SimpleHammingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.bits
import com.github.ericytsang.comp7481.finalproject.model.byte
import org.junit.Test

class SimpleHammingCodingStrategyTest:CodingStrategyTest(SimpleHammingCodingStrategy())
{
    @Test
    fun encodeTest()
    {
        val dataBlock = listOf(byteArrayOf(listOf(true,false,true,true,false,false,true,false).byte)).iterator()
        val codeBlock = codingStrategy.encoder.transform(dataBlock).next()!!
        check(codeBlock.flatMap {it.bits}.take(12) == listOf(true,false,true,false,false,true,true,true,false,false,true,false))
        {codeBlock.flatMap {it.bits}}
    }
}
