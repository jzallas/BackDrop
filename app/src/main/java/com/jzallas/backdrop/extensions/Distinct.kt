package com.jzallas.backdrop.extensions

import kotlin.properties.Delegates

/**
 * [onChange] is called after the change is made and only if the new value differs from the old value
 */
inline fun <T> distinct(initialValue: T, crossinline onChange: (old: T, new: T) -> Unit) =
  Delegates.observable(initialValue) { _, old, new ->
    if (old != new) onChange.invoke(old, new)
}
