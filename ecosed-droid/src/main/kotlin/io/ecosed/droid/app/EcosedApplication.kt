/**
 * Copyright EcosedDroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ecosed.droid.app

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import io.ecosed.droid.engine.EcosedEngine
import org.lsposed.hiddenapibypass.HiddenApiBypass

class EcosedApplication<YourApplication : IEcosedApplication> : ContextWrapper(null), IEcosedApplication {

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var mApplication: Application

    private lateinit var mEngine: EcosedEngine

    private lateinit var mYourApplication: YourApplication

    private lateinit var mHost: EcosedHost


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
    }



    override val engine: Any
        get() = mEngine

    override val host: Any
        get() = mHost

    override fun <T> IEcosedApplication.execMethodCall(
        name: String,
        method: String,
        bundle: Bundle?,
    ): T? = engineUnit {
        return@engineUnit execMethodCall<T>(
            name = name,
            method = method,
            bundle = bundle
        )
    }

    // 为了不将引擎暴露通过上下文包装器传递需要重定义为引擎
    private fun <T> engineUnit(
        content: EcosedEngine.() -> T,
    ): T? = (engine as EcosedEngine).content()


    @Suppress("UNCHECKED_CAST")
    override fun IEcosedApplication.attachEcosed(
        application: Application,
        host: Any
    ) {
        // 附加基本上下文
        attachBaseContext(base = application.baseContext)
        // 获取应用程序全局类
        mApplication = application
        // 获取mME
        mYourApplication = mApplication as YourApplication


        mHost = host as EcosedHost
        // 初始化引擎
        mEngine = EcosedEngine.create(
            application = mApplication
        )



        mYourApplication.apply {
//            this@EcosedApplication.init()
//            this@apply.init()
        }

        object : Thread() {
            override fun run() {
                mYourApplication.apply {
                    synchronized(this@apply) {
//                        this@EcosedApplication.initSDKs()
//                        this@apply.initSDKs()
                        mainHandler.post {
//                            this@EcosedApplication.initSDKInitialized()
//                            this@apply.initSDKInitialized()
                        }
                    }
                }
            }
        }.start()
    }


    fun runOnMain(runnable: Runnable) {
        mainHandler.post {
            runnable.run()
        }
    }

    private var toast: Toast? = null

    override fun IEcosedApplication.toast(obj: Any) {
        try {
            mainHandler.post {
                log(obj.toString())
                if (toast == null) {
                    toast = Toast.makeText(
                        this@EcosedApplication,
                        mNull,
                        Toast.LENGTH_SHORT
                    )
                }
                toast?.setText(obj.toString())
                toast?.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun IEcosedApplication.log(obj: Any) {
        Log.i(tag, obj.toString())
    }

    companion object {
        const val tag: String = "tag"
        const val mNull: String = ""
    }
}