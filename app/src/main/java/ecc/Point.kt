package ecc

import java.math.BigInteger

// FIX: Point.kt sebelumnya kosong — class untuk representasi titik pada kurva eliptik
data class Point(
    val x: BigInteger,
    val y: BigInteger
) {
    companion object {
        /** Titik tak hingga (identity element pada grup ECC) */
        val INFINITY = Point(BigInteger.ZERO, BigInteger.ZERO)
    }

    fun isInfinity(): Boolean = this == INFINITY

    override fun toString(): String = "(${x.toString(16)}, ${y.toString(16)})"
}
