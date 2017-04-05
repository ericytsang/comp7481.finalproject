package com.github.ericytsang.comp7481.finalproject

import java.util.LinkedList
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class ErrorInducer:AbstractTransformer()
{
    var currentBurstBitErrorsRemaining = 0
    var errorBitValue = false
    var targetBurstErrorFrequency = 0.0
    var minBurstLength = 1
    var maxBurstLength = 32
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
    {
        val bytes = source.next()
        return bytes
            .asSequence()

            // map each byte to a list of 8 booleans
            .map()
            {
                byte ->
                listOf(
                    (0b10000000 and byte.toInt()) != 0,
                    (0b01000000 and byte.toInt()) != 0,
                    (0b00100000 and byte.toInt()) != 0,
                    (0b00010000 and byte.toInt()) != 0,
                    (0b00001000 and byte.toInt()) != 0,
                    (0b00000100 and byte.toInt()) != 0,
                    (0b00000010 and byte.toInt()) != 0,
                    (0b00000001 and byte.toInt()) != 0)
            }

            // introduce bit errors
            .map()
            {
                byteBits ->
                byteBits.map()
                {
                    bit ->

                    // possibly go to error state
                    if (currentBurstBitErrorsRemaining == 0 && Math.random() < targetBurstErrorFrequency)
                    {
                        errorBitValue = Math.random() < 0.5
                        currentBurstBitErrorsRemaining = minBurstLength+(Math.random()*maxBurstLength).toInt()
                    }

                    // no error state right now...return actual bit
                    if (currentBurstBitErrorsRemaining == 0)
                    {
                        bit
                    }

                    // error state, make bits into errors...
                    else
                    {
                        --currentBurstBitErrorsRemaining
                        errorBitValue
                    }
                }
            }

            // rebuild the byte
            .map()
            {
                bits ->
                var byte = 0
                for (i in 0..7)
                {
                    val mask = 0b10000000
                    if (bits[i]) byte = byte or mask.ushr(i)
                }
                byte.toByte()
            }
            .toCollection(LinkedList())
            .toByteArray()
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
