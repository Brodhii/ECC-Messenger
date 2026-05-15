package ecc

import java.math.BigInteger
import java.security.SecureRandom

// FIX: ECDH.kt sebelumnya kosong — implementasi Elliptic-Curve Diffie-Hellman
class ECDH {

    private var privateKey: BigInteger = BigInteger.ZERO
    var publicKey: Point = Point.INFINITY
        private set

    /** Generate pasangan kunci private/public */
    fun generateKeyPair() {
        val random = SecureRandom()
        // Private key: angka acak dalam rentang [1, n-1]
        do {
            privateKey = BigInteger(256, random).mod(ECC.n)
        } while (privateKey == BigInteger.ZERO)

        // Public key = private key * G
        publicKey = ECC.scalarMultiply(privateKey, ECC.G)
    }

    /**
     * Hitung shared secret dari public key pihak lain.
     * sharedSecret = privateKey * otherPublicKey
     * Kedua pihak akan menghasilkan titik yang sama karena:
     *   Alice: a * (b*G) = ab*G
     *   Bob:   b * (a*G) = ab*G
     */
    fun computeSharedSecret(otherPublicKey: Point): String {
        val sharedPoint = ECC.scalarMultiply(privateKey, otherPublicKey)
        return sharedPoint.x.toString(16) // gunakan koordinat X sebagai shared secret
    }

    /** Enkripsi pesan sederhana menggunakan XOR dengan shared secret (demonstrasi) */
    fun encrypt(message: String, sharedSecret: String): String {
        val key = sharedSecret.take(message.length).padEnd(message.length, '0')
        return message.mapIndexed { i, c ->
            (c.code xor key[i].code).toChar()
        }.joinToString("") { it.code.toString(16).padStart(2, '0') }
    }

    /** Dekripsi pesan */
    fun decrypt(hexMessage: String, sharedSecret: String): String {
        val bytes = hexMessage.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
        val key = sharedSecret.take(bytes.length).padEnd(bytes.length, '0')
        return bytes.mapIndexed { i, c ->
            (c.code xor key[i].code).toChar()
        }.joinToString("")
    }
}
