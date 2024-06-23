/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.*
import org.springframework.context.annotation.Bean
import org.opt2code.example.KtLazyExtService.Private
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Component

interface KtLazyExtService: KtLazyExt {
    object Private

    fun Private.factory(f: Sp<BeanFactory>? = null): BeanFactory = calc(f) { illegal()}
}

inline fun <reified T> KtLazyExtService.init(hint: String? = null) = accessorGet<T>(null, {it.toFieldName()}, hint){
    Private.factory().getBean(T::class.java)
}

interface HelloWorldContextService : KtLazyExtService {

    var printer set(f) = setter(f); get() = init<Printer>()

    val message get() = init<HelloWorldMessage>()

    fun printGreeting() {
        printer.print(message.text())
    }

    @Component
    open class Def {
        @Bean
        open fun helloWorldContext(factory: BeanFactory) = object: HelloWorldContextService{}
                .postInit<HelloWorldContextService> { Private.factory{ factory } }
    }

}