package com.github.ericytsang.comp7481.finalproject.model

import java.util.LinkedList

class SimpleHammingCodingStrategy:CodingStrategy
{
    private var pow2Calc = object
    {
        private val powersOf2 = mutableSetOf<Long>()
        private var nextExponent = 0L
        private var maxPowerOf2Computed = Long.MIN_VALUE
        fun isPowerOf2(number:Int):Boolean
        {
            // compute more powers of 2 is we haven't computed yet
            while (number > maxPowerOf2Computed)
            {
                synchronized(powersOf2)
                {
                    maxPowerOf2Computed = Math.pow(2.0,nextExponent.toDouble()).toLong()
                    powersOf2 += maxPowerOf2Computed
                    nextExponent++
                }
            }

            return number.toLong() in powersOf2
        }
    }
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            // get data block to encode
            val dataBlock = source.next()?.flatMap {it.bits} ?: return null

            // encode data block and store in code block
            val codeBlock = LinkedList<Boolean>()
            val parityBitIndices = mutableSetOf<Int>()
            for (bit in dataBlock)
            {
                // append parity bits
                while (pow2Calc.isPowerOf2(codeBlock.size+1))
                {
                    codeBlock += false
                    parityBitIndices += codeBlock.lastIndex
                }

                // append data bit
                codeBlock += bit

                // update parity bits
                parityBitIndices.forEach()
                {
                    parityBitIndex ->
                    val dataBitPosition = codeBlock.lastIndex+1

                    // skip on updating parity bit if it doesn't take this data
                    // bit position in its computations
                    if ((dataBitPosition/(parityBitIndex+1))%2 == 0) return@forEach

                    // update the parity bit
                    codeBlock[parityBitIndex] = codeBlock[parityBitIndex] xor codeBlock[dataBitPosition-1]
                }
            }

            // convert code block into a byte array and return
            val groupedCodeBits = LinkedList<List<Boolean>>()
            val codeBitsIterator = codeBlock.iterator()
            while (codeBitsIterator.hasNext())
            {
                groupedCodeBits += listOf(
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false,
                    if (codeBitsIterator.hasNext()) codeBitsIterator.next() else false)
            }
            return groupedCodeBits.map {it.byte}.toByteArray()
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            // get code block to vet
            val codeBlock = source.next()?.flatMap {it.bits} ?: return null

            // compute erroneous parity bit indices
            val checkBlock = LinkedList<Boolean>()
            val parityBitIndices = mutableSetOf<Int>()
            for (bit in codeBlock)
            {
                // append parity bit
                if (pow2Calc.isPowerOf2(checkBlock.size+1))
                {
                    checkBlock += false
                    parityBitIndices += checkBlock.lastIndex
                }

                // append data bit
                else
                {
                    checkBlock += bit
                }

                // update parity bits
                parityBitIndices.forEach()
                {
                    parityBitIndex ->
                    val dataBitPosition = checkBlock.lastIndex+1

                    // skip on updating parity bit if it doesn't take this data
                    // bit position in its computations
                    if ((dataBitPosition/(parityBitIndex+1))%2 == 0) return@forEach

                    // update the parity bit
                    checkBlock[parityBitIndex] = checkBlock[parityBitIndex] xor checkBlock[dataBitPosition-1]
                }
            }
            val erroneousParityBitIndices = (codeBlock zip checkBlock)
                .mapIndexedNotNull { index, pair -> if (pair.first != pair.second) index else null}
                .toSet()

            // fixme  pinpoint erroneous bit and flip it or discard the code block
            for (i in checkBlock.indices)
            {
                // compute parity bits for index i
                val checkBitIndices = parityBitIndices.filter()
                {
                    parityBitIndex ->
                    val dataBitPosition = i+1
                    (dataBitPosition/(parityBitIndex+1))%2 == 1
                }

                if (checkBitIndices == erroneousParityBitIndices)
                {
                    checkBlock[i] = !checkBlock[i]
                }
            }

            // remove parity bits and return data block
            val dataBitsIterator = checkBlock
                .mapIndexedNotNull { index, b -> if (pow2Calc.isPowerOf2(index+1)) null else b }
                .iterator()
            val groupedDataBits = LinkedList<List<Boolean>>()
            while (dataBitsIterator.hasNext())
            {
                groupedDataBits += listOf(
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break,
                    if (dataBitsIterator.hasNext()) dataBitsIterator.next() else break)
            }
            return groupedDataBits.map {it.byte}.toByteArray()
        }
    }
    override fun toString():String
    {
        return "Hamming"
    }
}