package com.eccchat.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eccchat.app.ui.LoginActivity

// FIX: MainActivity cukup redirect ke LoginActivity saat launch
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Langsung buka LoginActivity, tidak perlu setContentView di sini
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // tutup MainActivity agar tidak ada di back stack
    }
}
