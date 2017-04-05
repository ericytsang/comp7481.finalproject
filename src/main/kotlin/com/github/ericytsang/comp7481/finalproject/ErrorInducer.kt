package com.github.ericytsang.comp7481.finalproject

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class ErrorInducer:AbstractTransformer()
{
    var bitErrorsRemaining = 0
    var targetBurstErrorFrequency = 0.0
    var minBurstLength = 1
    var maxBurstLength = 32
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
    {
        val bytes = source.next()
        //bytes.
        if (bitErrorsRemaining > 0)
        {

        }
        TODO()// todo
    }

    companion object
    {
        fun setFirstNToValue(subject:Byte,n:Int,value:Boolean):Byte
        {
            require(n in 1..8)
            val mask = 255.shl(8-n).toByte()
            return if (value)
            {
                subject or mask
            }
            else
            {
                subject and mask.inv()
            }
        }

        fun setLastNToValue(subject:Byte,n:Int,value:Boolean):Byte
        {
            require(n in 1..8)
            val mask = 255.ushr(8-n).toByte()
            return if (value)
            {
                subject or mask
            }
            else
            {
                subject and mask.inv()
            }
        }
    }
}
