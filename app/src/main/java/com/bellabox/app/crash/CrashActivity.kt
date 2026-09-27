package com.bellabox.app.crash

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.bellabox.app.MainActivity
import java.io.File

class CrashActivity : Activity() {

    companion object {
        const val EXTRA_CRASH_INFO = "extra_crash_info"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make status & navigation bars dark and transparent
        window.statusBarColor = Color.parseColor("#121318")
        window.navigationBarColor = Color.parseColor("#121318")

        val crashText = intent.getStringExtra(EXTRA_CRASH_INFO)
            ?: run {
                val f = File(filesDir, "crash_latest.log")
                if (f.exists()) f.readText() else "无详细日志 (No crash log available)"
            }

        val isZh = try {
            val prefs = getSharedPreferences("bellabox_settings", Context.MODE_PRIVATE)
            prefs.getString("pref_language", "zh") != "en"
        } catch (e: Exception) {
            true
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121318"))
            val pad = dpToPx(20)
            setPadding(pad, pad + dpToPx(24), pad, pad + dpToPx(16))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Header Title
        val titleView = TextView(this).apply {
            text = if (isZh) "BellaBox 异常拦截报告" else "BellaBox Crash Report"
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        rootLayout.addView(titleView)

        // Subtitle
        val subtitleView = TextView(this).apply {
            text = if (isZh) {
                "应用遇到未捕获的异常，已自动拦截以防静默退出。请复制以下日志以排查问题，或重置数据重启。"
            } else {
                "An uncaught exception occurred. Crash has been intercepted. You can copy the diagnostic logs or reset data."
            }
            setTextColor(Color.parseColor("#A0A5B5"))
            textSize = 13f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(6)
                bottomMargin = dpToPx(16)
            }
            layoutParams = params
        }
        rootLayout.addView(subtitleView)

        // Scrollable Log Box
        val logScrollView = ScrollView(this).apply {
            val bgDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#1C1E26"))
                cornerRadius = dpToPx(12).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#2E3240"))
            }
            background = bgDrawable
            val p = dpToPx(14)
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        val logTextView = TextView(this).apply {
            text = crashText
            setTextColor(Color.parseColor("#00E676"))
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        logScrollView.addView(logTextView)
        rootLayout.addView(logScrollView)

        // Action Buttons Container
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            layoutParams = params
        }

        // Button 1: Copy Log
        val copyButton = createStyledButton(
            text = if (isZh) "复制完整错误日志" else "Copy Crash Log",
            bgColor = Color.parseColor("#3B82F6"),
            textColor = Color.WHITE
        ) {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("BellaBox Crash", crashText))
            Toast.makeText(
                this,
                if (isZh) "日志已成功复制到剪贴板" else "Log copied to clipboard",
                Toast.LENGTH_SHORT
            ).show()
        }
        buttonContainer.addView(copyButton)

        // Button 2: Reset Data & Restart
        val resetButton = createStyledButton(
            text = if (isZh) "重置本地数据并重启" else "Reset Data & Restart",
            bgColor = Color.parseColor("#EF4444"),
            textColor = Color.WHITE
        ) {
            try {
                deleteDatabase("bellabox.db")
                getSharedPreferences("bellabox_settings", Context.MODE_PRIVATE).edit().clear().commit()
                Toast.makeText(this, if (isZh) "数据已重置" else "Data reset", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Ignore
            }
            restartApp()
        }
        (resetButton.layoutParams as LinearLayout.LayoutParams).topMargin = dpToPx(10)
        buttonContainer.addView(resetButton)

        // Button 3: Restart App
        val restartButton = createStyledButton(
            text = if (isZh) "安全重启应用" else "Restart Application",
            bgColor = Color.parseColor("#272A34"),
            textColor = Color.WHITE
        ) {
            restartApp()
        }
        (restartButton.layoutParams as LinearLayout.LayoutParams).topMargin = dpToPx(10)
        buttonContainer.addView(restartButton)

        rootLayout.addView(buttonContainer)
        setContentView(rootLayout)
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun createStyledButton(
        text: String,
        bgColor: Int,
        textColor: Int,
        onClick: (View) -> Unit
    ): Button {
        return Button(this).apply {
            this.text = text
            this.setTextColor(textColor)
            this.textSize = 14f
            this.isAllCaps = false
            this.typeface = Typeface.DEFAULT_BOLD
            val bg = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dpToPx(10).toFloat()
            }
            this.background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
            )
            this.layoutParams = params
            this.setOnClickListener(onClick)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
