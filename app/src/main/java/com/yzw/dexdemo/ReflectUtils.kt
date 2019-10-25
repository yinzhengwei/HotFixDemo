package com.yzw.dexdemo

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Create by yinzhengwei on 2019-10-15
 *
 * @Function
 */
object ReflectUtils {


    /**
     * 反射获取一个类中的字段
     */
    fun getField(`object`: Any, fieldName: String): Field? {
        var clz: Class<*>? = `object`.javaClass
        while (clz != null) {
            var field: Field? = null
            try {
                field = clz.getDeclaredField(fieldName)

                //设置权限
                field!!.isAccessible = true

                return field
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }

            clz = clz.superclass
        }
        return null
    }

    /**
     * 反射获取一个类中的字段
     */
    fun getMethod(`object`: Any, methodName: String, vararg paramsTypes: Class<*>): Method? {
        var clz: Class<*>? = `object`.javaClass
        while (clz != null) {
            var method: Method? = null
            try {
                method = clz.getDeclaredMethod(methodName, *paramsTypes)

                //设置权限
                method!!.isAccessible = true

                return method
            } catch (e: Exception) {
                e.printStackTrace()
            }

            clz = clz.superclass
        }
        return null
    }
}
