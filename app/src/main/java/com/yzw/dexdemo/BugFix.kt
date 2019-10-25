package com.yzw.dexdemo

import android.content.Context
import android.os.Build
import java.io.*
import java.lang.Exception
import java.util.ArrayList

/**
 * Create by yinzhengwei on 2019-10-15
 * @Function
 */
object BugFix {

    /**
     * 执行修复
     */
    fun installPatch(context: Context, patchPath: String) {

        //pathClassLoader
        val classLoader = context.classLoader

        //pathList
        val pathListField = ReflectUtils.getField(classLoader, "pathList") ?: return
        //DexPathList
        val pathList = pathListField.get(classLoader)
        //Element[]
        val dexElementsField = ReflectUtils.getField(pathList, "dexElements") ?: return
        val oldDexElements = dexElementsField.get(pathList) as Array<*>

        /**
         * 这里需要兼容不同版本的makeDexElements
         */
        val clxType = makeDexElementParamsType()

        val makeDexElements =
            ReflectUtils.getMethod(
                pathList,
                makeDexElementMethodName(),
                clxType,
                File::class.java,
                clxType
            )
                ?: return
        val newDexElements = makeDexElements.invoke(
            null,
            arrayListOf(copyFile(context), File(patchPath)),
            //这个参数是opt(dex优化后的odex)目录必须是私有目录
            context.cacheDir,
            arrayListOf<IOException>()
        ) as Array<*>


        //要替换系统原本的Element数组的新空数组
        val replaceElement = java.lang.reflect.Array.newInstance(
            oldDexElements.javaClass.componentType,
            oldDexElements.size + newDexElements.size
        )

        //从newDexElements拷贝到replaceElement里
        System.arraycopy(newDexElements, 0, replaceElement, 0, newDexElements.size)
        //从oldDexElements也拷贝到replaceElement里，起点从newDexElements.size开始
        System.arraycopy(
            oldDexElements,
            0,
            replaceElement,
            newDexElements.size,
            oldDexElements.size
        )

        dexElementsField.isAccessible = true
        //新的element替换立马替换老的element数组
        dexElementsField.set(pathList, replaceElement)

    }

    //6.0以下版本中该方法的参数类型是ArrayList,6.0及以上版本该方法的参数类型是List
    private fun makeDexElementParamsType(): Class<out List<*>> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            List::class.java else ArrayList::class.java
    }

    // 6.0以下版本中该方法是makeDexElements,6.0及以上版本是makePathElements
    private fun makeDexElementMethodName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            "makePathElements"
        } else {
            "makeDexElements"
        }
    }

    private fun copyFile(context: Context): File? {
        val dir = context.getDir("hack", Context.MODE_PRIVATE) ?: return null
        val file = File(dir, "hack.dex")
        if (!file.exists()) {
            try {
                val inputStream = BufferedInputStream(context.assets.open("hack.dex"))
                val outputStream = BufferedOutputStream(FileOutputStream(file))
                val bytes = ByteArray(2048)
                var len = 0
                while (len != -1) {
                    outputStream.write(bytes, 0, len)
                    len = inputStream.read(bytes)
                }
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
            }
        }
        return file
    }

}