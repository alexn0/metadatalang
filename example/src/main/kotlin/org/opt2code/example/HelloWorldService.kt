/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.*
import org.springframework.context.annotation.Bean
import kotlin.reflect.KProperty0

import org.springframework.stereotype.Component

// It is recommended to redefine KtLazy so you can redefine and add convenience methods.
interface KtLazyExt: KtLazy {
    fun <T> set(prop: KProperty0<T>, g: T) {
        val getterName = "get" + prop.name.replaceFirstChar{it.uppercase()}
        map().put(getterName, g)
    }

    // this function is needed here not for beauty. if use "get<T> { throw IllegalArgumentException() }" instead of "get<T> { illegal() }",
    // the code will be optimized to throw IllegalArgumentException always! (it looks like a bug of kotlin compiler I use)
    fun <T> illegal(): T {
        throw IllegalArgumentException()
    }
}

inline fun <T> KtLazyExt.get() = get<T> { illegal() }

interface HelloWorldService : KtLazyExt {
    fun printer(f: Sp<Printer>? = null): Printer = calc(f) { throw IllegalArgumentException() }
    // added to demonstrate that we can use property syntax as well
    val message get(): HelloWorldMessage = get()  // calling the inline function get, see the code above

    fun printGreeting() {
        printer().print(message.text())
    }

    companion object : Def()

    @Component
    open class Def {
        @Bean("helloWorldService")
        open operator fun invoke(msg: HelloWorldMessage, printer: Printer): HelloWorldService =
                object : HelloWorldService {}.postInit<HelloWorldService> {
                    set(::message, msg) // added to demonstrate that we can use property syntax as well
                    printer { printer } // a standard way for setting
                }
    }

}