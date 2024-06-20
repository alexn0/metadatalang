/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.KtLazy
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface Printer : KtLazy {

    fun print(message: String) = println(message)

    class O : Printer

    companion object : Def()

    @Component
    open class Def {
        @Bean("printer")
        open operator fun invoke(): Printer = O()
    }


}

