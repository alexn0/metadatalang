/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example.demo

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

interface HelloWorldMessage2: KtLazy {

	fun text2(f: Sp<String>? = null) = calc(f) { "" }


	class O: HelloWorldMessage2


	@Component
	open class Def {
		@Bean
		open fun helloWorldMessage2(): HelloWorldMessage2 = O().postInit<HelloWorldMessage2>{
			text2{"Hello world2"}
		}
	}

}