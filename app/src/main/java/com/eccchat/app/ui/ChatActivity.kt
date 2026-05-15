package com.eccchat.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.eccchat.app.R
import ecc.ECDH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

// FIX: ChatActivity sebelumnya kosong — implementasi dasar dengan ECDH encryption
class ChatActivity : AppCompatActivity() {

    private val ecdh = ECDH()
    private var sharedSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val etMessage   = findViewById<EditText>(R.id.etMessage)
        val btnSend     = findViewById<Button>(R.id.btnSend)
        val tvMessages  = findViewById<TextView>(R.id.tvMessages)

        // Generate key pair saat activity dibuka
        ecdh.generateKeyPair()

        btnSend.setOnClickListener {
            val plainText = etMessage.text.toString().trim()
            if (plainText.isEmpty()) return@setOnClickListener

            val encrypted = if (sharedSecret != null) {
                ecdh.encrypt(plainText, sharedSecret!!)
            } else {
                plainText // fallback jika shared secret belum tersedia
            }

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.sendMessage(encrypted)
                    }
                    if (response.isSuccessful) {
                        val current = tvMessages.text.toString()
                        tvMessages.text = "$current\nSaya: $plainText"
                        etMessage.setText("")
                    } else {
                        Toast.makeText(this@ChatActivity, "Gagal kirim pesan", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ChatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
