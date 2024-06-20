/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example.demo

import org.opt2code.code.postInit
import org.opt2code.example.Printer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface HelloWorldMessagesService : HelloWorldMessage1, HelloWorldMessage2, HelloWorldMessage3, Printer {

    fun print() {
        print(text1())
        print(text2())
        print(text3())
    }

    class O() : HelloWorldMessagesService

    companion object : Def()

    @Component
    open class Def {

        @Bean("helloWorldMessagesService")
        open operator fun invoke(
                m1: HelloWorldMessage1,
                m2: HelloWorldMessage2,
                m3: HelloWorldMessage3,
                p: Printer
        ): HelloWorldMessagesService = O().postInit<HelloWorldMessagesService> {
            m1.init()?.invoke(this)
            m2.init()?.invoke(this)
            m3.init()?.invoke(this)
            p.init()?.invoke(this)
        }

    }

}