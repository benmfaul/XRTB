// Copyright OpenX Limited 2010. All Rights Reserved.
package com.xrtb.exchanges.openx;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A class that can encrypt and decrypt the price macro used in
 * Server-Side Real-time bidders.
 */
public class SsRtbCrypter {
  private static final Charset US_ASCII = Charset.forName("US-ASCII");
  private static final String HMAC_SHA1 = "HmacSHA1";

  private static final int IV_SIZE = 16;
  private static final int CIPHERTEXT_SIZE = 8;
  private static final int INTEGRITY_SIZE = 4;

  private final Random rand;

  public SsRtbCrypter() {
    this.rand = new Random();
  }

  public SecretKey b64StrToKey(String b64Str) {
    byte[] keyBytes = Base64.decodeBase64(b64Str.getBytes(US_ASCII));
    return new SecretKeySpec(keyBytes, HMAC_SHA1);
  }

  public SecretKey hexStrToKey(String hexKey) throws DecoderException {
    char[] keyChars = hexKey.toCharArray();
    byte[] keyBytes = Hex.decodeHex(keyChars);
    return new SecretKeySpec(keyBytes, HMAC_SHA1);
  }

  public String encryptEncode(long value, String b64EncryptKey, String b64IntegrityKey) {
    return encryptEncode(value, b64StrToKey(b64EncryptKey), b64StrToKey(b64IntegrityKey));
  }

  public long decodeDecrypt(String base64Websafe, String b64EncryptKey, String b64IntegrityKey) throws SsRtbDecryptingException {
    return decodeDecrypt(base64Websafe, b64StrToKey(b64EncryptKey), b64StrToKey(b64IntegrityKey));
  }

  public String encryptEncode(long value, SecretKey encryptKey, SecretKey integrityKey) {
    byte[] encrypted = encrypt(toBytes(value), encryptKey, integrityKey);
    String b64NonWebsafe = new String(Base64.encodeBase64(encrypted), US_ASCII);
    String b64Websafe = b64NonWebsafe.replace("+", "-").replace("/", "_");
    return b64Websafe.substring(0, 38);
  }

  public long decodeDecrypt(String base64Websafe, SecretKey encryptKey, SecretKey integrityKey) throws SsRtbDecryptingException {
    String base64NonWebsafe = base64Websafe.replace("-", "+").replace("_", "/") + "==";
    byte[] encrypted = Base64.decodeBase64(base64NonWebsafe.getBytes(US_ASCII));
    byte[] decrypted = decrypt(encrypted, encryptKey, integrityKey);
    return toLong(decrypted);
  }

  public byte[] encrypt(byte[] unciphered, String b64EncryptKey, String b64IntegrityKey) {
    return encrypt(unciphered, b64StrToKey(b64EncryptKey), b64StrToKey(b64IntegrityKey));
  }

  public byte[] decrypt(byte[] ciphered, String b64EncryptKey, String b64IntegrityKey) throws SsRtbDecryptingException {
    return decrypt(ciphered, b64StrToKey(b64EncryptKey), b64StrToKey(b64IntegrityKey));
  }

  public byte[] encrypt(
      byte[] unciphered,
      SecretKey encryptionKey,
      SecretKey integrityKey) {

    // Byte array to store the encrypted value
    byte[] out = new byte[IV_SIZE + CIPHERTEXT_SIZE + INTEGRITY_SIZE];

    // Wrap byte array in ByteBuffer
    ByteBuffer outBuff = ByteBuffer.wrap(out);

    // First 8 bytes of IV are set to current time in millis
    outBuff.putLong(System.currentTimeMillis());

    // Second 8 bytes should be set to time in micros, but java doesn't
    // have a way to get that, so we just set it to random bytes
    outBuff.putLong(rand.nextLong());

    // Flip the buffer so that we can read the IV when creating keypad
    outBuff.flip();

    try {
      // Create MAC
      Mac mac = Mac.getInstance(HMAC_SHA1);
      mac.init(encryptionKey);

      // Update it with the IV
      mac.update(outBuff);

      // Update limit for writing
      outBuff.limit(IV_SIZE + CIPHERTEXT_SIZE + INTEGRITY_SIZE);

      // Now write 8 bytes of the keypad to the buffer
      outBuff.put(mac.doFinal(), 0, CIPHERTEXT_SIZE);

      // Update the buffer by xor-ing the keypad values with the the
      // plaintext values.
      for (int i = 0; i < CIPHERTEXT_SIZE; i++) {
        out[IV_SIZE + i] = (byte)(out[IV_SIZE + i] ^ unciphered[i]);
      }

      // Now compute the signature
      mac.init(integrityKey);

      // First put unciphered
      mac.update(unciphered);

      // Now put IV
      mac.update(out, 0, 16);

      // Take first four bytes as integrity check
      outBuff.put(mac.doFinal(), 0, INTEGRITY_SIZE);

      return out;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("HmacSHA1 not supported.", e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException("Key not valid for HmacSHA1", e);
    }
  }
  
  public byte[] decrypt(byte[] crypted, SecretKey encryptionKey, SecretKey integrityKey)
      throws SsRtbDecryptingException {
    
    // Create array to store unciphered value
    byte[] unciphered = new byte[CIPHERTEXT_SIZE];
    
    try {
      // Create encrypting MAC
      Mac mac = Mac.getInstance(HMAC_SHA1);
      mac.init(encryptionKey);
      
      // Create keypad
      mac.update(crypted, 0, IV_SIZE);
      byte[] pad = mac.doFinal();
      
      // XOR values to get unciphered value
      for (int i = 0; i < CIPHERTEXT_SIZE; i++) {
        unciphered[i] = (byte)(pad[i] ^ crypted[IV_SIZE + i]);
      }

      // Calculate signature bytes
      mac.init(integrityKey);
      mac.update(unciphered);
      mac.update(crypted, 0, IV_SIZE);
      byte[] signature = mac.doFinal();
      
      // Check signature
      for (int i = 0; i < INTEGRITY_SIZE; i++) {
        if (signature[i] != crypted[IV_SIZE + CIPHERTEXT_SIZE + i]) {
          throw new SsRtbDecryptingException("Signature does not match.", null);
        }
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("HmacSHA1 not supported.", e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException("Key not valid for HmacSHA1", e);
    }

    return unciphered;
  }

  /**
   * Converts a long to an 8 byte array. Code is derived from that
   * used in {@link java.io.DataOutputStream#writeLong(long)}.
   * @param val
   * @return byte array of length 8
   */
  protected byte[] toBytes(long val) {
    byte[] buff = new byte[8];
    buff[0] = (byte)(val >>> 56);
    buff[1] = (byte)(val >>> 48);
    buff[2] = (byte)(val >>> 40);
    buff[3] = (byte)(val >>> 32);
    buff[4] = (byte)(val >>> 24);
    buff[5] = (byte)(val >>> 16);
    buff[6] = (byte)(val >>>  8);
    buff[7] = (byte)(val >>>  0);
    return buff;
  }

  /**
   * Converts an array of 8 bytes into a long. Code is dervied from that
   * used in {@link java.io.DataInputStream#readLong()}.
   * @param buff
   * @return value of the 8 byte array as a long
   */
  protected long toLong(byte[] buff) {
    return (((long)buff[0] << 56) +
            ((long)(buff[1] & 255) << 48) +
            ((long)(buff[2] & 255) << 40) +
            ((long)(buff[3] & 255) << 32) +
            ((long)(buff[4] & 255) << 24) +
            ((buff[5] & 255) << 16) +
            ((buff[6] & 255) <<  8) +
            ((buff[7] & 255) <<  0));
  }
}
