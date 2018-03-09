package com.xrtb.exchanges.openx;

import com.xrtb.pojo.WinObject;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.spec.SecretKeySpec;

/**
 * A class that handles Openx wins (synthesized in the creative, google doesn't support the nurl).
 * @author Ben M. Faul
 *
 */
public class OpenXWinObject extends WinObject {
	/** Integrity key */
	private static SecretKeySpec integrity;
	/** Encryption key */
	private static SecretKeySpec encryption;
	/** The OpenX encryption object */
	private static final SsRtbCrypter crypter = new SsRtbCrypter();

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
	public OpenXWinObject(String hash, String cost, String lat,
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
		this.timestamp = System.currentTimeMillis();
		this.domain = domain;
		this.bidtype = bidType;
		try {
			Double value = decrypt(price);
			value /= 1000;
			this.price = value.toString();
		} catch (Exception e) {
			this.price = price;
			e.printStackTrace();
		}
		this.adm = null;
	}
	
	/**
	 * Google price decrypter
	 * @param cypher String. The encoded crypto text
	 * @return double. The decrypted price as a double.
	 * @throws Exception on crypto errors.
	 */
	public static double decrypt(String cypher ) throws Exception {
		return crypter.decodeDecrypt(cypher, encryption, integrity);
	}

	/**
	 * Creates a secret key.
	 * @param keyStr String. The key string, can be base64 or Hex.
	 * @return SecretKeySpec. They key spec.
	 * @throws Exception on mangled keys.
	 */
	public static SecretKeySpec getKeySpec(String keyStr) throws Exception {
		byte[] keyBytes = null;
		if (keyStr.length() == 44) {
			keyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(keyStr.getBytes("US-ASCII"));
		} else if (keyStr.length() == 64) {
			keyBytes = Hex.decodeHex(keyStr.toCharArray());
		}

		return new SecretKeySpec(keyBytes, "HmacSHA1");
	}

	/**
	 * Sets the ekey and ikey specifications from string ekey and ikey specifications.
	 * @param ekey String. Base64 or web64 based key representation.
	 * @param ikey String. Base64 or web64 based key representation.
	 * @throws Exception on key specification errors.
	 */
	public static void setKeys(String ekey, String ikey) throws Exception {
		encryption = getKeySpec(ekey);
		integrity  = getKeySpec(ikey);
	}
}
