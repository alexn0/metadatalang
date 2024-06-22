/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.*
import org.springframework.context.annotation.Bean
import org.opt2code.code.KtLazy.Util
import kotlin.reflect.KProperty0

import org.springframework.stereotype.Component

// It is recommended to redefine KtLazy, so you can redefine and add convenience methods as we do now.
interface KtLazyExt: KtLazy {

    fun KtLazyExt.ignoredMethodNames() = get { hashSetOf("calc", "get", "pp", "pup", "default", "invokeWithArguments", "invoke") }

    fun <T> set(prop: KProperty0<T>, g: T) { map().put(prop.name, g) }

    fun <T> calc(g: (() -> T)? = null, normalize: Fn<String>, hint: String? = null, f: (String) -> T) : T  =
            calc(normalize(fieldName()), g, false, f)

    fun <T> get(g: ((T) -> T)? = null, normalize: Fn<String>, hint: String? = null,  f: (String) -> T) : T =
            get(normalize(fieldName()), g, false, f)

    fun <T> pp(g: ((T) -> T)? = null, normalize: Fn<String>, hint: String? = null, f: (String) -> T): T =
            pp(name = normalize(fieldName()), g = g, f = f)

    fun <T> pup(g: (() -> T)? = null, normalize: Fn<String>, hint: String? = null, f: (String) -> T): T =
            pup(name = normalize(fieldName()), g = g, f = f)

    // this function is needed here not for beauty. if use "getter<T> { throw IllegalArgumentException() }" instead of "getter<T> { illegal() }",
    // the code will be optimized to throw IllegalArgumentException always! (it looks like a bug of kotlin compiler I use)
    fun <T> illegal(): T { throw IllegalArgumentException() }

}


inline fun KtLazyExt.fieldName(hint: String? = null) = Util.upperFuncName(hint = hint) { !ignoredMethodNames().contains(it) }



interface HelloWorldService : KtLazyExt {
    // added to demonstrate that we can use "var" property syntax as well
    var printer: Printer set(f) = setter(f); get() = getter()

    // added to demonstrate that we can use "val" property syntax as well
    val message get() = getter<HelloWorldMessage>()  // calling the inline function getter, see the code below

    fun printGreeting() {
        printer.print(message.text())
    }

    companion object : Def()

    @Component
    open class Def {
        @Bean("helloWorldService")
        open operator fun invoke(msg: HelloWorldMessage, printer: Printer): HelloWorldService =
                object: HelloWorldService{}.postInit<HelloWorldService> {
                    set(::message, msg) // added to demonstrate that we can use "val" property syntax as well, and we can initialize "val" property in initializer
                    this.printer = printer //  added to demonstrate that we can use "var" property syntax as well
                }
    }

}

inline fun <T> KtLazyExt.getter() = get<T>(null, {it.toFieldName()}){ illegal() }
inline fun <T> KtLazyExt.getter(crossinline f: Sp<T>) = calc( null, {it.toFieldName()}, null) {f()} // if we need a default value
inline fun <T> KtLazyExt.setter(f: T) { calc({f}, {it.toFieldName()}){ illegal() } }
inline fun String.toFieldName(): String = substring(3).replaceFirstChar{it.lowercaseChar()}