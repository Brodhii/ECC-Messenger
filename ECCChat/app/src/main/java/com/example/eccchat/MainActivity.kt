package com.example.eccchat

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.eccchat.repository.FirebaseRepository
import com.example.eccchat.ui.LoginScreen
import com.example.eccchat.ui.RegisterScreen
import com.example.eccchat.ui.ChatScreen
import com.example.eccchat.ui.theme.ECCChatTheme
import com.example.eccchat.ui.UserListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ECCChatTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ecc_prefs", Context.MODE_PRIVATE)

    var currentScreen by remember { mutableStateOf("login") }
    var currentUserId by remember { mutableStateOf("") }
    var otherUserId by remember { mutableStateOf("") }
    var privateKeyHex by remember { mutableStateOf("") }



    LaunchedEffect(Unit) {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid != null) {
            currentUserId = uid
            privateKeyHex = prefs.getString("private_key", "") ?: ""
            currentScreen = "userlist"
        }
    }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = {
                val uid = FirebaseRepository.getCurrentUserId() ?: ""
                currentUserId = uid
                privateKeyHex = prefs.getString("private_key", "") ?: ""
                currentScreen = "userlist"
            },
            onGoToRegister = { currentScreen = "register" }
        )
        "register" -> RegisterScreen(
            onRegisterSuccess = {
                val uid = FirebaseRepository.getCurrentUserId() ?: ""
                currentUserId = uid
                privateKeyHex = prefs.getString("private_key", "") ?: ""
                currentScreen = "userlist"
            },
            onGoToLogin = { currentScreen = "login" }
        )
        "chat" -> ChatScreen(
            currentUserId = currentUserId,
            otherUserId = otherUserId,
            privateKeyHex = privateKeyHex
        )

        "userlist" -> UserListScreen(
            currentUserId = currentUserId,
            onUserSelected = { uid ->
                otherUserId = uid
                currentScreen = "chat"
            },
            onLogout = {
                FirebaseRepository.logout()
                currentScreen = "login"
            }
        )
    }
}