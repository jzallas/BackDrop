package com.jzallas.backdrop.extensions.collections

import android.util.SparseArray

inline fun <T> SparseArray<T>.asIterable(): Iterable<T> = SparseArrayIterable(this)

class SparseArrayIterable<T>(private val a: SparseArray<T>) : Iterable<T> {
  override fun iterator(): Iterator<T> = SparseArrayIterator()

  private inner class SparseArrayIterator : Iterator<T> {
    private var index = 0
    private val size = a.size()

    override fun hasNext() = size > index

    override fun next(): T {
      if (a.size() != size) throw ConcurrentModificationException()
      return a.valueAt(index++)
    }
  }
}
