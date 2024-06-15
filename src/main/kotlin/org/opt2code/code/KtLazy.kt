/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code

import org.opt2code.code.util.WrapperMap
import org.opt2code.code.util.instance
import org.opt2code.core.utils.Proxies
import org.opt2code.core.utils.WeakReferenceMap
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap


interface KtLazy {


    object InternalMap


    object InternalStaticMap


    object InternalState


    object Util


    class MapHolder(var map: MutableMap<String, Any?>? = null)


    interface FieldExt {
        var ext: MutableMap<String, Any?>?
    }


    class O: KtLazy


    open class E: FieldExt {
        override var ext: MutableMap<String, Any?>? = null
    }


    fun map(): MutableMap<String, Any?> {
        val proxyOrThis = Util.proxyOrThis()
        return InternalState.getOrPut(proxyOrThis) { InternalMap.instance() } as MutableMap<String, Any?>
    }


    fun <T> get(x: Class<T>, force: Boolean = false, f: (String) -> T) : T = get(x.canonicalName, null, force, f)


    fun fieldSet(name: String): Boolean = map().containsKey(name)


    fun propSet(name: String): Boolean = props().containsKey(name)


    fun propsSet(): Boolean = fieldSet("#pp")


    fun <T> get(name: String, g: ((T) -> T)? = null, force: Boolean = false, f: (String) -> T) : T {
        return exitingWithProxy {
            val map = exitingWithNoProxy { map() }
            val oldValue = if (force) {
                f(name).also { map.put(name, it) }
            } else {
                map.getOrPut(name) { f(name) } as T
            }

            g?.let {
                it.invoke(oldValue).also { map.put(name, it) }
            } ?: oldValue
        }
    }


    fun defaultFuncPriorityInheritance(): Boolean = true


    fun <T> calc(name: String, g: (() -> T)? = null, force: Boolean = false, f: (String) -> T) : T {
        return exitingWithProxy {
            exitingWithNoProxy { map() }.run {
                val map = this
                g?.let {
                    it.invoke().also { put(name, it) }
                } ?: if (containsKey(name)) {
                    if (force) {
                        f(name).also { map.put(name, it) }
                    } else {
                        map.get(name) as T
                    }
                } else {
                    f(name)
                }
            }
        }
    }


    fun clear() {
        InternalState.put(Util.proxyOrThis(), InternalMap.instance())
    }


    fun <T: KtLazy> T.by(source: KtLazy): T {
        Util.apply {
            exitingWithProxy{
                val proxyOrThis = exitingWithNoProxy {proxyOrThis()}
                if (proxyOrThis === source) {
                    return@exitingWithProxy
                }

                InternalState.put(proxyOrThis, WrapperMap(source.map()))
            }
        }
        return this
    }


    fun <T: KtLazy> T.of(source: KtLazy): T {
        Util.apply {
            exitingWithProxy { proxy ->
                val proxyOrThis = exitingWithNoProxy { proxyOrThis() }

                if (proxyOrThis === source) {
                    return@exitingWithProxy
                }


                val pp: MutableMap<String, Any?>? = if (source.propsSet()) {
                    exitingWithNoProxy(proxy) { props() }.apply {
                        putAll(source.props())
                    }
                } else null

                exitingWithNoProxy(proxy) { map() }.apply {
                    putAll(source.map())
                    pp?.let { put("#pp", it) }
                }
            }

        }
        return this
    }


    fun props(): MutableMap<String, Any?> {
        return get("#pp") { InternalMap.instance() }
    }


    fun <T> pup(name: String, g: (() -> T)? = null, f: (String) -> T): T {
        return exitingWithProxy {exitingWithNoProxy{props()}.run {
            g?.let {
                it.invoke().also { put(name, it)}
            } ?: if (containsKey(name)) get(name) as T else { f(name) }
        } }
    }


    fun <T> pp(name: String, g: ((T) -> T)? = null, f: (String) -> T): T {
        return exitingWithProxy {
            exitingWithNoProxy{ props() }.run {
                g?.let {
                    val res = getOrPut(name) { f(name) } as T
                    it.invoke(res).also { put(name, it)}
                } ?: getOrPut(name) { f(name) } as T
            }
        }
    }


    fun isConcurrentSafe(): Boolean = false


    fun isInConcurrentSafeMap(): Boolean = true


    fun InternalState.getOrPut(key: KtLazy, v: () -> Any?): Any? {
        if (key is FieldExt) {
            if (key.ext == null) {
                synchronized(key) {
                    key.ext ?: run {
                        key.ext = InternalMap.instance()
                    }
                }
            }
            return key.ext
        }
        val mapHolder = Proxies.mapHolderLocal.get()
        if (mapHolder != null && key !== this@KtLazy) {
            if (mapHolder.map == null) {
                synchronized(mapHolder) {
                    mapHolder.map ?: run {
                        mapHolder.map = InternalMap.instance()
                    }
                }
            }
            return mapHolder.map
        }
        return InternalStaticMap.stickyMap().getOrPut(key, v)
    }


    fun InternalState.put(key: KtLazy, value: MutableMap<String, Any?>): Any? {
        if (key is FieldExt) {
            return key.ext.also {
                key.ext = value
            }
        }
        val mapHolder = Proxies.mapHolderLocal.get()
        if (mapHolder != null && key !== this@KtLazy) {
            return mapHolder.map.also {
                mapHolder.map = value
            }
        }
        return InternalStaticMap.stickyMap().put(key, value)
    }


    fun InternalMap.instance(): MutableMap<String, Any?> {
        return if (isConcurrentSafe()) {
            ConcurrentHashMap()
        } else {
            LinkedHashMap()
        }
    }


    fun InternalStaticMap.stickyMap(): MutableMap<KtLazy, Any?> {
        return if (isInConcurrentSafeMap()) {
            concurrentStickyMap
        } else {
            stickyMap
        }
    }


    fun Util.orig(): KtWrapper {
        return KtWrapper(this@KtLazy)
    }


    fun Util.proxyOrThis(): KtLazy {
        val s = Proxies.state.get() as? KtLazy
        return if (s != null) {
            val d = Proxies.delegateLocal.get() as? KtLazy
            if (d == null || d === this@KtLazy) {
                s
            } else {
                this@KtLazy
            }
        } else {
            this@KtLazy
        }
    }

    fun Util.hints(): MutableMap<String, String> {
        val res = this@KtLazy.javaClass.let {
            if (!Proxy.isProxyClass(it)) {
                  it
            } else {
                val itfs = this.javaClass.interfaces.filter { KtLazy::class.java.isAssignableFrom(it) }
                if (itfs.size == 1) {
                    itfs.first()
                } else {
                    throw IllegalArgumentException()
                }
            }
        }
        return hints.getOrPut(res) { InternalMap.instance() as MutableMap<String, String> }
    }


    fun Util.upperFuncName(skip: Long = 1, max: Long = 10, hint: String? = null): String {
        if (hint != null) {
            hints()[hint]?.let { return it }
        }
        val extProxy = Proxies.state.get() as? KtLazy
        if (extProxy == null) {
            val methodName: String = StackWalker.getInstance().walk { frames ->
                frames.limit(max).skip(skip).findFirst().map { it.methodName.nameForField() }.orElseThrow { NoSuchElementException() }
            }
            return methodName.apply { hint?.let{ hints()[it] = this } }
        } else if(Proxy.isProxyClass(extProxy::class.java)) {
            var isFilteringStopped = false
            val methodName: String = StackWalker.getInstance(setOf(StackWalker.Option.RETAIN_CLASS_REFERENCE)).walk { frames ->
                frames.limit(max).skip(skip).filter {
                    (isFilteringStopped).run {
                        val dc = it.declaringClass
                        var res = this
                        if (!isFilteringStopped) {
                            if (Proxy.isProxyClass(dc)) {
                                isFilteringStopped = true
                            } else {
                                if (KtLazy::class.java.isAssignableFrom(dc) && dc != KtLazy::class.java) {
                                    res = true
                                }
                            }
                        }
                        res
                    }
                }.findFirst().map { it.methodName.nameForField() }.orElseThrow { NoSuchElementException() }
            }
            return methodName.apply { hint?.let{ hints()[it] = this } }
        } else {
            throw IllegalArgumentException()
        }
    }


    fun <T: KtLazy> T.init(f: Sp<T.() -> Unit>? = null): (T.() -> Unit)? = calc("#init", f) { null }


    fun <T: KtLazy> T.postInit(f: (T.() -> Unit)? = null): T {
        val itfs = this.javaClass.interfaces.filter { KtLazy::class.java.isAssignableFrom(it) }
        if (itfs.size == 1) {
            return this.postInit(itfs.first() as Class<T>, f)
        } else {
            throw IllegalArgumentException()
        }
    }


    fun <T: KtLazy> T.postInit(cls: Class<T>, f: (T.() -> Unit)? = null): T {
        return if (this is FieldExt) {
            this as T
        } else {
            instance(cls, this as T, false, proxyForce = true)
        }.also {
            it.postInitialize(true, f)
        }
    }


    companion object {
        private val stickyMap = WeakReferenceMap<KtLazy, Any?>(false)

        private val hints = WeakReferenceMap<Class<*>, MutableMap<String, String>>(true)

        private val concurrentStickyMap = WeakReferenceMap<KtLazy, Any?>(true)
    }


    fun <T> get(f: (String) -> T) : T =
            get(Util.upperFuncName(3), null, false, f)

    fun <T> get(g: ((T) -> T)? = null, hint: String, f: (String) -> T) : T =
            get(Util.upperFuncName(3, hint = hint), g, false, f)

    fun <T> get(g: ((T) -> T)?,  f: (String) -> T) : T =
            get(Util.upperFuncName(3), g, false, f)

    fun <T> get(force: Boolean, f: (String) -> T) : T =
            get(Util.upperFuncName(3), null, force, f)

    fun <T> get(g: ((T) -> T)?, force: Boolean, f: (String) -> T) : T =
            get(Util.upperFuncName(3), g, force, f)


    fun <T> calc(f: (String) -> T) : T =
            calc(Util.upperFuncName(3), null, false, f)

    fun <T> calc(g: (() -> T)? = null, hint: String, f: (String) -> T) : T =
            calc(Util.upperFuncName(3, hint = hint), g, false, f)

    fun <T> calc(g: (() -> T)?, f: (String) -> T) : T =
            calc(Util.upperFuncName(3), g, false, f)

    fun <T> calc(force: Boolean, f: (String) -> T) : T =
            calc(Util.upperFuncName(3), null, force, f)

    fun <T> calc(g: (() -> T)?, force: Boolean, f: (String) -> T) : T =
            calc(Util.upperFuncName(3), g, force, f)


    fun <T> pp(g: ((T) -> T)? = null, hint: String, f: (String) -> T): T = pp(name = Util.upperFuncName(3, hint = hint), g = g, f = f)
    fun <T> pp(g: ((T) -> T)?, f: (String) -> T): T = pp(name = Util.upperFuncName(3), g = g, f = f)
    fun <T> pp(f: (String) -> T): T = pp(name = Util.upperFuncName(3), g = null, f = f)

    fun <T> pup(g: (() -> T)? = null, hint: String, f: (String) -> T): T = pup(name = Util.upperFuncName(3, hint = hint), g = g, f = f)
    fun <T> pup(g: (() -> T)?, f: (String) -> T): T = pup(name = Util.upperFuncName(3), g = g, f = f)
    fun <T> pup(f: (String) -> T): T = pup(name = Util.upperFuncName(3), g = null, f = f)

}

fun <T: KtLazy> T.init(f: Sp<T.() -> Unit>? = null): (T.() -> Unit)? = init<T>(f)


fun <T: KtLazy> T.postInit(f: (T.() -> Unit)? = this.init()): T = postInit(f)


fun <T: KtLazy> T.postInit(cls: Class<T>, f: (T.() -> Unit)? = this.init()): T = postInit(cls, f)


class KtWrapper(val orig: KtLazy)


typealias Sp<T> = Function0<T>


typealias Fn<T> = Function1<T, T>


private fun String.nameForField(): String {
    return replace(Regex("(\\$\\d+)+$"), "").substringAfterLast('$')
}


fun <T: KtLazy> T.of(source: KtLazy): T = this.run { of(source) }


fun <T: KtLazy> T.by(source: KtLazy): T = this.run { by(source) }


@JvmOverloads
fun <T> exitingWithNoProxy(proxy: Any? = Proxies.state.get(), f: () -> T): T {
    try {
        if(proxy != null) {
            Proxies.state.set(proxy)
        }
        return f()
    } finally {
        Proxies.state.remove()
    }
}


@JvmOverloads
fun <T> exitingWithProxy(proxy: Any? = Proxies.state.get(), f: (Any?) -> T): T {
    try {
        return f(proxy)
    } finally {
        if (proxy != null) {
            Proxies.state.set(proxy)
        }
    }
}


@JvmOverloads
fun <T> inNoProxy(proxy: Any? = Proxies.state.get(), f: () -> T): T {
    try {
        if (proxy != null) {
            Proxies.state.remove()
        }
        return f()
    } finally {
        if (proxy != null) {
            Proxies.state.set(proxy)
        } else {
            Proxies.state.remove()
        }
    }
}


fun <T: KtLazy> T.postInitialize(enabled: Boolean = true, f: (T.() -> Unit)? = null): T {
    if (!enabled) {
        return this
    }
    if (f != null) {
        f(this)
        init{f}
    } else {
        val code = init<T>()
        code?.invoke(this)
    }
    return this
}



