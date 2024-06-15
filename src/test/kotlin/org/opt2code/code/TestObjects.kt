/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code

import org.opt2code.code.util.instance


class TestData<T: KtLazy>(val r: T, val f: (KtLazy) -> T)


interface StringFieldProperty: KtLazy {
    fun field(f: Sp<String?>? = null): String?

    fun fieldBackedByGet(f: Fn<String?>? = null): String?

    fun fieldBackedByPp(f: Fn<String?>? = null): String?

    fun fieldBackedByPup(f: Sp<String?>? = null): String?
}


interface Common: StringFieldProperty {
    class O: Common

    override fun field(f: Sp<String?>?): String? = calc (f) { null }

    override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

    override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

    override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }

    companion object {
        operator fun invoke() = O().apply {}
    }
}


interface CommonPrivate: StringFieldProperty {
    private class O: CommonPrivate

    override fun field(f: Sp<String?>?): String? = calc (f) { null }

    override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

    override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

    override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }

    companion object {
        operator fun invoke(): CommonPrivate = O().apply {}
    }
}


interface CommonS: StringFieldProperty {

    class S(): CommonS {
        override fun field(f: Sp<String?>?): String?  = calc (f) { null }

        override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

        override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

        override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }
    }

    companion object {
        operator fun invoke() = S()
    }
}


interface CommonL: StringFieldProperty {

    class L(f: Boolean): CommonL{
        override fun field(f: Sp<String?>?): String? = calc (f) { null }

        override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

        override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

        override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }
    }

    companion object {
        operator fun invoke() = L(true)
    }
}


interface CommonO: StringFieldProperty {
    override fun field(f: Sp<String?>?): String? = calc(f) { null }

    override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

    override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

    override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }

    companion object {
        operator fun invoke() = object: CommonO {}
    }
}



interface CommonE: StringFieldProperty {

    class O: KtLazy.E(), CommonE

    override fun field(f: Sp<String?>?): String? = calc(f) { null }

    override fun fieldBackedByGet(f: Fn<String?>?): String? = get (f) { null }

    override fun fieldBackedByPp(f: Fn<String?>?): String? = pp (f) { null }

    override fun fieldBackedByPup(f: Sp<String?>?): String? = pup (f) { null }

    companion object {
        operator fun invoke() = O().apply {}
    }
}


fun testDataForByFunc() = arrayOf(
        TestData<CommonL>(instance<CommonL>(CommonL::class.java, CommonL())) {it.byInstance()},
        TestData<CommonL>(instance<CommonL>(CommonL::class.java, CommonL()).byInstance()) {it.byInstance()},

        TestData<CommonS>(instance<CommonS>(CommonS::class.java, CommonS())) {it.byInstance()},
        TestData<CommonS>(instance<CommonS>(CommonS::class.java, CommonS()).byInstance()) {it.byInstance()},

        TestData<CommonO>(instance<CommonO>(CommonO::class.java)) {it.byInstance()},
        TestData<CommonO>(instance<CommonO>(CommonO::class.java).byInstance()) {it.byInstance()},
        TestData<CommonO>(instance<CommonO>(CommonO::class.java)) { instance<CommonO>(CommonO::class.java).by(it)},

        TestData<CommonO>(object: CommonO {}) {it.byInstance()},
        TestData<CommonO>(object: CommonO {}) { instance<CommonO>(CommonO::class.java).by(it)},

        TestData<CommonE>(CommonE()){ it.byInstance() },
        TestData<CommonE.O>(CommonE()){ it.byInstance() },
        TestData<CommonE.O>(CommonE()){ CommonE().by(it) },
        TestData<CommonE>(CommonE()){ CommonE().by(it) },

        TestData<CommonPrivate>(CommonPrivate()){ CommonPrivate().by(it) },
        TestData<CommonPrivate>(CommonPrivate()){ it.byInstance() },

        TestData<Common>(Common()){ Common().by(it) },
        TestData<Common>(Common()){ it.byInstance() },
        TestData<Common.O>(Common()){ it.byInstance() },
        TestData<Common.O>(Common()){ Common().by(it) },
)



fun testDataForOfFunc() = arrayOf(
        TestData<CommonL>(instance<CommonL>(CommonL::class.java, CommonL())) {it.ofInstance()},
        TestData<CommonL>(instance<CommonL>(CommonL::class.java, CommonL()).ofInstance()) {it.ofInstance()},

        TestData<CommonS>(instance<CommonS>(CommonS::class.java, CommonS())) {it.ofInstance()},
        TestData<CommonS>(instance<CommonS>(CommonS::class.java, CommonS()).ofInstance()) {it.ofInstance()},

        TestData<CommonO>(instance<CommonO>(CommonO::class.java)) {it.ofInstance()},
        TestData<CommonO>(instance<CommonO>(CommonO::class.java).ofInstance()) {it.ofInstance()},
        TestData<CommonO>(instance<CommonO>(CommonO::class.java)) { instance<CommonO>(CommonO::class.java).of(it)},

        TestData<CommonO>(object: CommonO {}) {it.ofInstance()},
        TestData<CommonO>(object: CommonO {}) { instance<CommonO>(CommonO::class.java).of(it)},

        TestData<CommonE>(CommonE()){ it.ofInstance() },
        TestData<CommonE.O>(CommonE()){ it.ofInstance() },
        TestData<CommonE.O>(CommonE()){ CommonE().of(it) },
        TestData<CommonE>(CommonE()){ CommonE().of(it) },

        TestData<CommonPrivate>(CommonPrivate()){ CommonPrivate().of(it) },
        TestData<CommonPrivate>(CommonPrivate()){ it.ofInstance() },

        TestData<Common>(Common()){ Common().of(it) },
        TestData<Common>(Common()){ it.ofInstance() },
        TestData<Common.O>(Common()){ it.ofInstance() },
        TestData<Common.O>(Common()){ Common().of(it) },
)