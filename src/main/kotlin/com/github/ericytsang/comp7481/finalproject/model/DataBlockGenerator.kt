package com.github.ericytsang.comp7481.finalproject

import com.github.ericytsang.lib.randomstream.RandomInputStream

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
