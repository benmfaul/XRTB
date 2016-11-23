
public class AdxStuff {

	public static void main (String args[]) {
		byte [] x = javax.xml.bind.DatatypeConverter.parseBase64Binary("");
		int k = 0;
		for (int i=0; i<x.length;i++) {

			if (k == 5) {
				k = 0;
				System.out.println();
			}
			k++;
			System.out.print("0x" + Integer.toHexString(0xff & x[i]) + ", ");
		}
	}
}
