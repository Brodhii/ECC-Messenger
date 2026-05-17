package com.example.eccchat.ecc

import ecc.ECC
import ecc.Point
import java.math.BigInteger
import java.security.SecureRandom

object ECCHelper {

    /** Generate a key pair (private key as BigInteger, public key as Point) */
    fun generateKeyPair(): Pair<BigInteger, Point> {
        val random = SecureRandom()
        var privateKey: BigInteger
        do {
            privateKey = BigInteger(256, random).mod(ECC.n)
        } while (privateKey == BigInteger.ZERO)

        val publicKey = ECC.scalarMultiply(privateKey, ECC.G)
        return Pair(privateKey, publicKey)
    }

    /** Convert public key Point to String for storage */
    fun publicKeyToString(point: Point): String {
        return "${point.x.toString(16)}:${point.y.toString(16)}"
    }

    /** Convert String back to public key Point */
    fun stringToPublicKey(str: String): Point {
        val parts = str.split(":")
        return Point(BigInteger(parts[0], 16), BigInteger(parts[1], 16))
    }

    /** Encrypt message (Demonstration using simple ECC-based XOR or ElGamal-like) */
    fun encrypt(message: String, otherPublicKey: Point): String {
        // For demonstration, we'll use a simplified version of ECIES/ElGamal
        // In a real app, use ECIES with AES.
        val random = SecureRandom()
        val k = BigInteger(256, random).mod(ECC.n)
        val R = ECC.scalarMultiply(k, ECC.G)
        val S = ECC.scalarMultiply(k, otherPublicKey)
        
        val sharedSecret = S.x.toString(16)
        val encrypted = xorWithKey(message, sharedSecret)
        
        return "${publicKeyToString(R)}|$encrypted"
    }

    /** Decrypt message */
    fun decrypt(encryptedContent: String, privateKey: BigInteger): String {
        val parts = encryptedContent.split("|")
        val R = stringToPublicKey(parts[0])
        val S = ECC.scalarMultiply(privateKey, R)
        
        val sharedSecret = S.x.toString(16)
        return xorWithKey(parts[1], sharedSecret)
    }

    private fun xorWithKey(text: String, key: String): String {
        val fullKey = key.repeat((text.length / key.length) + 1).take(text.length)
        return text.mapIndexed { i, c ->
            (c.code xor fullKey[i].code).toChar()
        }.joinToString("") { it.code.toString(16).padStart(2, '0') }
    }

    private fun xorFromHex(hex: String, key: String): String {
        val text = hex.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
        val fullKey = key.repeat((text.length / key.length) + 1).take(text.length)
        return text.mapIndexed { i, c ->
            (c.code xor fullKey[i].code).toChar()
        }.joinToString("")
    }

    // Adjusting xorWithKey to handle hex input for decryption
    private fun xorWithKey(text: String, key: String, isHexInput: Boolean = false): String {
        val actualText = if (isHexInput) {
             text.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
        } else text
        
        val fullKey = key.repeat((actualText.length / key.length) + 1).take(actualText.length)
        val result = actualText.mapIndexed { i, c ->
            (c.code xor fullKey[i].code).toChar()
        }.joinToString("")
        
        return if (!isHexInput) {
            result.map { it.code.toString(16).padStart(2, '0') }.joinToString("")
        } else result
    }
}
