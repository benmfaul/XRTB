package com.xrtb.exchanges.google;

import com.xrtb.exchanges.adx.Base64;
import com.xrtb.exchanges.adx.Decrypter;
import com.xrtb.pojo.WinObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.URLDecoder;

/**
 * A class that handles google wins (synthesized in the creative, google doesn't support the nurl).
 * @author Ben M. Faul
 *
 */
public class GoogleWinObject extends WinObject {

	public static byte[] encryptionKeyBytes;
	public static byte[] integrityKeyBytes;

	protected static final Logger logger = LoggerFactory.getLogger(GoogleWinObject.class);
	
	/**
	 * 
	 * @param hash String. The bid id.
	 * @param cost String. The cost.
	 * @param lat String. The latitude.
	 * @param lon String. The longitude.
	 * @param adId String. The ad id (campaign).
	 * @param crid String. The creative id.
	 * @param pubId String. The exchange name.
	 * @param image String. The image url.
	 * @param forward String. The forward url (raw creative).
	 * @param price String. The price.
	 * @param adm String. The adm field sent to google.
	 */
	public GoogleWinObject(String hash, String cost, String lat,
                           String lon, String adId, String crid, String pubId, String image,
                           String forward, String price, String adm, String adtype, String domain, String bidType) {
		this.hash = hash;
		this.cost = cost;
		this.lat = lat;
		this.lon = lon;
		this.adId = adId;
		this.cridId = crid;
		this.pubId = pubId;
		this.image = image;
		this.forward = forward;
		this.adtype = adtype;
		this.domain = domain;
		this.bidtype = bidtype;
		this.timestamp = System.currentTimeMillis();

		try {
			this.hash = URLDecoder.decode(this.hash, "UTF-8");
			Double value = new Double(decrypt(price,timestamp));
			value /= 1000;
			this.price = value.toString();
		} catch (Exception e) {
			this.price = price;
			logger.warn("Error in hash or price decryption, id: {}, value: {}, error: {}", this.hash,price, e.toString());
			e.printStackTrace();
		}
		this.adm = null;
	}
	
	/**
	 * Google price decrypter
	 * @param websafeB64EncodedCiphertext String. The encoded crypto text
	 * @param utc long. The current UTC.
	 * @return double. The decrypted price as a double.
	 * @throws Exception on crypto errors.
	 */
	public static double decrypt(String websafeB64EncodedCiphertext, long utc) throws Exception {
		String b64EncodedCiphertext = Decrypter.unWebSafeAndPad(websafeB64EncodedCiphertext);
		byte[] codeString = Base64.decodeBase64(b64EncodedCiphertext.getBytes("US-ASCII"));
		byte[] plaintext;
		double value;
		
	    SecretKey encryptionKey = new SecretKeySpec(encryptionKeyBytes, "HmacSHA1");
	    SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, "HmacSHA1");
	    try {
	      plaintext = Decrypter.decrypt(codeString, encryptionKey, integrityKey);
			DataInputStream dis = new DataInputStream( new ByteArrayInputStream(plaintext));

			value = dis.readLong();
			return value;
	    } catch (Exception e) {
	      // might be a test, we will try to just use it as is.
	      try {
	    	  value = Double.parseDouble(websafeB64EncodedCiphertext);
			  return value;
		  } catch (Exception ee) {
	    	  logger.warn("Failed to decode ciphertext; {}, error: {}", websafeB64EncodedCiphertext, e.getMessage());
	    	  throw ee;
	      }
	    }
	}
	
	
	
	/**
	 * Take the bytes from BidReqyest.encrypted)hyperlocal_set, and send them here. Then you can take the
	 * cleartext and 
	 * @param code byte[]. The encrypted bytes of the hyperlocal.
	 * @return byte []. The bytes of the decoded protobuf.
	 * @throws Exception on protobuf or encryption errors.
	 */
	public static byte[] decryptHyperLocal(byte [] code) throws Exception {

		byte[] plaintext;
	    SecretKey encryptionKey = new SecretKeySpec(encryptionKeyBytes, "HmacSHA1");
	    SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, "HmacSHA1");
	 
	    plaintext = Decrypter.decrypt(code, encryptionKey, integrityKey);

	    return plaintext;
	}
	
	/**
	 * Decrypt the user id.
	 * @param encrypted byte []. Encrypted protobuf bytes.
	 * @return String. The decrypted advertising id.
	 * @throws Exception on decryption errors.
	 */
	public static String decryptAdvertisingId(byte [] encrypted) throws Exception {
		 SecretKey encryptionKey = new SecretKeySpec(encryptionKeyBytes, "HmacSHA1");
		 SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, "HmacSHA1");
		 
		 byte [] rc = Decrypter.decrypt(encrypted, encryptionKey, integrityKey);
		 StringBuffer sb = new StringBuffer();
		 for (int i=0;i<rc.length;i++) {
			 sb.append(Integer.toHexString(0xff & rc[i]));
		 }
		 sb.insert(20, "-");
		 sb.insert(16, "-");
		 sb.insert(12, "-");
		 sb.insert(8, "-");
		 return sb.toString();
	}
	
	/**
	 * Decryprt the IFA field.
	 * @param encrypted
	 * @return
	 * @throws Exception
	 */
	public static String decryptIfa(byte [] encrypted) throws Exception {
		 SecretKey encryptionKey = new SecretKeySpec(encryptionKeyBytes, "HmacSHA1");
		 SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, "HmacSHA1");
		 
		 byte [] rc = Decrypter.decrypt(encrypted, encryptionKey, integrityKey);
		 StringBuffer sb = new StringBuffer();
		 for (int i=0;i<rc.length;i++) {
			 sb.append(Integer.toHexString(0xff & rc[i]));
		 }
		 sb.insert(20, "-");
		 sb.insert(16, "-");
		 sb.insert(12, "-");
		 sb.insert(8, "-");
		 return sb.toString();
		 //return new String(rc);
	}
	
	/**
	 * GIven a String of hex values, return the byte[] array corresponding to it.
	 * @param s String. The string to encode.
	 * @return byte[] the hexstring encoded as bytes.
	 */
	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
