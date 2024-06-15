/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code.util

class WrapperMap<S, T>(private val map: MutableMap<S,T>): MutableMap<S,T> {

    override val size: Int
        get() = map.size

    override fun containsKey(key: S): Boolean {
        return map.containsKey(key)
    }

    override fun containsValue(value: T): Boolean {
        return map.containsValue(value)
    }

    override fun get(key: S): T? {
        return map.get(key)
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<S, T>>
        get() = map.entries

    override val keys: MutableSet<S>
        get() = map.keys

    override val values: MutableCollection<T>
        get() = map.values

    override fun clear() {
        map.clear()
    }

    override fun put(key: S, value: T): T? {
        return map.put(key, value)
    }

    override fun putAll(from: Map<out S, T>) {
        map.putAll(from)
    }

    override fun remove(key: S): T? {
        return map.remove(key)
    }

}