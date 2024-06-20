/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit
import org.springframework.context.annotation.Bean

import org.springframework.stereotype.Component

interface HelloWorld : KtLazy {

    fun printer(f: Sp<Printer>? = null): Printer = calc(f) { throw IllegalArgumentException() }

    fun message(f: Sp<HelloWorldMessage>? = null): HelloWorldMessage = calc(f) { throw IllegalArgumentException() }

    fun printGreeting() {
        printer().print(message().text())
    }

    companion object : Def()

    @Component
    open class Def {
        @Bean("helloWorld")
        open operator fun invoke(msg: HelloWorldMessage, printer: Printer): HelloWorld =
                object : HelloWorld {}.postInit<HelloWorld> {
                    message { msg }
                    printer { printer }
                }
    }

}