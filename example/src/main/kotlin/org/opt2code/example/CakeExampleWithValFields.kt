/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example.cakewithvalfields

import org.opt2code.code.KtLazy
import org.opt2code.code.Sp
import org.opt2code.code.postInit

interface AComponent : BHolder, CHolder
interface AHolder : KtLazy {
    val aService: AService
}

interface AService : AComponent {
    fun AService.reg(f: Sp<AComponent>? = null): AComponent = calc(f) { throw IllegalArgumentException() }
    override val bService get() = reg().bService
    override val cService get() = reg().cService

    //your service methods

    private class O : AService

    companion object {
        operator fun invoke(r: AComponent) = O().postInit<AService> {
            reg { r }
        }
    }
}


interface BComponent : AHolder, CHolder
interface BHolder : KtLazy {
    val bService: BService
}

interface BService : BComponent {
    fun BService.reg(f: Sp<BComponent>? = null): BComponent = calc(f) { throw IllegalArgumentException() }
    override val aService get() = reg().aService
    override val cService get() = reg().cService

    //your service methods

    private class O : BService

    companion object {
        operator fun invoke(r: BComponent) = O().postInit<BService> {
            reg { r }
        }
    }
}

// CService dependencies are defined here
interface CComponent : BHolder, AHolder
interface CHolder : KtLazy {
    val cService: CService
}

interface CService : CComponent {
    fun CService.reg(f: Sp<CComponent>? = null): CComponent = calc(f) { throw IllegalArgumentException() }
    override val aService get() = reg().aService
    override val bService get()= reg().bService

    //your service methods

    private class O : CService

    companion object {
        operator fun invoke(r: CComponent) = O().postInit<CService> {
            reg { r }
        }
    }
}


interface Registry : AComponent, BComponent, CComponent {
    override val aService get() = get { AService(this) }
    override val bService get() = get { BService(this) }
    override val cService get() = get { CService(this) }

    private class O : Registry

    companion object {
        operator fun invoke() = O().postInit<Registry>()
    }
}
