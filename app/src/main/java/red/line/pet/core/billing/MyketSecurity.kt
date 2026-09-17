package red.line.pet.core.billing

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Security helper to verify in-app purchases with Myket RSA public key.
 */
object MyketSecurity {

    const val MYKET_PUBLIC_KEY =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxpaeU01XQZxJQrtuCUQqQbHUirMaXAmxHEUyElstFnBndrFj2x1Zt5746hjN7cq7VvbgpdgbOTCh0g543AaYO64Gw93stZOeloVWMTiUsQNzKz7e8qflR4PE4hIi0aHMYqIt3X2t45YVGnwooGn7vmMPApBudwcCAsWBuX5T2HwIDAQAB"

    const val PRODUCT_ID_VIP = "red.line.pet_pro"
    const val PACKAGE_NAME = "red.line.pet"

    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM_SHA1 = "SHA1withRSA"
    private const val SIGNATURE_ALGORITHM_SHA256 = "SHA256withRSA"

    /**
     * Verifies that the data was signed with the private key corresponding to the given public key.
     */
    fun verifyPurchase(
        base64PublicKey: String = MYKET_PUBLIC_KEY,
        signedData: String,
        signature: String
    ): Boolean {
        if (signedData.isBlank() || base64PublicKey.isBlank() || signature.isBlank()) {
            return false
        }

        val publicKey = generatePublicKey(base64PublicKey) ?: return false
        return verify(publicKey, signedData, signature)
    }

    /**
     * Generates a PublicKey instance from a base64 encoded string.
     */
    fun generatePublicKey(encodedPublicKey: String): PublicKey? {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifies that the signature is valid for the signedData.
     * Tries SHA1withRSA (standard for IAB V3) and fallbacks to SHA256withRSA.
     */
    fun verify(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        val signatureBytes = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: Exception) {
            return false
        }

        val dataBytes = signedData.toByteArray(Charsets.UTF_8)

        // 1. Standard Myket / IAB v3 algorithm: SHA1withRSA
        try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM_SHA1)
            sig.initVerify(publicKey)
            sig.update(dataBytes)
            if (sig.verify(signatureBytes)) {
                return true
            }
        } catch (_: Exception) {
        }

        // 2. Fallback algorithm: SHA256withRSA
        try {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM_SHA256)
            sig.initVerify(publicKey)
            sig.update(dataBytes)
            if (sig.verify(signatureBytes)) {
                return true
            }
        } catch (_: Exception) {
        }

        return false
    }
}
