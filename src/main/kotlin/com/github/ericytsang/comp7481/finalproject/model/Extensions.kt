package com.github.ericytsang.comp7481.finalproject.model

/**
 * [Byte]
 */

val Byte.bits:List<Boolean> get()
{
    return listOf(
        (0b10000000 and toInt()) != 0,
        (0b01000000 and toInt()) != 0,
        (0b00100000 and toInt()) != 0,
        (0b00010000 and toInt()) != 0,
        (0b00001000 and toInt()) != 0,
        (0b00000100 and toInt()) != 0,
        (0b00000010 and toInt()) != 0,
        (0b00000001 and toInt()) != 0)
}

val Collection<Boolean>.byte:Byte get()
{
    require(size == 8)
    return foldIndexed(0)
    {
        i,byte,bit ->
        if (bit) byte or 0b10000000.ushr(i) else byte
    }.toByte()
}
