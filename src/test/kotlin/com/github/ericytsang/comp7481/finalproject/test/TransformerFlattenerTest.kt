package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.SimplePaddingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import com.github.ericytsang.comp7481.finalproject.model.monitored
import org.junit.Test
import java.util.Arrays

class TransformerFlattenerTest
{
    val dataBlockGenerator = DataBlockGenerator().monitored()
    val codingStrategy = SimplePaddingCodingStrategy(byteArrayOf(0,0,0,0))

    @Test
    fun encodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockGenerator)}
            .all {it.next()!!.takeLast(codingStrategy.padding.size) == codingStrategy.padding.asList()})
    }

    @Test
    fun nEncodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder,codingStrategy.encoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockGenerator)}
            .all {it.next()!!.takeLast(codingStrategy.padding.size*2) == codingStrategy.padding.asList()+codingStrategy.padding.asList()})
    }

    @Test
    fun encodingDecodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder,codingStrategy.decoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockGenerator)}
            .all {Arrays.equals(it.next()!!,dataBlockGenerator.elements.single())})
    }
}
