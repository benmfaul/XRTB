package com.xrtb.exchanges;

import java.io.InputStream;

import com.xrtb.pojo.BidRequest;

/**
 * Implements the Fyber Exchange specific processing.
 * @author Ben M. Faul
 *
 */
public class Fyber extends BidRequest {
	
	public Fyber() {
		super();
		parseSpecial();
	}
	
	public Fyber(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	public Fyber(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Private Exchange object from this class instance.
	 * @throws JsonProcessingException on parse errors.
	 * @throws Exception on stream reading errors
	 */
	@Override
	public Fyber copy(InputStream in) throws Exception  {
		return new Fyber(in);
	}
	
	
	/**
	 * Process special Nexage stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		exchange = "fyber";
		usesEncodedAdm = false;
		return true;
	}

///////////////////////////////////////////////////////////////
/*
 *
public static long ecpi_decode(String encoded,
		 StringBuilder ekey, int ekey_len,
		StringBuilder ikey, int ikey_len,
		StringBuilder tv,
		String uuid) throws Exception
		{
		 int tv_len = 8; // time value (sec + usec)
		 int uuid_len = 8; // uuid
		 int cipher_len = 8; // encrypted price cipher
		 int sign_len = 4; // integrity signature
		 int iv_len = tv_len + uuid_len;
		 int buf_len = iv_len + cipher_len + sign_len;

		 String buffer = base64_decode(encoded);
		 if (buffer.length() != buf_len)
		 {
		 throw new Exception("bad ecpi buffer length");
		 }

		 // calculate decrypt digest
		 String digest;
		 {
		      HMAC hmac = new HMAC(ekey, ekey_len);
		 	  digest = hmac.digest(buffer, iv_len);
		 }

		 // price in network byte order
		 StringBuilder netw_price;

		 // decrypt...
		 StringBulder work = new StringBuilder();
		 StringBuilder cipher = new StringBuilder();
		 cipher.append(buffer.substr(iv_len);
		 for (uint i = 0; i < cipher_len; ++i)
		 {
		 	work.append(cipher[i] ^ digest[i]);
		 }

		 // check signature
		 {
		 hmac(ikey, ikey_len);
		 hmac.update(work, cipher_len);
		 hmac.update(&buffer[0], iv_len);
		 digest = hmac.getFinal();
		 
		 work.length(0);
		 work.append(buffer.substr(iv_len + cipher_len);
		 for (int i = 0; i < sign_len; ++i)
		 {
		 if (work.charAt[i] != digest.charAt[i])
		 {
		 throw new Exception("ecpi integrity");
		 }
		 }
		 }

		 // timestamp
		 if (tv != null)
		 {
		 uint32_t* pval = reinterpret_cast<uint32_t*>(&buffer[0]); // first word
		 tv->tv_sec = be32toh(*pval);
		 pval = reinterpret_cast<uint32_t*>(&buffer[4]); // second word
		 tv->tv_usec = be32toh(*pval);
		 }

		 // uuid

if (uuid != null)
{
uuid.length(0);
uuid.append(buffer.substr(tv_len
std::copy(buffer.begin() + tv_len, buffer.begin() + iv_len, uuid-
>begin());
}
return be64toh(netw_price); // return host byte order
}
*/
	public static String base64_decode(String encoded) {
		return null;
	}
}

class HMAC {
	
	public HMAC() {
		
	}
	
	public StringBuilder digest(StringBuilder a, int b) {
		return null;
	}
	
	public StringBuilder update(StringBuilder a, int b) {
		return null;
	}
	
	public StringBuilder getFinal() {
		return null;
	}
}
