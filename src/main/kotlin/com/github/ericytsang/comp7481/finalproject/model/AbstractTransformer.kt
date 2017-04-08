package com.github.ericytsang.comp7481.finalproject

import java.util.LinkedHashSet

abstract class AbstractTransformer:Transformer,CodingStrategy.EncodingStrategy,CodingStrategy.DecodingStrategy
{
    override fun transform(source:Iterator<ByteArray>):ByteArray?
    {
        val input = mutableListOf<ByteArray>()
        val sourceWrapper = object:Iterator<ByteArray>
        {
            override fun hasNext():Boolean = source.hasNext()
            override fun next():ByteArray
            {
                val element = source.next()
                input += element
                return element
            }
        }
        val output = transform(this,sourceWrapper)
        transformObservers.forEach {it.onTransform(input,output)}
        return output
    }
    protected abstract fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
    override val transformObservers:MutableSet<Transformer.Observer> = LinkedHashSet()
}
