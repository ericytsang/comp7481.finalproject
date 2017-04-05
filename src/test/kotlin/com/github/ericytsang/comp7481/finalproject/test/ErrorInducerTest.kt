package com.github.ericytsang.comp7481.finalproject.test

import com.github.ericytsang.comp7481.finalproject.ErrorInducer
import org.junit.Test

class ErrorInducerTest
{
    @Test
    fun setFirstNToValueBoundsCheckTest1()
    {
        TestUtils.exceptionExpected {setFirstNToValueTest(0,0,true,0.toByte())}
        TestUtils.exceptionExpected {setFirstNToValueTest(0,0,false,0.toByte())}
    }

    @Test
    fun setFirstNToValueTest1()
    {
        // 0000 0000 -> 1000 0000
        setFirstNToValueTest(0,1,true,128.toByte())
        // 1111 1111 -> 0111 1111
        setFirstNToValueTest(0.inv(),1,false,127.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,1,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest2()
    {
        // 0000 0000 -> 1100 0000
        setFirstNToValueTest(0,2,true,(128+64).toByte())
        // 1111 1111 -> 0011 1111
        setFirstNToValueTest(0.inv(),2,false,63.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,2,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest3()
    {
        // 0000 0000 -> 1110 0000
        setFirstNToValueTest(0,3,true,(128+64+32).toByte())
        // 1111 1111 -> 0001 1111
        setFirstNToValueTest(0.inv(),3,false,31.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,3,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest4()
    {
        // 0000 0000 -> 1111 0000
        setFirstNToValueTest(0,4,true,(128+64+32+16).toByte())
        // 1111 1111 -> 0000 1111
        setFirstNToValueTest(0.inv(),4,false,15.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,4,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest5()
    {
        // 0000 0000 -> 1111 1000
        setFirstNToValueTest(0,5,true,(128+64+32+16+8).toByte())
        // 1111 1111 -> 0000 0111
        setFirstNToValueTest(0.inv(),5,false,7.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,5,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest6()
    {
        // 0000 0000 -> 1111 1100
        setFirstNToValueTest(0,6,true,(128+64+32+16+8+4).toByte())
        // 1111 1111 -> 0000 0011
        setFirstNToValueTest(0.inv(),6,false,3.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,6,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest7()
    {
        // 0000 0000 -> 1111 1110
        setFirstNToValueTest(0,7,true,(128+64+32+16+8+4+2).toByte())
        // 1111 1111 -> 0000 0001
        setFirstNToValueTest(0.inv(),7,false,1.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,7,false,0.toByte())
    }

    @Test
    fun setFirstNToValueTest8()
    {
        // 0000 0000 -> 1111 1111
        setFirstNToValueTest(0,8,true,(128+64+32+16+8+4+2+1).toByte())
        // 1111 1111 -> 0000 0000
        setFirstNToValueTest(0.inv(),8,false,0.toByte())
        // 0000 0000 -> 0000 0000
        setFirstNToValueTest(0,8,false,0.toByte())
    }

    @Test
    fun setFirstNToValueBoundsCheckTest2()
    {
        TestUtils.exceptionExpected {setFirstNToValueTest(0,9,true,0.toByte())}
        TestUtils.exceptionExpected {setFirstNToValueTest(0,9,false,0.toByte())}
    }

    @Test
    fun setLastNToValueBoundsCheckTest1()
    {
        TestUtils.exceptionExpected {setLastNToValueTest(0,0,true,0.toByte())}
        TestUtils.exceptionExpected {setLastNToValueTest(0,0,false,0.toByte())}
    }

    @Test
    fun setLastNToValueTest9()
    {
        // 0000 0000 -> 0000 0001
        setLastNToValueTest(0,1,true,1.toByte())
        // 1111 1111 -> 1111 1110
        setLastNToValueTest(0.inv(),1,false,(128+64+32+16+8+4+2).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,1,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest1()
    {
        // 0000 0000 -> 0000 0011
        setLastNToValueTest(0,2,true,3.toByte())
        // 1111 1111 -> 1111 1100
        setLastNToValueTest(0.inv(),2,false,(128+64+32+16+8+4).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,2,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest2()
    {
        // 0000 0000 -> 0000 0111
        setLastNToValueTest(0,3,true,7.toByte())
        // 1111 1111 -> 1111 1000
        setLastNToValueTest(0.inv(),3,false,(128+64+32+16+8).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,3,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest3()
    {
        // 0000 0000 -> 0000 1111
        setLastNToValueTest(0,4,true,15.toByte())
        // 1111 1111 -> 1111 0000
        setLastNToValueTest(0.inv(),4,false,(128+64+32+16).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,4,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest4()
    {
        // 0000 0000 -> 0001 1111
        setLastNToValueTest(0,5,true,31.toByte())
        // 1111 1111 -> 1110 0000
        setLastNToValueTest(0.inv(),5,false,(128+64+32).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,5,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest5()
    {
        // 0000 0000 -> 0011 1111
        setLastNToValueTest(0,6,true,63.toByte())
        // 1111 1111 -> 1100 0000
        setLastNToValueTest(0.inv(),6,false,(128+64).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,6,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest6()
    {
        // 0000 0000 -> 0111 1111
        setLastNToValueTest(0,7,true,127.toByte())
        // 1111 1111 -> 1000 0000
        setLastNToValueTest(0.inv(),7,false,(128).toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,7,false,0.toByte())
    }

    @Test
    fun setLastNToValueTest7()
    {
        // 0000 0000 -> 1111 1111
        setLastNToValueTest(0,8,true,255.toByte())
        // 1111 1111 -> 0000 0000
        setLastNToValueTest(0.inv(),8,false,0.toByte())
        // 0000 0000 -> 0000 0000
        setLastNToValueTest(0,8,false,0.toByte())
    }

    @Test
    fun setLastNToValueBoundsCheckTest2()
    {
        TestUtils.exceptionExpected {setLastNToValueTest(0,9,true,0.toByte())}
        TestUtils.exceptionExpected {setLastNToValueTest(0,9,false,0.toByte())}
    }

    fun setFirstNToValueTest(subject:Byte,n:Int,value:Boolean,expected:Byte)
    {
        val result = ErrorInducer.setFirstNToValue(subject,n,value)
        check(result == expected) {result}
    }

    fun setLastNToValueTest(subject:Byte,n:Int,value:Boolean,expected:Byte)
    {
        val result = ErrorInducer.setLastNToValue(subject,n,value)
        check(result == expected) {result}
    }
}
