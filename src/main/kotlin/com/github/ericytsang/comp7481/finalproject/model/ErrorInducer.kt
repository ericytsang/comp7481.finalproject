package com.github.ericytsang.comp7481.finalproject.model

import java.util.LinkedList

class ErrorInducer:AbstractTransformer()
{
    var burstErrorCounter = 0L
    var currentBurstBitErrorsRemaining = 0
    var errorBitValue = false
    var targetBurstErrorFrequency = 0.0
    var minBurstLength = 1
    var maxBurstLength = 32
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
    {
        val bytes = source.next()
        return bytes
            ?.asSequence()

            // map each byte to a list of 8 booleans
            ?.map(Byte::bits)

            // introduce bit errors
            ?.map()
            {
                byteBits ->
                byteBits.map()
                {
                    bit ->

                    // possibly go to error state
                    if (currentBurstBitErrorsRemaining == 0 && Math.random() < targetBurstErrorFrequency)
                    {
                        errorBitValue = Math.random() < 0.5
                        currentBurstBitErrorsRemaining = minBurstLength+(Math.random()*(maxBurstLength-minBurstLength+1)).toInt()
                        burstErrorCounter++
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
            ?.map(List<Boolean>::byte)
            ?.toCollection(LinkedList())
            ?.toByteArray()
    }
}
