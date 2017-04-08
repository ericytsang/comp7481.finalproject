package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.DataBlockGenerator
import com.github.ericytsang.comp7481.finalproject.model.SimplePaddingCodingStrategy
import com.github.ericytsang.comp7481.finalproject.model.Transformer
import com.github.ericytsang.comp7481.finalproject.model.TransformerFlattener
import org.junit.Test
import java.util.Arrays

class TransformerFlattenerTest
{
    val dataBlockDenerator = DataBlockGenerator()
    val codingStrategy = SimplePaddingCodingStrategy(byteArrayOf(0,0,0,0))

    @Test
    fun encodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockDenerator)}
            .all {it.next()!!.takeLast(codingStrategy.padding.size) == codingStrategy.padding.asList()})
    }

    @Test
    fun nEncodingStrategy()
    {
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder,codingStrategy.encoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockDenerator)}
            .all {it.next()!!.takeLast(codingStrategy.padding.size*2) == codingStrategy.padding.asList()+codingStrategy.padding.asList()})
    }

    @Test
    fun encodingDecodingStrategy()
    {
        var codeInput:ByteArray = byteArrayOf()
        codingStrategy.encoder.transformObservers += object:Transformer.Observer
        {
            override fun onTransform(input:List<ByteArray?>,output:ByteArray?)
            {
                codeInput = input.single()!!
            }
        }
        val testSubject = TransformerFlattener(listOf(codingStrategy.encoder,codingStrategy.decoder))
        check((1..1000)
            .asSequence()
            .map {testSubject.transform(dataBlockDenerator)}
            .all {Arrays.equals(it.next()!!,codeInput)})
    }
}
