package com.github.ericytsang.comp7481.finalproject.model

import java.util.ArrayList
import java.util.LinkedList
import java.util.concurrent.LinkedBlockingQueue

class SimpleBitErrorDiffusionStrategy(val dataBlocksPerCodeBlock:Int):CodingStrategy
{
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            // get the data blocks
            val dataBlocks = source.next(dataBlocksPerCodeBlock).map {it?.flatMap {it.bits}?:return null}

            // build the code block (transpose of all data blocks)
            val dataBlockLen = dataBlocks.first().size
            val codeBlock = LinkedList<Boolean>()
            for (bitIndex in 0..dataBlockLen-1)
            {
                for (dataBlockIndex in dataBlocks.indices)
                {
                    codeBlock += dataBlocks[dataBlockIndex][bitIndex]
                }
            }

            // turn the code block into a byte array
            val codeBlockIterator = codeBlock.iterator()
            val codeBlockBytes = LinkedList<Byte>()
            while (codeBlockIterator.hasNext())
            {
                codeBlockBytes += codeBlockIterator.next(8).byte
            }
            return codeBlockBytes.toByteArray()
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        val dataBlockQ = LinkedBlockingQueue<()->ByteArray?>()
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            if (dataBlockQ.isEmpty())
            {
                val codeBlock = source.next()?.flatMap {it.bits}
                if (codeBlock == null)
                {
                    repeat(dataBlocksPerCodeBlock) {dataBlockQ.put {null}}
                }
                else
                {
                    val dataBlockLen = codeBlock.size/dataBlocksPerCodeBlock
                    val dataBlocks = (1..dataBlocksPerCodeBlock).map {ArrayList<Boolean>(dataBlockLen)}
                    for (bitIndex in 0..dataBlockLen-1)
                    {
                        for (dataBlockIndex in 0..dataBlocksPerCodeBlock-1)
                        {
                            dataBlocks[dataBlockIndex] += codeBlock[bitIndex*dataBlocksPerCodeBlock+dataBlockIndex]
                        }
                    }
                    val dataBlocksAsByteArrays = dataBlocks.map()
                    {
                        val bytes = LinkedList<Byte>()
                        val bitIterator = it.iterator()
                        while (bitIterator.hasNext()) bytes += bitIterator.next(8).byte
                        bytes.toByteArray()
                    }
                    dataBlockQ.addAll(dataBlocksAsByteArrays.map {{it}})
                }
            }

            return dataBlockQ.take().invoke()
        }
    }
    override fun toString():String
    {
        return "Error diffusion (transpose $dataBlocksPerCodeBlock data blocks per code block)"
    }
}
