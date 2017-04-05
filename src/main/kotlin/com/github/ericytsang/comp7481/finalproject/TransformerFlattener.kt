package com.github.ericytsang.comp7481.finalproject

class TransformerFlattener(val transformers:List<Transformer>):AbstractTransformer()
{
    override fun transform(transformer:AbstractTransformer,source:Iterator<ByteArray>):ByteArray
    {
        return transformers.fold(source)
        {
            source,encoder ->
            object:Iterator<ByteArray>
            {
                override fun hasNext():Boolean = true
                override tailrec fun next():ByteArray
                {
                    return encoder.transform(source) ?: next()
                }
            }
        }.next()
    }
}
