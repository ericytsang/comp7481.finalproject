package com.github.ericytsang.comp7481.finalproject.model

class MonitoredIterator<out T>(val watched:Iterator<T>):Iterator<T>
{
    val elements:List<T>
        get() = synchronized(_elements)
        {
            val result = _elements.toList()
            _elements.clear()
            result
        }
    private val _elements = mutableListOf<T>()
    override fun hasNext():Boolean = watched.hasNext()
    override fun next():T = synchronized(_elements)
    {
        val element = watched.next()
        _elements += element
        return@synchronized element
    }
}
