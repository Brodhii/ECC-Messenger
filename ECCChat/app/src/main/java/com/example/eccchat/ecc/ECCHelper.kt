package com.example.eccchat.ecc

import java.math.BigInteger

object ECCHelper {

    // Parameter kurva secp192r1
    private val p = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF", 16)
    private val a = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFC", 16)
    private val b = BigInteger("64210519E59C80E70FA7E9AB72243049FEB8DEECC146B9B1", 16)
    private val Gx = BigInteger("188DA80EB03090F67CBF20EB43A18800F4FF0AFD82FF1012", 16)
    private val Gy = BigInteger("07192B95FFC8DA78631011ED6B24CDD573F977A11E794811", 16)
    private val n = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFF99DEF836146BC9B1B4D22831", 16)
    private val G = Pair(Gx, Gy)

    // Hitung modular inverse pakai Extended Euclidean
    private fun modInverse(k: BigInteger, p: BigInteger): BigInteger {
        return k.modInverse(p)
    }

    // Point Addition: P + Q
    private fun pointAdd(
        P: Pair<BigInteger, BigInteger>?,
        Q: Pair<BigInteger, BigInteger>?
    ): Pair<BigInteger, BigInteger>? {
        if (P == null) return Q
        if (Q == null) return P
        if (P == Q) return pointDouble(P)

        val (x1, y1) = P
        val (x2, y2) = Q

        if (x1 == x2) return null

        val lam = ((y2 - y1) * modInverse(x2 - x1, p)).mod(p)
        val x3 = (lam.pow(2) - x1 - x2).mod(p)
        val y3 = (lam * (x1 - x3) - y1).mod(p)
        return Pair(x3, y3)
    }

    // Point Doubling: P + P
    private fun pointDouble(
        P: Pair<BigInteger, BigInteger>?
    ): Pair<BigInteger, BigInteger>? {
        if (P == null) return null
        val (x1, y1) = P

        val lam = ((BigInteger.valueOf(3) * x1.pow(2) + a) * modInverse(
            BigInteger.valueOf(2) * y1, p
        )).mod(p)
        val x3 = (lam.pow(2) - BigInteger.valueOf(2) * x1).mod(p)
        val y3 = (lam * (x1 - x3) - y1).mod(p)
        return Pair(x3, y3)
    }

    // Scalar Multiplication: k × P (inti ECC)
    fun scalarMultiply(k: BigInteger, P: Pair<BigInteger, BigInteger>): Pair<BigInteger, BigInteger>? {
        var result: Pair<BigInteger, BigInteger>? = null
        var addend = P
        var kTemp = k

        while (kTemp > BigInteger.ZERO) {
            if (kTemp.testBit(0)) {
                result = pointAdd(result, addend)
            }
            addend = pointDouble(addend) ?: break
            kTemp = kTemp.shiftRight(1)
        }
        return result
    }

    // Generate Key Pair
    fun generateKeyPair(): Pair<BigInteger, Pair<BigInteger, BigInteger>> {
        val privateKey = BigInteger(192, java.security.SecureRandom()).mod(n - BigInteger.ONE) + BigInteger.ONE
        val publicKey = scalarMultiply(privateKey, G)!!
        return Pair(privateKey, publicKey)
    }

    // Enkripsi pesan (XOR sederhana dengan shared secret)
    fun encrypt(message: String, publicKey: Pair<BigInteger, BigInteger>): String {
        val k = BigInteger(192, java.security.SecureRandom()).mod(n - BigInteger.ONE) + BigInteger.ONE
        val sharedPoint = scalarMultiply(k, publicKey)!!
        val kG = scalarMultiply(k, G)!!
        val secret = sharedPoint.first.toByteArray()
        val msgBytes = message.toByteArray()
        val encrypted = ByteArray(msgBytes.size) { i -> (msgBytes[i].toInt() xor secret[i % secret.size].toInt()).toByte() }
        val encHex = encrypted.joinToString("") { "%02x".format(it) }
        val kGHex = "${kG.first.toString(16)},${kG.second.toString(16)}"
        return "$kGHex|$encHex"
    }

    // Dekripsi pesan
    fun decrypt(encrypted: String, privateKey: BigInteger): String {
        return try {
            val parts = encrypted.split("|")
            val kGParts = parts[0].split(",")
            val kG = Pair(BigInteger(kGParts[0], 16), BigInteger(kGParts[1], 16))
            val encHex = parts[1]
            val sharedPoint = scalarMultiply(privateKey, kG)!!
            val secret = sharedPoint.first.toByteArray()
            val encBytes = ByteArray(encHex.length / 2) {
                encHex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
            }
            val decrypted = ByteArray(encBytes.size) { i ->
                (encBytes[i].toInt() xor secret[i % secret.size].toInt()).toByte()
            }
            String(decrypted)
        } catch (e: Exception) {
            "Dekripsi gagal!"
        }
    }

    // Konversi public key ke String untuk disimpan di Firebase
    fun publicKeyToString(publicKey: Pair<BigInteger, BigInteger>): String {
        return "${publicKey.first.toString(16)},${publicKey.second.toString(16)}"
    }

    // Konversi String kembali ke public key
    fun stringToPublicKey(str: String): Pair<BigInteger, BigInteger> {
        val parts = str.split(",")
        return Pair(BigInteger(parts[0], 16), BigInteger(parts[1], 16))
    }
}