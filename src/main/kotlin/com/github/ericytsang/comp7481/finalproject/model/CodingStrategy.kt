package com.github.ericytsang.comp7481.finalproject.model


interface CodingStrategy
{
    interface EncodingStrategy:Transformer
    interface DecodingStrategy:Transformer

    val encoder:EncodingStrategy
    val decoder:DecodingStrategy
}
