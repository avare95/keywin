package com.example.keywin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Adım: Android Klavye Ayarlarını Aç
        findViewById<Button>(R.id.btn_enable_ime)?.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // 2. Adım: Sistem Klavye Seçici Diyaloğunu Göster
        findViewById<Button>(R.id.btn_select_ime)?.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }

        // 3. Adım: GitHub Reposunu Tarayıcıda Aç
        findViewById<Button>(R.id.btn_open_github)?.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/avare95/keywin"))
            startActivity(browserIntent)
        }
    }
}