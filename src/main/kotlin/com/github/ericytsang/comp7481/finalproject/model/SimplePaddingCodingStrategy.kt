package com.github.ericytsang.comp7481.finalproject.model

class SimplePaddingCodingStrategy(val padding:ByteArray):CodingStrategy
{
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            return source.next()?.plus(padding)
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
        {
            return source.next()?.dropLast(padding.size)?.toByteArray()
        }
    }
}
// todo bit error diffusion
// todo checksum
// todo repeat message n times