package com.bellabox.app.crash

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

class CrashHandler private constructor(private val app: Application) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e("BellaCrashHandler", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)
            val crashReport = generateCrashReport(thread, throwable)
            saveCrashReport(crashReport)

            val intent = Intent(app, CrashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(CrashActivity.EXTRA_CRASH_INFO, crashReport)
            }
            app.startActivity(intent)

            Process.killProcess(Process.myPid())
            exitProcess(10)
        } catch (e: Throwable) {
            Log.e("BellaCrashHandler", "Failed to handle uncaught exception: ${e.message}", e)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun generateCrashReport(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString()

        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val packageInfo = try {
            app.packageManager.getPackageInfo(app.packageName, 0)
        } catch (e: Exception) {
            null
        }

        val versionName = packageInfo?.versionName ?: "1.0.1-preview"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 101L
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toLong() ?: 101L
        }

        return buildString {
            appendLine("========== BellaBox 异常崩溃诊断报告 ==========")
            appendLine("时间: $timeStr")
            appendLine("应用版本: $versionName ($versionCode)")
            appendLine("设备厂商: ${Build.MANUFACTURER}")
            appendLine("设备型号: ${Build.MODEL} (${Build.DEVICE})")
            appendLine("系统版本: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("支持架构: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("崩溃线程: ${thread.name} (id: ${thread.id})")
            appendLine("异常类型: ${throwable.javaClass.name}")
            appendLine("异常信息: ${throwable.message}")
            appendLine("----------------- 详细调用栈 -----------------")
            appendLine(stackTrace)
            appendLine("==============================================")
        }
    }

    private fun saveCrashReport(report: String) {
        try {
            val file = File(app.filesDir, "crash_latest.log")
            file.writeText(report)
        } catch (e: Exception) {
            Log.e("BellaCrashHandler", "Failed to save crash log to file: ${e.message}")
        }
    }

    companion object {
        fun install(app: Application) {
            val handler = CrashHandler(app)
            Thread.setDefaultUncaughtExceptionHandler(handler)
        }
    }
}
