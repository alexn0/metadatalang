/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example.demo

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface HelloWorldMessage1: KtLazy {

	fun text1(f: Sp<String>? = null) = calc(f) { "" }


	class O: HelloWorldMessage1


	@Component
	open class Def {
		@Bean
		open fun helloWorldMessage1(): HelloWorldMessage1 = O().postInit<HelloWorldMessage1>{
			text1{"Hello world1"}
		}
	}

}