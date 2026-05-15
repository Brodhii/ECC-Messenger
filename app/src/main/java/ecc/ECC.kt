package ecc

import java.math.BigInteger

// FIX: ECC.kt sebelumnya kosong — implementasi kurva eliptik secp256k1
object ECC {

    // Parameter kurva secp256k1
    val p: BigInteger = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)
    val a: BigInteger = BigInteger.ZERO
    val b: BigInteger = BigInteger.valueOf(7)
    val n: BigInteger = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)

    // Generator point G
    val G: Point = Point(
        BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
        BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
    )

    /** Penjumlahan dua titik pada kurva eliptik */
    fun pointAdd(P: Point, Q: Point): Point {
        if (P.isInfinity()) return Q
        if (Q.isInfinity()) return P

        val lambda: BigInteger = if (P == Q) {
            // Point doubling
            val num = BigInteger.valueOf(3).multiply(P.x.pow(2)).add(a)
            val den = BigInteger.valueOf(2).multiply(P.y).modInverse(p)
            num.multiply(den).mod(p)
        } else {
            // Point addition
            val num = Q.y.subtract(P.y)
            val den = Q.x.subtract(P.x).modInverse(p)
            num.multiply(den).mod(p)
        }

        val xR = lambda.pow(2).subtract(P.x).subtract(Q.x).mod(p)
        val yR = lambda.multiply(P.x.subtract(xR)).subtract(P.y).mod(p)
        return Point(xR, yR)
    }

    /** Perkalian skalar: k * P menggunakan double-and-add */
    fun scalarMultiply(k: BigInteger, P: Point): Point {
        var result = Point.INFINITY
        var addend = P
        var scalar = k.mod(n)

        while (scalar != BigInteger.ZERO) {
            if (scalar.testBit(0)) {
                result = pointAdd(result, addend)
            }
            addend = pointAdd(addend, addend)
            scalar = scalar.shiftRight(1)
        }
        return result
    }
}
