package com.github.ericytsang.comp7481.finalproject

import com.github.ericytsang.lib.randomstream.RandomInputStream
import java.util.LinkedHashSet

interface Transformer
{
    fun transform(source:Iterator<ByteArray>):ByteArray?
    val transformObservers:MutableSet<Observer>
    interface Observer
    {
        fun onTransform(input:List<ByteArray>,output:ByteArray?)
    }
}

abstract class AbstractTransformer:Transformer,EncodingStrategy,DecodingStrategy
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

interface EncodingStrategy:Transformer
interface DecodingStrategy:Transformer

interface CodingStrategy
{
    val encoder:EncodingStrategy
    val decoder:DecodingStrategy
}

class DataBlockGenerator:Iterator<ByteArray>
{
    var dataBlockSize:Int = 16
    private val randomStream = RandomInputStream()
    override fun hasNext():Boolean = true
    override fun next():ByteArray
    {
        val next = ByteArray(dataBlockSize)
        randomStream.read(next)
        return next
    }
}

class TransformerFlattener(val transformers:List<Transformer>):AbstractTransformer()
{
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray
    {
        return transformers.fold(source)
        {
            source,encoder ->
            object:Iterator<ByteArray>
            {
                override fun hasNext():Boolean = true
                override tailrec fun next():ByteArray
                {
                    return encoder.transform(source) ?: next()
                }
            }
        }.next()
    }
}

class SimplePaddingCodingStrategy(val padding:ByteArray):CodingStrategy
{
    override val encoder:EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
        {
            return source.next()+padding
        }
    }
    override val decoder:DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
        {
            return source.next().dropLast(padding.size).toByteArray()
        }
    }
}
