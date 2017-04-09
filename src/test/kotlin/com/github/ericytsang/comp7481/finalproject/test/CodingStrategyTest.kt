package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.CodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import com.github.ericytsang.comp7481.finalproject.model.monitored
import com.github.ericytsang.comp7481.finalproject.model.next
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
            .all {
                val decodedBlocks = mutableListOf<ByteArray>()
                decodedBlocks += it.next()!!
                val dataBlocks = dataBlockGenerator.elements
                decodedBlocks.addAll(it.next(dataBlocks.size-1) as List<ByteArray>)
                dataBlocks.zip(decodedBlocks).all {Arrays.equals(it.first,it.second)}
            })
    }
}
