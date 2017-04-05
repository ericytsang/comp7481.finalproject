package com.github.ericytsang.comp7481.finalproject

interface Transformer
{
    fun transform(source:Iterator<ByteArray>):ByteArray?
    val transformObservers:MutableSet<Observer>
    interface Observer
    {
        fun onTransform(input:List<ByteArray>,output:ByteArray?)
    }
}
