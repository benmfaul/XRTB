import com.xrtb.exchanges.adx.AdxWinObject;
import com.xrtb.exchanges.google.GoogleWinObject;

public class TestPrice {

	public static void main(String args[]) throws Exception {
		String crypt = "WUOXfQAE0sIKUaLPAAgFA6QtpXwk7aG46DUWfQ";
		String ekey = "kB8RQtv1rlArbt1YFRoHUiCn3mtP3d88VdfRRT+ujMA=";
		String ikey = "+b5gBm7mJcmZgF/YT4bxZoQUsU+vpwqm2sShqc5rcPk=";
		
		long utc = System.currentTimeMillis();
		GoogleWinObject.encryptionKeyBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(ekey);
		GoogleWinObject.integrityKeyBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(ikey);
		double d  = GoogleWinObject.decrypt(crypt, utc);
		System.out.println(d);
	}
}
