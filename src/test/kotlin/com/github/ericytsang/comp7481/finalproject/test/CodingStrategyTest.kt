package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import com.github.ericytsang.comp7481.finalproject.model.monitored
import org.junit.Test
import java.util.Arrays

abstract class CodingStrategyTest(val codingStrategy:CodingStrategy)
{
    val dataBlockGenerator = DataBlockGenerator().monitored()

    @Test
    fun encodingDecodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder,codingStrategy.decoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockGenerator)}
            .let {
                val decodedBlocks = dataBlockGenerator.elements.iterator()
                it.all {
                    while (decodedBlocks.hasNext())
                    {
                        if (!Arrays.equals(it.next()!!,decodedBlocks.next())) return@all false
                    }
                    true
                }
            })
    }
}
