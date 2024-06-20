/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface HelloWorldMessage {

    fun text() = "Hello world"


    class O : HelloWorldMessage

    companion object : Def()

    @Component
    open class Def {
        @Bean("helloWorldMessage")
        open operator fun invoke(): HelloWorldMessage = O()
    }

}


