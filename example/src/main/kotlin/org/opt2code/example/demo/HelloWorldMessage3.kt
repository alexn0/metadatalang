/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example.demo

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface HelloWorldMessage3: KtLazy {

	fun text3(f: Sp<String>? = null) = calc(f) { "" }

	private class O(): HelloWorldMessage3

	@Component
	open class Def {

		@Bean
		open fun helloWorldMessage3() = O().postInit<HelloWorldMessage3>{
			text3{"Hello world3"}
		}
	}

}