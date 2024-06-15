/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.code.pp

import org.junit.Assert
import org.junit.Test
import org.opt2code.code.*

private typealias Sp<T> = Function1<T, T>

class CopyingForPpFunctionTest {

    inline fun StringFieldProperty.fieldCall(noinline f: Sp<String?>? = null): String? { return fieldBackedByPp(f)}

    inline fun StringFieldProperty.getCall(noinline f: Sp<String?>? = null): String? { return pp(f) { null }}

    inline fun TestData<*>.isSet(name: String) = r.propSet(name)

    inline fun KtLazy.isSet(name: String) = propSet(name)

    val fieldName = StringFieldProperty::fieldBackedByPp.name

    /*
      Below should be identical for files CopyingFor***FunctionTest.kt
     */


    @Test
    fun testPropValue() {
        testDataForByFunc().forEach { x ->
            val r = x.r
            Assert.assertEquals(null, r.fieldCall())
            r.fieldCall { "test" }
            Assert.assertEquals("test", r.fieldCall())
        }
    }

    @Test
    fun testFunctionBy() {
        testDataForByFunc().forEach { x ->
            val r: StringFieldProperty = x.r
            Assert.assertEquals(null, r.fieldCall())
            r.fieldCall { "test" }
            Assert.assertTrue(x.isSet(fieldName))
            val z = x.f(r)
            Assert.assertEquals("test", z.fieldCall())
            Assert.assertTrue(z.isSet(fieldName))
            r.fieldCall { "test2" }
            Assert.assertEquals("test2", z.fieldCall()) // value is updated
        }
    }


    @Test
    fun testFunctionOf() {
        testDataForOfFunc().forEach { x ->
            val r = x.r
            Assert.assertEquals(null, r.fieldCall())
            r.fieldCall { "test" }
            Assert.assertTrue(x.isSet(fieldName))
            val z = x.f(r)
            Assert.assertTrue(z.isSet(fieldName))
            Assert.assertEquals("test", z.fieldCall())
            r.fieldCall { "test2" }
            Assert.assertNotEquals(r.map(), z.map())
            Assert.assertEquals("test", z.fieldCall()) // value is not updated
        }
    }


    @Test
    fun testFunctionByIfLocalExtensionFunctionIsCalled() {
        fun StringFieldProperty.domain(f: Sp<String?>? = null): String? = getCall(f)
        testDataForByFunc().forEach { x ->
            val r = x.r
            Assert.assertEquals(null, r.domain())
            r.domain { "test" }
            Assert.assertTrue(x.isSet("domain"))
            val z = x.f(r)
            Assert.assertTrue(z.isSet("domain"))
            Assert.assertEquals("test", z.domain())
            r.domain { "test2" }
            Assert.assertEquals("test2", z.domain()) // value is  updated
        }
    }


    @Test
    fun testFunctionByIfLocalExtensionFunctionIsCalled2() {
        fun xx() {
            fun StringFieldProperty.domain(f: Sp<String?>? = null): String? = getCall(f)
            testDataForByFunc().forEach { x ->
                val r = x.r
                Assert.assertEquals(null, r.domain())
                r.domain { "test" }
                Assert.assertTrue(x.isSet("domain"))
                val z = x.f(r)
                Assert.assertTrue(z.isSet("domain"))
                Assert.assertEquals("test", z.domain())
                r.domain { "test2" }
                Assert.assertEquals("test2", z.domain()) // value is  updated
            }
        }
        xx()
    }

    @Test
    fun testFunctionByIfExternalExtensionFunctionIsCalled() {
        testDataForByFunc().forEach { x ->
            val r = x.r
            Assert.assertEquals(null, r.domain())
            r.domain { "test" }
            Assert.assertTrue(x.isSet("domain"))
            val z = x.f(r)
            Assert.assertTrue(z.isSet("domain"))
            Assert.assertEquals("test", z.domain())
            r.domain { "test2" }
            Assert.assertEquals("test2", z.domain()) // value is  updated
        }
    }



    @Test
    fun testFunctionOfIfLocalExtensionFunctionIsCalled() {
        fun StringFieldProperty.domain(f: Sp<String?>? = null): String? = getCall(f)
        testDataForOfFunc().forEach { x ->
            val r = x.r
            Assert.assertEquals(null, r.domain())
            r.domain { "test" }
            Assert.assertTrue(x.isSet("domain"))
            val z = x.f(r)
            Assert.assertTrue(z.isSet("domain"))
            Assert.assertEquals("test", z.domain())
            r.domain { "test2" }
            Assert.assertEquals("test", z.domain()) // value is not updated
        }
    }

    fun StringFieldProperty.domain(f: Sp<String?>? = null): String? = getCall(f)

    @Test
    fun testFunctionOfIfExternalExtensionFunctionIsCalled() {
        testDataForOfFunc().forEach { checkThatValueIsNotPropagated(it) }
    }

    private fun checkThatValueIsNotPropagated(x: TestData<out StringFieldProperty>) {
        val r = x.r
        Assert.assertEquals(null, r.domain())
        r.domain { "test" }
        Assert.assertTrue(x.isSet("domain"))
        val z = x.f(r)
        Assert.assertTrue(z.isSet("domain"))
        Assert.assertEquals("test", z.domain())
        r.domain { "test2" }
        Assert.assertEquals("test", z.domain()) // value is not updated
    }


}

