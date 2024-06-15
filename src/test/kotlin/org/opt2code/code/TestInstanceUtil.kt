/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code

import org.opt2code.code.util.byInstance
import org.opt2code.code.util.ofInstance

inline fun <reified T: KtLazy> KtLazy.byInstance(): T = (this as T).byInstance(T::class.java)
inline fun <reified T: KtLazy> KtLazy.ofInstance(): T = (this as T).ofInstance(T::class.java)