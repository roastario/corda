package net.corda.core.internal

import net.corda.core.crypto.DigitalSignature
import net.corda.core.crypto.verify
import net.corda.core.serialization.CordaSerializable
import net.corda.core.serialization.SerializedBytes
import net.corda.core.serialization.deserialize
import net.corda.core.utilities.OpaqueBytes
import java.security.cert.X509Certificate

// TODO: Rename this to DigitalSignature.WithCert once we're happy for it to be public API
/** A digital signature that identifies who the public key is owned by, and the certificate which provides prove of the identity */
class DigitalSignatureWithCert(val by: X509Certificate, bytes: ByteArray) : DigitalSignature(bytes) {
    fun verify(content: ByteArray): Boolean = by.publicKey.verify(content, this)
    fun verify(content: OpaqueBytes): Boolean = by.publicKey.verify(content.bytes, this)
}

@CordaSerializable
class SignedDataWithCert<T : Any>(val raw: SerializedBytes<T>, val sig: DigitalSignatureWithCert) {
    fun verified(): T {
        sig.verify(raw)
        return uncheckedCast(raw.deserialize<Any>())
    }
}