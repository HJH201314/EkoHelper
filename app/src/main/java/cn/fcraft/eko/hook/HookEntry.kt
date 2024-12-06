package cn.fcraft.eko.hook

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.widget.Toast
import cn.fcraft.eko.R
import cn.fcraft.eko.const.PackageName
import cn.fcraft.eko.hook.finder.ClassFinder
import cn.fcraft.eko.wrapper.BuildConfigWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.core.finder.members.FieldFinder
import com.highcapable.yukihookapi.hook.factory.allFields
import com.highcapable.yukihookapi.hook.factory.applyModuleTheme
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.searchClass
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.ViewClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog { tag = "EkoHelper" }
        isDebug  = true
        isEnableHookModuleStatus = true
        isEnableDataChannel = false
    }

    override fun onHook() = encase {
        var isLoaded = false

        loadApp(name = PackageName.EKO) {
            ClassFinder.instance.find(this.appInfo.packageName, this.appInfo.sourceDir)
            resources().hook {
                // The title of Settings
                injectResource {
                    conditions {
                        name = "app_settings_fragment_title_app_settings_dc_navigation_bar_title"
                        string()
                    }
                    replaceTo("App Settings (Pro)")
                }
                //  The entry of Settings
                injectResource {
                    conditions {
                        name = "menu_category_app_settings_dc_rows_title_icon"
                        string()
                    }
                    replaceTo("App Settings (Pro)")
                }
                // The entry of Live Stream
                injectResource {
                    conditions {
                        name = "live_stream_view_title_label_dc_sheet_recording_header"
                        string()
                    }
                    replaceTo("Live stream!")
                }
                // The "Record" button on older version
                injectResource {
                    conditions {
                        name = "fragment_ce_btn_record"
                        string()
                    }
                    replaceTo("Record!")
                }
                // The "Record" button on newer version
                injectResource {
                    conditions {
                        name = "ce_btn_record_title_dc_sheet_onboarding_title"
                        string()
                    }
                    replaceTo("Record!")
                }
            }

            ActivityClass.hook {
                injectMember {
                    method {
                        name = "onCreate"
                        param(BundleClass)
                        returnType = UnitType
                    }
                    afterHook {
                        if (!isLoaded) {
                            Toast.makeText(instance(), "EkoHelper Loaded!", Toast.LENGTH_SHORT).show()
                            isLoaded = true
                        }
                    }
                }
            }

            // Return "US" to bypass location restriction, though may have side effects
            "java.util.Locale".hook {
                injectMember {
                    method {
                        name = "getCountry"
                    }
                    replaceTo("US")
                }
            }

            // Seems like a class runs on JNI, so we can hook it directly
            "com.ekodevices.library.device.EDDevice".hook {
                injectMember {
                    method {
                        name = "startRecording"
                    }
                    beforeHook {
                        // the first argument is the recording time, we change it to 12000s
                        this.args(0).set(12000.0f)
                        loggerI(msg = "Start Record with 12000s limit.")
                    }
                }
            }

            // disable force update dialog
            "com.ekodevices.app.common.BaseActivity".hook {
                injectMember {
                    method {
                        name = "x0" // only supports app version 4.0.2
                        replaceUnit {
                            loggerI(msg = "Bypassed force update dialog.")
                        }
                    }
                }
            }

            // enable Live Stream entry
            "com.ekodevices.ekodata.model.user.Plan".hook {
                injectMember {
                    method {
                        name = "getLiveStream"
                        replaceToTrue()
                    }
                }
            }
        }
    }

    override fun onXposedEvent() {
        // 监听原生 Xposed API 的装载事件
        YukiXposedEvent.events {
            onInitZygote {
                // it 对象即 [StartupParam]
            }
            onHandleLoadPackage {
                // it 对象即 [LoadPackageParam]
                loggerI(msg = "onHandleLoadPackage")
                // ClassFinder.instance.handleLoadPackage(it)
            }
            onHandleInitPackageResources {
                // it 对象即 [InitPackageResourcesParam]
            }
        }
    }
}