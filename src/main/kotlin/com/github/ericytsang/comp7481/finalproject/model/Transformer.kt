package com.github.ericytsang.comp7481.finalproject.model

interface Transformer
{
    //fun transform(source:Iterator<ByteArray>):ByteArray?
    fun transform(source:Iterator<ByteArray?>):Iterator<ByteArray?>
    val transformObservers:MutableSet<Observer>
    interface Observer
    {
        fun onTransform(input:List<ByteArray?>,output:ByteArray?)
    }
}
