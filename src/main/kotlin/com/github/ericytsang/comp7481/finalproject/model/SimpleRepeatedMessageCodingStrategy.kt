package com.github.ericytsang.comp7481.finalproject.model

import java.util.LinkedList

class SimpleRepeatedMessageCodingStrategy(val times:Int):CodingStrategy
{
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            val dataBlock = source.next() ?: return null
            return (1..times).flatMap {dataBlock.asList()}.toByteArray()
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            val codeBlock = source.next()?.flatMap {it.bits} ?: return null
            val dataBlockLen = codeBlock.size/times
            val trueCount = Array(dataBlockLen,{0})
            for (i in codeBlock.indices)
            {
                trueCount[i%dataBlockLen] += if (codeBlock[i]) 1 else -1
            }
            return if (trueCount.any {it == 0})
            {
                null
            }
            else
            {
                val bitsIterator = trueCount.map {it > 0}.iterator()
                val result = LinkedList<Byte>()
                while (bitsIterator.hasNext())
                {
                    result += bitsIterator.next(8).byte
                }
                result.toByteArray()
            }
        }
    }
    override fun toString():String
    {
        return "Repeat message $times times"
    }
}
