package com.example.eccchat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eccchat.ecc.ECCHelper
import com.example.eccchat.model.Message
import com.example.eccchat.repository.FirebaseRepository
import java.math.BigInteger

@Composable
fun ChatScreen(
    currentUserId: String,
    otherUserId: String,
    privateKeyHex: String
) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Pair<Message, String>>()) }
    var otherPublicKey by remember { mutableStateOf("") }
    var showDebug by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }

    // Ambil public key lawan chat
    LaunchedEffect(otherUserId) {
        FirebaseRepository.getPublicKey(otherUserId) { key ->
            otherPublicKey = key ?: ""
        }
        FirebaseRepository.listenMessages(currentUserId, otherUserId) { msg ->
            val privateKey = BigInteger(privateKeyHex, 16)
            val decrypted = try {
                ECCHelper.decrypt(msg.encryptedContent, privateKey)
            } catch (e: Exception) {
                "Gagal dekripsi"
            }
            messages = messages + Pair(msg, decrypted)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Header
        Text("💬 Chat", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        // Tombol Debug
        TextButton(onClick = { showDebug = !showDebug }) {
            Text(if (showDebug) "Sembunyikan enkripsi" else "Lihat proses enkripsi")
        }

        // Panel Debug
        if (showDebug && debugInfo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("🔐 Proses Enkripsi ECC:",
                        style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(debugInfo,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Daftar pesan
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { (msg, decrypted) ->
                val isMine = msg.senderId == currentUserId
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                ) {
                    // Pesan terdekripsi
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMine)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = decrypted,
                            modifier = Modifier.padding(10.dp),
                            color = if (isMine)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Tampilkan versi terenkripsi
                    if (showDebug) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔒 ${msg.encryptedContent.take(30)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Input pesan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Ketik pesan...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (messageText.isNotBlank() && otherPublicKey.isNotBlank()) {
                        val pubKey = ECCHelper.stringToPublicKey(otherPublicKey)
                        val encrypted = ECCHelper.encrypt(messageText, pubKey)

                        // Simpan info debug
                        debugInfo = "Pesan asli: $messageText\n" +
                                "Public key: ${otherPublicKey.take(20)}...\n" +
                                "Terenkripsi: ${encrypted.take(40)}..."

                        val msg = Message(
                            senderId = currentUserId,
                            receiverId = otherUserId,
                            encryptedContent = encrypted,
                            timestamp = System.currentTimeMillis()
                        )
                        FirebaseRepository.sendMessage(msg,
                            onSuccess = { messageText = "" },
                            onError = { }
                        )
                    }
                }
            ) {
                Text("Kirim")
            }
        }
    }
}
