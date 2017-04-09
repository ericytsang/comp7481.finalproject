package com.github.ericytsang.comp7481.finalproject.model

interface Transformer
{
    fun transform(source:Iterator<ByteArray?>):Iterator<ByteArray?>
}
