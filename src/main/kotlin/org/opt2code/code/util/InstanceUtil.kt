/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */


package org.opt2code.code.util

import org.opt2code.code.KtLazy
import org.opt2code.code.init
import org.opt2code.code.postInitialize
import org.opt2code.core.utils.Proxies
import java.lang.reflect.Proxy



@JvmOverloads
fun <T: KtLazy> T.byInstance(cls: Class<T>? = null): T = instance(cls ?: javaClass, this, false).by(this)


@JvmOverloads
fun <T: KtLazy> T.ofInstance(cls: Class<T>? = null): T = instance(cls ?: javaClass, this, false).of(this)


private fun <T: KtLazy> Class<*>.instance(cls: Class<T>) = getDeclaredConstructor().apply { trySetAccessible() }.newInstance() as T


@JvmOverloads
fun <T: KtLazy> instance(cls: Class<T>, obj: T? = null, postInit: Boolean = true, proxyForce: Boolean = false): T {
    cls.let {
        if (!proxyForce) {
            try {
                if (it.isInterface) {
                    if (obj == null || obj.javaClass.simpleName == "O") {
                        val c = classForNameOrNull("${it.name + "$"}O")
                        if (c != null && !c.isInterface && KtLazy::class.java.isAssignableFrom(c)) {
                            return c.instance(cls).postInitialize(postInit, obj?.init())
                        }
                    } else if (!Proxy.isProxyClass(obj.javaClass)) {
                        return obj.javaClass.instance(cls).postInitialize(postInit, obj.init())
                    }
                } else {
                    return it.instance(cls).postInitialize(postInit, obj?.init())
                }
            } catch (e: Throwable) {
            }
        }

        val res = if (obj == null) {
            null
        } else if (Proxy.isProxyClass(obj.javaClass)) {
            val orig = obj.run { KtLazy.Util.orig().orig }
            if (cls.isAssignableFrom(orig.javaClass)) {
                orig
            } else {
                null
            }
        } else {
            obj
        }

        return (Proxies.delegatingProxy(res, cls) as T).postInitialize(postInit, obj?.init())
    }
}


fun classForNameOrNull(name: String): Class<*>? {
    return try {
        Class.forName(name)
    } catch (e: Throwable) {
        null
    }
}

