package com.github.ericytsang.comp7481.finalproject

class SimplePaddingCodingStrategy(val padding:ByteArray):CodingStrategy
{
    override val encoder:CodingStrategy.EncodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
        {
            return source.next()+padding
        }
    }
    override val decoder:CodingStrategy.DecodingStrategy = object:AbstractTransformer()
    {
        override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray?
        {
            return source.next().dropLast(padding.size).toByteArray()
        }
    }
}
