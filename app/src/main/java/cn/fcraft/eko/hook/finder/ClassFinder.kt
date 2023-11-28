package cn.fcraft.eko.hook.finder

import cn.fcraft.eko.const.PackageName
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerI
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Modifier

class ClassFinder private constructor() {
    fun find(packageName: String, sourceDir: String) {
        loggerI(msg = "[Find Class] Start!")
        System.loadLibrary("dexkit")
        val bridge = DexKitBridge.create(sourceDir)
            ?: throw NullPointerException("DexKitBridge.create() failed")
        bridge.findClass {
            // searchPackages = listOf(PackageName.EKO)
            matcher {
                methods {
                    // RecordingTime矫正函数
                    add {
                        modifiers = Modifier.PUBLIC
                        paramTypes = listOf("float")
                        usingNumbers = listOf(30.0f, 60.0f, 90.0f, 120.0f)
                    }
                }
            }
        }.forEach {
            loggerI(msg = "[Find Class] Result: ${it.className}")
        }
        // 释放缓存
        bridge.close()
        loggerI(msg = "[Find Class] Finish!")
    }
    fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        val packageName = loadPackageParam.packageName
        val apkPath = loadPackageParam.appInfo.sourceDir
        loggerI(msg = "[Find Class] packageName: ${loadPackageParam.packageName}")
        if (packageName != PackageName.EKO) {
            return
        }
        loggerI(msg = "[Find Class] Start!")
        System.loadLibrary("dexkit")
        val bridge = DexKitBridge.create(apkPath)
            ?: throw NullPointerException("DexKitBridge.create() failed")
        bridge.findClass {
            searchPackages = listOf(PackageName.EKO)
            matcher {
                methods {
                    // RecordingTime矫正函数
                    add {
                        modifiers = Modifier.PUBLIC
                        paramTypes = listOf("float")
                        usingNumbers = listOf(30.0f, 60.0f, 90.0f, 120.0f)
                    }
                }
            }
        }.forEach {
            loggerD(msg = "find class: ${it.className}")
        }
        // 释放缓存
        bridge.close()
        loggerI(msg = "[Find Class] Finish!")
    }

    companion object {
        val instance: ClassFinder by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ClassFinder()
        }
    }
}