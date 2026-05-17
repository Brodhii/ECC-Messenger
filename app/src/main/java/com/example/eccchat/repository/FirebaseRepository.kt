package com.example.eccchat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.eccchat.model.Message

object FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance("https://eccchat-default-rtdb.asia-southeast1.firebasedatabase.app")

    // Register user baru
    fun register(
        email: String,
        password: String,
        publicKey: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                db.reference.child("users").child(uid).setValue(
                    mapOf("email" to email, "publicKey" to publicKey)
                )
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Register gagal") }
    }

    // Login user
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Login gagal") }
    }

    // Ambil public key user lain
    fun getPublicKey(userId: String, onResult: (String?) -> Unit) {
        db.reference.child("users").child(userId).child("publicKey")
            .get()
            .addOnSuccessListener { onResult(it.value as? String) }
            .addOnFailureListener { onResult(null) }
    }

    // Kirim pesan terenkripsi
    fun sendMessage(message: Message, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val key = db.reference.child("messages").push().key ?: return
        val msg = message.copy(id = key)
        db.reference.child("messages").child(key).setValue(msg)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal kirim pesan") }
    }

    // Dengarkan pesan masuk secara realtime
    fun listenMessages(
        currentUserId: String,
        otherUserId: String,
        onNewMessage: (Message) -> Unit
    ) {
        db.reference.child("messages")
            .addChildEventListener(object : com.google.firebase.database.ChildEventListener {
                override fun onChildAdded(
                    snapshot: com.google.firebase.database.DataSnapshot,
                    previousChildName: String?
                ) {
                    val msg = snapshot.getValue(Message::class.java) ?: return
                    val isRelevant = (msg.senderId == currentUserId && msg.receiverId == otherUserId) ||
                            (msg.senderId == otherUserId && msg.receiverId == currentUserId)
                    if (isRelevant) onNewMessage(msg)
                }
                override fun onChildChanged(s: com.google.firebase.database.DataSnapshot, p: String?) {}
                override fun onChildRemoved(s: com.google.firebase.database.DataSnapshot) {}
                override fun onChildMoved(s: com.google.firebase.database.DataSnapshot, p: String?) {}
                override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
            })
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun logout() = auth.signOut()
}
