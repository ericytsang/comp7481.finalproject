package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.model.ErrorInducer
import org.junit.Test

class ErrorInducerTest
{
    @Test
    fun noErrorsTest()
    {
        val testSubject = ErrorInducer()
        testSubject.currentBurstBitErrorsRemaining = 0
        testSubject.targetBurstErrorFrequency = 0.0
        testSubject.errorBitValue = true
        testSubject.maxBurstLength = 10
        testSubject.minBurstLength = 10
        val bytes = listOf(
            byteArrayOf(0),
            byteArrayOf(0,0),
            byteArrayOf(0,0,0),
            byteArrayOf(0,0,0,0),
            byteArrayOf(0,0,0,0,0),
            byteArrayOf(0,0,0,0,0),
            byteArrayOf(1,2,3,4,5,6,7,8),
            byteArrayOf(9,10,11,12,13,14,15,16,17,18,19,20),
            (Byte.MIN_VALUE..Byte.MAX_VALUE).map(Int::toByte).toByteArray())
            .iterator()
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0,0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0,0,0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0,0,0,0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0,0,0,0,0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(0,0,0,0,0).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(1,2,3,4,5,6,7,8).toList())
        check(testSubject.transform(bytes).next()!!.toList() == byteArrayOf(9,10,11,12,13,14,15,16,17,18,19,20).toList())
        check(testSubject.transform(bytes).next()!!.toList() == (Byte.MIN_VALUE..Byte.MAX_VALUE).map(Int::toByte))
    }

    @Test
    fun yesErrorsTest()
    {
        val testSubject = ErrorInducer()
        testSubject.currentBurstBitErrorsRemaining = 0
        testSubject.targetBurstErrorFrequency = 0.2
        testSubject.errorBitValue = true
        testSubject.maxBurstLength = 15
        testSubject.minBurstLength = 5
        val source = object:Iterator<ByteArray>
        {
            override fun hasNext():Boolean = true
            override fun next():ByteArray = byteArrayOf(0,0,0,0)
        }
        val errorLengths = (testSubject.minBurstLength..testSubject.maxBurstLength).toCollection(mutableSetOf())
        val bitGenerator = object:Iterator<Boolean>
        {
            private var remainingBits = mutableListOf<Boolean>()
            override fun hasNext():Boolean = true
            override fun next():Boolean
            {
                if (remainingBits.isEmpty())
                {
                    remainingBits = testSubject.transform(source).next()!!
                        .flatMap()
                        {
                            listOf(
                                0b10000000 and it.toInt() != 0,
                                0b01000000 and it.toInt() != 0,
                                0b00100000 and it.toInt() != 0,
                                0b00010000 and it.toInt() != 0,
                                0b00001000 and it.toInt() != 0,
                                0b00000100 and it.toInt() != 0,
                                0b00000010 and it.toInt() != 0,
                                0b00000001 and it.toInt() != 0)
                        }
                        .toMutableList()
                }
                return remainingBits.removeAt(0)
            }
        }
        while (errorLengths.isNotEmpty())
        {
            val errorBits = bitGenerator.asSequence().takeWhile {it}.toList()
            check(errorBits.isEmpty() || errorBits.size >= testSubject.minBurstLength) {errorBits}
            errorLengths.remove(errorBits.size)
        }
    }
}
