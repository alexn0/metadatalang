/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code

import org.junit.Assert
import org.junit.Test
import org.opt2code.code.util.instance


interface CalcSimpleInstance: KtLazy {
    fun value(f: Sp<Int>? = null): Int = calc(f, "v") { 0 }

    fun sum(instance: CalcSimpleInstance): Int

    companion object {
        operator fun invoke(number: Int? = null) = object : CalcSimpleInstance {

        override fun sum(instance: CalcSimpleInstance): Int = value() + instance.value()

        }.postInit<CalcSimpleInstance> {
            number?.let { value { it } }
        }
    }
}


interface GetSimpleInstance: KtLazy {
    fun value(f: Fn<Int>? = null): Int = get(f) { 0 }

    private class O: GetSimpleInstance

    companion object {
        operator fun invoke(number: Int? = null) = O().postInit<GetSimpleInstance> {
            number?.let { value { number } }
        }
    }
}


interface PupSimpleInstance: KtLazy {
    fun value(f: Sp<Int>? = null): Int = pup(f) { 0 }

    fun sum(instance: PupSimpleInstance): Int

    private class O: KtLazy.E(), PupSimpleInstance{
        override fun sum(instance: PupSimpleInstance): Int = value() + instance.value()
    }



    companion object {
        operator fun invoke(number: Int? = null) = O().postInit<PupSimpleInstance> {
            number?.let { value { number } }
        }
    }
}


interface PpSimpleInstance: KtLazy {
    fun value(f: Fn<Int>? = null): Int = pp(f) { 0 }

    private class O: KtLazy.E(), PpSimpleInstance

    companion object {
        operator fun invoke(number: Int? = null) = O().postInit<PpSimpleInstance> {
            number?.let { value { number } }
        }
    }
}


class KtLazyTest {

    @Test
    fun testCalcInitialization(){
        val s = CalcSimpleInstance()
        Assert.assertTrue(s.map().size == 1)
        Assert.assertTrue(s.fieldSet("#init"))
        // checking that properties are not initialized
        Assert.assertTrue(!s.propsSet())
        Assert.assertTrue(!s.fieldSet("#pp"))

        // properties initialization by calling the props function
        Assert.assertTrue(s.props().isEmpty())
        Assert.assertTrue(s.map().size == 2)
        Assert.assertTrue(s.fieldSet("#pp"))

        Assert.assertEquals(0, s.value())
        Assert.assertTrue(!s.fieldSet("value"))
        s.value { 1 }
        Assert.assertTrue(s.fieldSet("value"))
        Assert.assertEquals(1, s.value())

        //
        val s2 = instance(CalcSimpleInstance::class.java, s)
        Assert.assertEquals(0, s2.value())
        Assert.assertTrue(!s2.fieldSet("value"))

        val s3 = s.postInit(null)
        Assert.assertEquals(0, s3.value())
        Assert.assertTrue(!s3.fieldSet("value"))



        val s4 = CalcSimpleInstance(5)
        Assert.assertTrue(s4.fieldSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        Assert.assertEquals(5, s4.value())
        s4.value { 3 }
        Assert.assertEquals(3, s4.value())
        Assert.assertTrue(s4.fieldSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        val s5 = instance(CalcSimpleInstance::class.java, s4)
        Assert.assertEquals(5, s5.value())
        Assert.assertTrue(s5.fieldSet("value"))

        val s6 = s4.postInit()
        Assert.assertEquals(5, s6.value())
        Assert.assertTrue(s6.fieldSet("value"))

        val s7 = s4.postInit(CalcSimpleInstance::class.java)
        Assert.assertEquals(5, s7.value())
        Assert.assertTrue(s7.fieldSet("value"))

        val s8 = instance(CalcSimpleInstance::class.java, s4, false)
        Assert.assertEquals(0, s8.value())
        Assert.assertTrue(!s8.fieldSet("value"))

        val s9 = CalcSimpleInstance(9)
        Assert.assertEquals(14, s9.sum(s6))

        val s10 = object : CalcSimpleInstance {
            override fun sum(instance: CalcSimpleInstance): Int = value() + instance.value()
        }
        s10.value { 5 }
        Assert.assertEquals(14, s9.sum(s10))

        val s11 = instance(CalcSimpleInstance::class.java, s10, false, true)
        s11.value { 9 }
        // unsafe since p<CalcSimpleInstance> was not called during construction of s10 object
        Assert.assertEquals(18, s11.sum(s10))
        Assert.assertEquals(18, s11.sum(s10.of(s10)))
        Assert.assertEquals(18, s11.sum(s10.by(s10)))
        // safe
        Assert.assertEquals(14, s11.sum(s10.byInstance()))
        Assert.assertEquals(14, s11.sum(s10.ofInstance()))
        Assert.assertEquals(14, s11.sum(s10.postInit<CalcSimpleInstance>{ value { 5 }}))


        s.clear()
        s2.clear()
        s3.clear()
        s4.clear()
        s5.clear()
        s6.clear()
        s7.clear()
        Assert.assertEquals(0, s.value())
        Assert.assertEquals(0, s2.value())
        Assert.assertEquals(0, s3.value())
        Assert.assertEquals(0, s4.value())
        Assert.assertEquals(0, s5.value())
        Assert.assertEquals(0, s6.value())
        Assert.assertEquals(0, s7.value())
        Assert.assertTrue(s.map().isEmpty())
        Assert.assertTrue(s2.map().isEmpty())
        Assert.assertTrue(s3.map().isEmpty())
        Assert.assertTrue(s4.map().isEmpty())
        Assert.assertTrue(s5.map().isEmpty())
        Assert.assertTrue(s6.map().isEmpty())
        Assert.assertTrue(s7.map().isEmpty())




    }


    @Test
    fun testGetInitialization(){
        val s = GetSimpleInstance()
        Assert.assertTrue(s.map().size == 1)
        Assert.assertTrue(s.fieldSet("#init"))
        // checking that properties are not initialized
        Assert.assertTrue(!s.propsSet())
        Assert.assertTrue(!s.fieldSet("#pp"))

        // properties initialization by calling the props function
        Assert.assertTrue(s.props().isEmpty())
        Assert.assertTrue(s.map().size == 2)
        Assert.assertTrue(s.fieldSet("#pp"))

        Assert.assertEquals(0, s.value())
        Assert.assertTrue(s.fieldSet("value"))
        s.value { 1 }
        Assert.assertTrue(s.fieldSet("value"))
        Assert.assertEquals(1, s.value())

        //
        val s2 = instance(GetSimpleInstance::class.java, s)
        Assert.assertEquals(0, s2.value())
        Assert.assertTrue(s2.fieldSet("value"))

        val s3 = s.postInit(null)
        Assert.assertEquals(0, s3.value())
        Assert.assertTrue(s3.fieldSet("value"))



        val s4 = GetSimpleInstance(5)
        Assert.assertTrue(s4.fieldSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        Assert.assertEquals(5, s4.value())
        s4.value { 3 }
        Assert.assertEquals(3, s4.value())
        Assert.assertTrue(s4.fieldSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        val s5 = instance(GetSimpleInstance::class.java, s4)
        Assert.assertEquals(5, s5.value())
        Assert.assertTrue(s5.fieldSet("value"))

        val s6 = s4.postInit()
        Assert.assertEquals(5, s6.value())
        Assert.assertTrue(s6.fieldSet("value"))

        val s7 = s4.postInit(GetSimpleInstance::class.java)
        Assert.assertEquals(5, s7.value())
        Assert.assertTrue(s7.fieldSet("value"))

        val s8 = instance(GetSimpleInstance::class.java, s4, false)
        Assert.assertEquals(0, s8.value())
        Assert.assertTrue(s8.fieldSet("value"))



        s.clear()
        s2.clear()
        s3.clear()
        s4.clear()
        s5.clear()
        s6.clear()
        s7.clear()
        Assert.assertTrue(s.map().isEmpty())
        Assert.assertTrue(s2.map().isEmpty())
        Assert.assertTrue(s3.map().isEmpty())
        Assert.assertTrue(s4.map().isEmpty())
        Assert.assertTrue(s5.map().isEmpty())
        Assert.assertTrue(s6.map().isEmpty())
        Assert.assertTrue(s7.map().isEmpty())
        Assert.assertEquals(0, s.value())
        Assert.assertEquals(0, s2.value())
        Assert.assertEquals(0, s3.value())
        Assert.assertEquals(0, s4.value())
        Assert.assertEquals(0, s5.value())
        Assert.assertEquals(0, s6.value())
        Assert.assertEquals(0, s7.value())
        Assert.assertTrue(!s.map().isEmpty())
        Assert.assertTrue(!s2.map().isEmpty())
        Assert.assertTrue(!s3.map().isEmpty())
        Assert.assertTrue(!s4.map().isEmpty())
        Assert.assertTrue(!s5.map().isEmpty())
        Assert.assertTrue(!s6.map().isEmpty())
        Assert.assertTrue(!s7.map().isEmpty())
    }

    @Test
    fun testPupInitialization(){
        val s = PupSimpleInstance()
        Assert.assertTrue(s.map().size == 1)
        Assert.assertTrue(s.fieldSet("#init"))
        // checking that properties are not initialized
        Assert.assertTrue(!s.propsSet())
        Assert.assertTrue(!s.fieldSet("#pp"))

        // properties initialization by calling the props function
        Assert.assertTrue(s.props().isEmpty())
        Assert.assertTrue(s.map().size == 2)
        Assert.assertTrue(s.fieldSet("#pp"))

        Assert.assertEquals(0, s.value())
        Assert.assertTrue(!s.propSet("value"))
        s.value { 1 }
        Assert.assertTrue(s.propSet("value"))
        Assert.assertEquals(1, s.value())

        //
        val s2 = instance(PupSimpleInstance::class.java, s)
        Assert.assertEquals(0, s2.value())
        Assert.assertTrue(!s2.propSet("value"))

        val s3 = s.postInit(null)
        Assert.assertEquals(1, s3.value())
        Assert.assertTrue(s3.propSet("value"))



        val s4 = PupSimpleInstance(5)
        Assert.assertTrue(s4.propSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        Assert.assertEquals(5, s4.value())
        s4.value { 3 }
        Assert.assertEquals(3, s4.value())
        Assert.assertTrue(s4.propSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        val s5 = instance(PupSimpleInstance::class.java, s4)
        Assert.assertEquals(5, s5.value())
        Assert.assertTrue(s5.propSet("value"))

        val s6 = s4.postInit()
        Assert.assertEquals(5, s6.value())
        Assert.assertTrue(s6.propSet("value"))

        val s7 = s4.postInit(PupSimpleInstance::class.java)
        Assert.assertEquals(5, s7.value())
        Assert.assertTrue(s7.propSet("value"))

        val s8 = instance(PupSimpleInstance::class.java, s4, false)
        Assert.assertEquals(0, s8.value())
        Assert.assertTrue(!s8.propSet("value"))

        val s9 = PupSimpleInstance(9)
        Assert.assertEquals(14, s9.sum(s6))


        val s10 = object : PupSimpleInstance {
            override fun sum(instance: PupSimpleInstance): Int = value() + instance.value()
        }
        s10.value { 5 }
        Assert.assertEquals(14, s9.sum(s10))

        val s11 = instance(PupSimpleInstance::class.java, s10, false)
        s11.value { 9 }
        // safe
        Assert.assertEquals(14, s11.sum(s10))
        Assert.assertEquals(14, s11.sum(s10.of(s10)))
        Assert.assertEquals(14, s11.sum(s10.by(s10)))
        // safe
        Assert.assertEquals(14, s11.sum(s10.byInstance()))
        Assert.assertEquals(14, s11.sum(s10.ofInstance()))
        Assert.assertEquals(14, s11.sum(s10.postInit<PupSimpleInstance>{ value { 5 }}))



        s.clear()
        s2.clear()
        s3.clear()
        s4.clear()
        s5.clear()
        s6.clear()
        s7.clear()
        Assert.assertTrue(s.map().isEmpty())
        Assert.assertTrue(s2.map().isEmpty())
        Assert.assertTrue(s3.map().isEmpty())
        Assert.assertTrue(s4.map().isEmpty())
        Assert.assertTrue(s5.map().isEmpty())
        Assert.assertTrue(s6.map().isEmpty())
        Assert.assertTrue(s7.map().isEmpty())
        Assert.assertEquals(0, s.value())
        Assert.assertEquals(0, s2.value())
        Assert.assertEquals(0, s3.value())
        Assert.assertEquals(0, s4.value())
        Assert.assertEquals(0, s5.value())
        Assert.assertEquals(0, s6.value())
        Assert.assertEquals(0, s7.value())
        Assert.assertTrue(!s.map().isEmpty())
        Assert.assertTrue(!s2.map().isEmpty())
        Assert.assertTrue(!s3.map().isEmpty())
        Assert.assertTrue(!s4.map().isEmpty())
        Assert.assertTrue(!s5.map().isEmpty())
        Assert.assertTrue(!s6.map().isEmpty())
        Assert.assertTrue(!s7.map().isEmpty())

    }


    @Test
    fun testPpInitialization(){
        val s = PpSimpleInstance()
        Assert.assertTrue(s.map().size == 1)
        Assert.assertTrue(s.fieldSet("#init"))
        // checking that properties are not initialized
        Assert.assertTrue(!s.propsSet())
        Assert.assertTrue(!s.fieldSet("#pp"))

        // properties initialization by calling the props function
        Assert.assertTrue(s.props().isEmpty())
        Assert.assertTrue(s.map().size == 2)
        Assert.assertTrue(s.fieldSet("#pp"))

        Assert.assertTrue(!s.propSet("value"))
        Assert.assertEquals(0, s.value())
        Assert.assertTrue(s.propSet("value"))
        s.value { 1 }
        Assert.assertTrue(s.propSet("value"))
        Assert.assertEquals(1, s.value())

        //
        val s2 = instance(PpSimpleInstance::class.java, s)
        Assert.assertTrue(!s2.propSet("value"))
        Assert.assertEquals(0, s2.value())
        Assert.assertTrue(s2.propSet("value"))

        val s3 = s.postInit(null)
        Assert.assertTrue(s3.propSet("value"))
        Assert.assertEquals(1, s3.value())
        Assert.assertTrue(s3.propSet("value"))



        val s4 = PpSimpleInstance(5)
        Assert.assertTrue(s4.propSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        Assert.assertEquals(5, s4.value())
        s4.value { 3 }
        Assert.assertEquals(3, s4.value())
        Assert.assertTrue(s4.propSet("value"))
        Assert.assertTrue(s4.map().isNotEmpty())

        val s5 = instance(PpSimpleInstance::class.java, s4)
        Assert.assertTrue(s5 is KtLazy.E)
        Assert.assertEquals(5, s5.value())
        Assert.assertTrue(s5.propSet("value"))

        Assert.assertEquals(3, s4.value())
        val s6 = s4.postInit()
        Assert.assertTrue(s6 is KtLazy.E)
        Assert.assertEquals(s4, s6)
        Assert.assertEquals(5, s6.value())
        Assert.assertTrue(s6.propSet("value"))

        val s7 = s4.postInit(PpSimpleInstance::class.java)
        Assert.assertTrue(s7 is KtLazy.E)
        Assert.assertEquals(s4, s7)

        val s8 = instance(PpSimpleInstance::class.java, s4, false)
        Assert.assertTrue(s8 is KtLazy.E)
        Assert.assertTrue(!s8.propSet("value"))
        Assert.assertEquals(0, s8.value())
        Assert.assertTrue(s8.propSet("value"))



        s.clear()
        s2.clear()
        s3.clear()
        s4.clear()
        s5.clear()
        s6.clear()
        s7.clear()
        Assert.assertTrue(s.map().isEmpty())
        Assert.assertTrue(s2.map().isEmpty())
        Assert.assertTrue(s3.map().isEmpty())
        Assert.assertTrue(s4.map().isEmpty())
        Assert.assertTrue(s5.map().isEmpty())
        Assert.assertTrue(s6.map().isEmpty())
        Assert.assertTrue(s7.map().isEmpty())
        Assert.assertEquals(0, s.value())
        Assert.assertEquals(0, s2.value())
        Assert.assertEquals(0, s3.value())
        Assert.assertEquals(0, s4.value())
        Assert.assertEquals(0, s5.value())
        Assert.assertEquals(0, s6.value())
        Assert.assertEquals(0, s7.value())
        Assert.assertTrue(!s.map().isEmpty())
        Assert.assertTrue(!s2.map().isEmpty())
        Assert.assertTrue(!s3.map().isEmpty())
        Assert.assertTrue(!s4.map().isEmpty())
        Assert.assertTrue(!s5.map().isEmpty())
        Assert.assertTrue(!s6.map().isEmpty())
        Assert.assertTrue(!s7.map().isEmpty())

    }

}