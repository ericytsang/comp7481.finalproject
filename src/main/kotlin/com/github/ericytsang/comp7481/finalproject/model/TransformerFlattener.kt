package com.github.ericytsang.comp7481.finalproject.model

class TransformerFlattener(val transformers:List<Transformer>):AbstractTransformer()
{
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray?>):ByteArray?
    {
        return transformers.fold(source)
        {
            source,encoder ->
            encoder.transform(source)
        }.next()
    }
}
