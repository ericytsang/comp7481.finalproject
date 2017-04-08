package com.github.ericytsang.comp7481.finalproject.model

import com.github.ericytsang.lib.randomstream.RandomInputStream

class DataBlockGenerator(val dataBlockSize:Int = 16):Iterator<ByteArray>
{
    private val randomStream = RandomInputStream()
    override fun hasNext():Boolean = true
    override fun next():ByteArray
    {
        val next = ByteArray(dataBlockSize)
        randomStream.read(next)
        return next
    }
}
