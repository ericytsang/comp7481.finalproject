package com.github.ericytsang.comp7481.finalproject.model

import java.util.zip.CRC32

class SimpleChecksumCodingStrategy:CodingStrategy
{
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            val dataBlock = source.next() ?: return null
            val checksumCalc = CRC32()
            checksumCalc.update(dataBlock)
            return dataBlock+checksumCalc.value.toByteArray()
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            val codeBlock = source.next() ?: return null
            val checksumCalc = CRC32()
            checksumCalc.update(codeBlock.dropLast(8).toByteArray())
            return if (checksumCalc.value.toByteArray().toList() == codeBlock.takeLast(8))
            {
                codeBlock.dropLast(8).toByteArray()
            }
            else
            {
                null
            }
        }
    }
    private fun Long.toByteArray():ByteArray
    {
        return listOf(
            shr(56).and(0xff).toByte(),
            shr(48).and(0xff).toByte(),
            shr(40).and(0xff).toByte(),
            shr(32).and(0xff).toByte(),
            shr(24).and(0xff).toByte(),
            shr(16).and(0xff).toByte(),
            shr(8).and(0xff).toByte(),
            shr(0).and(0xff).toByte())
            .toByteArray()
    }

    override fun toString():String
    {
        return "CRC32"
    }
}
