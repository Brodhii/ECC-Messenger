package com.example.eccchat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase

data class UserItem(val uid: String, val email: String)

@Composable
fun UserListScreen(
    currentUserId: String,
    onUserSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    var users by remember { mutableStateOf(listOf<UserItem>()) }

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance(
            "https://eccchat-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<UserItem>()
                snapshot.children.forEach { child ->
                    val uid = child.key ?: return@forEach
                    val email = child.child("email").value as? String ?: return@forEach
                    if (uid != currentUserId) {
                        list.add(UserItem(uid, email))
                    }
                }
                users = list
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("👥 Pilih User", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (users.isEmpty()) {
            Text("Belum ada user lain. Daftarkan akun lain dulu!",
                color = MaterialTheme.colorScheme.outline)
        }

        LazyColumn {
            items(users) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onUserSelected(user.uid) }
                ) {
                    Text(
                        text = user.email,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
