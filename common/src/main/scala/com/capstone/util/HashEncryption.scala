package com.capstone.util

import java.security.MessageDigest
import java.util

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

trait HashEncryption {

  val CIPHER = "AES/ECB/PKCS5Padding"
  val keyForEncryption = "capstone"
  val sixteen = 16
  val md: MessageDigest = MessageDigest.getInstance("SHA-1")
  val keyDigest: Array[Byte] = md.digest(keyForEncryption.getBytes)
  val keySpec: Option[SecretKeySpec] = Some(
    new SecretKeySpec(util.Arrays.copyOf(keyDigest, sixteen), "AES"))

  def encrypt(plaintext: String): String = keySpec match {
    case Some(spec) =>
      val cipher = Cipher.getInstance(CIPHER)
      cipher.init(Cipher.ENCRYPT_MODE, spec)
      Base64.encodeBase64String(cipher.doFinal(plaintext.getBytes("UTF-8")))

    case _ => plaintext
  }
}
