package com.xrtb.common;

/**
 * Utility functions used throughout the project.
 */
import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import java.util.TimeZone;


public class Utils {
	// used for classloader
	private static Utils instance = new Utils();

	private Utils() {
	}

	public static String readFile(String fileName) throws Exception {
		File inFile = new File(fileName);
		char cbuf[] = new char[(int) inFile.length()];

		// ... Create reader and writer for text files.
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		reader.read(cbuf);
		reader.close(); // Close to unlock.

		return new String(cbuf);
	}
	
	public static byte [] readFileAsBytes(String fileName) throws Exception, IOException {
		File file = new File(fileName);
		InputStream is = new FileInputStream(file);
		byte bytes[] = new byte[(int) file.length()];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
		return bytes;
	}

	public static void writeFile(String str, String fileName) throws Exception {
		File outFile = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(str);
		bw.close();
	}
	
	public static void writeFile(byte [] data, String fileName) throws Exception {
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(data);
		fos.close();
	}

	public static void appendFile(String str, String fileName) throws Exception {
		File outFile = new File(fileName);

		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));
		bw.write(str);
		bw.close();
	}
	
	/**
	 * Return the now string in form YYYYMMDD.
	 * 
	 * @return String. The YYYYMMDD of right now.
	 */
	public static String getNowDateString() {
		Calendar d = Calendar.getInstance();

		String sm = "" + (d.get(d.MONTH) + 1);
		if (sm.length() == 1)
			sm = "0" + sm;

		String sy = "" + d.get(d.YEAR);

		String sd = "" + d.get(d.DATE);
		if (sd.length() == 1)
			sd = "0" + sd;
		return sy + sm + sd;
	}
	
	/**
	 * Return a datetime string.
	 * 
	 * @return. String. Returns datetime in YYYYMMDD HH:SS:MM
	 */
	static public String now() {
		Calendar d = Calendar.getInstance();

		String sm = "" + (d.get(d.MONTH) + 1);
		if (sm.length() == 1)
			sm = "0" + sm;

		String sy = "" + d.get(Calendar.YEAR);

		String sd = "" + d.get(Calendar.DATE);
		if (sd.length() == 1)
			sd = "0" + sd;
		String hd = "" + d.get(Calendar.HOUR_OF_DAY);
		if (hd.length() == 1)
			hd = "0" + hd;
		String mn = "" + d.get(Calendar.MINUTE);
		if (mn.length() == 1)
			mn = "0" + mn;
		String sc = "" + d.get(Calendar.SECOND);
		if (sc.length() == 1)
			sc = "0" + sc;
		return sy + sm + sd + " " + hd + ":" + mn + ":" + sc;
	}
	/**
	 * Return a datetime string.
	 * 
	 * @return. String. Returns datetime in YYYYMMDD HH:SS:MM
	 */
	static public String getDate(String date) {

		//substring(4,9) of hamburger is urge.
		String sm = date.substring(4,6);
		String sy = "" + date.substring(2,4);
		String sd = "" + date.substring(6,8);
		String hd = "" + date.substring(9,11);
		// go to normal format
        String ampm = null;
		if (hd.equals("23")){ampm="pm"; hd = "11x";}
		if (hd.equals("22")){ampm="pm"; hd = "10x";}
		if (hd.equals("21")){ampm="pm"; hd = "9";}
		if (hd.equals("20")){ampm="pm"; hd = "8";}
		if (hd.equals("19")){ampm="pm"; hd = "7";}
		if (hd.equals("18")){ampm="pm"; hd = "6";}
		if (hd.equals("17")){ampm="pm"; hd = "5";}
		if (hd.equals("16")){ampm="pm"; hd = "4";}
		if (hd.equals("15")){ampm="pm"; hd = "3";}
		if (hd.equals("14")){ampm="pm"; hd = "2";}
		if (hd.equals("13")){ampm="pm"; hd = "1";}
		if (hd.equals("12")){ampm="pm"; hd = "12";}
		if (hd.equals("11")){ampm="am"; hd = "11";}
		if (hd.equals("10")){ampm="am"; hd = "10";}
		if (hd.equals("09")){ampm="am"; hd = "9";}
		if (hd.equals("08")){ampm="am"; hd = "8";}
		if (hd.equals("07")){ampm="am"; hd = "7";}
		if (hd.equals("06")){ampm="am"; hd = "6";}
		if (hd.equals("05")){ampm="am"; hd = "5";}
		if (hd.equals("04")){ampm="am"; hd = "4";}
		if (hd.equals("03")){ampm="am"; hd = "3";}
		if (hd.equals("02")){ampm="am"; hd = "2";}
		if (hd.equals("01")){ampm="am"; hd = "1";}
		if (hd.equals("00")){ampm="am"; hd = "12";}
		if (hd.equals("11x")){ampm="pm"; hd = "11";}
		if (hd.equals("10x")){ampm="pm"; hd = "10";}
		
		String mn = "" + date.substring(12,14);
		return sm + "/" + sd + "/" + sy + " " + hd + ":" + mn + ampm;
	}
	
	static public String normalizeDate(String date) {
		String [] triple = date.split("-");
		String mm = convertToDigits(triple[0]);
		String dd = triple[1];
		if (dd.length()==1) 
			dd = "0" + dd;
		String yy = triple[2];
		if (yy.length()==2)
			yy = "20" + yy;
		return mm + "/" + dd + "/" + yy;
	}
	
	static public String convertToDigits(String mm) {
		if (mm.equals("Jan"))
			return "01";
		if (mm.equals("Feb"))
			return "02";
		if (mm.equals("Mar"))
			return "03";
		if (mm.equals("Apr"))
			return "04";
		if (mm.equals("May"))
			return "05";
		if (mm.equals("Jun"))
			return "06";
		if (mm.equals("Jul"))
			return "07";
		if (mm.equals("Aug"))
			return "08";
		if (mm.equals("Sep"))
			return "09";
		if (mm.equals("Oct"))
			return "10";
		if (mm.equals("Nov"))
			return "11";
		if (mm.equals("Dec"))
			return "12";
		return "00";
	}
	
	// in mm/dd/yy
	static public long getTimeSlashFormat(String monthdayyear) {
		String [] parts = monthdayyear.split("/");
		String [] xparts = monthdayyear.split(" ");
		String time = "";
		if (xparts.length == 1) {
			time = "00:00:00";
		} else
			time = xparts[1];
		String year = parts[2];
		year = year.substring(0,4);
		String day = parts[1];
		String month = parts[0];
		
		return getTime(year+month+day+time);
	}
	
	static public long getTimeDashFormat(String monthdayyear) {
		String [] parts = monthdayyear.split("-");
		String [] xparts = monthdayyear.split(" ");
		String time = "";
		if (xparts.length == 1) {
			time = "00:00:00";
		} else
			time = xparts[1];
		String year = parts[2];
		year = year.substring(0,4);
		String day = parts[1];
		String month = convertToDigits(parts[0]);
		
		return getTime(year+month+day+time);
	}
	
	static public long getTime(String ymdb) {
		Calendar s = Calendar.getInstance();
		String s1 = null;
		if (ymdb.length() == 17) {
		  s1 = ymdb.substring(0,8).concat(ymdb.substring(9,17));
		  ymdb = s1;
		}

		String yr = ymdb.substring(0, 4);
		ymdb = ymdb.substring(4);
		String mo = ymdb.substring(0, 2);
		ymdb = ymdb.substring(2);
		String dy = ymdb.substring(0, 2);
		ymdb = ymdb.substring(2);
		String hr = ymdb.substring(0,2);
		ymdb = ymdb.substring(3);
		String mn = ymdb.substring(0,2);
		ymdb = ymdb.substring(3);
		String sc = ymdb.substring(0,2);

		s.set(Integer.parseInt(yr), Integer.parseInt(mo) - 1, Integer
				.parseInt(dy), Integer.parseInt(hr), Integer.parseInt(mn),Integer.parseInt(sc));
		Date bTime = s.getTime();
		return bTime.getTime();
	}

	/**
	 * Return tomorrow's date.
	 * 
	 * @return String. YYYYMMDD of tomorrow.
	 */
	static public String getTomorrowString() {
		Calendar d = Calendar.getInstance();
		String sm = "" + (d.get(d.MONTH) + 1);
		if (sm.length() == 1)
			sm = "0" + sm;
		String sy = "" + d.get(d.YEAR);
		String sd = "" + (d.get(d.DATE) + 1);
		if (sd.length() == 1)
			sd = "0" + sd;

		return sy + sm + sd;
	}
	/**
	 * Return date 6 mths from now.
	 * 
	 * @return String. YYYYMMDD of tomorrow.
	 */
	static public String getdateinmthsfromnow(int n) {
		Calendar d = Calendar.getInstance();
		String sm = null; String sd = null; String sy = null;
		if (n==6) {
		  sm = "" + (d.get(d.MONTH) + 6+1);
    	  if (sm.length() == 1)
			sm = "0" + sm;
    	  sy = "" + (d.get(d.YEAR));
    	  if (sm.equals("13")) {
    		  sm = "01"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
    	  if (sm.equals("14")) {
    		  sm = "02"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
    	  if (sm.equals("15")) {
    		  sm = "03"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
    	  if (sm.equals("16")) {
    		  sm = "04"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
    	  if (sm.equals("17")) {
    		  sm = "05"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
    	  if (sm.equals("18")) {
    		  sm = "06"; sy = "" + (d.get(d.YEAR)+ 1);
    	  }
		} 
	    
		if (n==18) {
		  sm = "" + (d.get(d.MONTH) + 6+1);
	      if (sm.length() == 1)
			sm = "0" + sm;
	      
	        sy = "" + (d.get(d.YEAR)+ 1);
	    	if (sm.equals("13")) {
	    	  sm = "01"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	    	if (sm.equals("14")) {
	    	  sm = "02"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	    	if (sm.equals("15") ){
	    	  sm = "03"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	    	if (sm.equals("16") ){
	    	  sm = "04"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	    	if (sm.equals("17") ){
	    	  sm = "05"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	    	if (sm.equals("18")) {
	    	  sm = "06"; sy = "" + (d.get(d.YEAR)+ 2);
	    	}
	      } 

		if (n==12) {
		  sm = "" + (d.get(d.MONTH) + 1);
		  sy = "" + (d.get(d.YEAR)+ 1);	
		}
		if (n==24) {
		  sm = "" + (d.get(d.MONTH) + 1);
		  sy = "" + (d.get(d.YEAR)+ 2);		
		}
		
		sd = "" + (d.get(d.DATE) );
		if (sd.length() == 1)
			sd = "0" + sd;

		return sy + sm + sd;
	}
	
	static public String convertTimeToZulu(String datetime) {
		String rets = null;
		Date d = new Date(Long.parseLong(datetime));
		
		SimpleDateFormat SDF =
            new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
		TimeZone utc = TimeZone.getTimeZone( "UTC" );
        SDF.setTimeZone( utc );
        rets = SDF.format( d );
        int i = rets.indexOf("T");
        rets = rets.substring(i);
        rets = rets.replaceAll(":", "");
        rets = rets.substring(0,rets.indexOf(".")) + "Z";
		return rets;
	}
	
	public static void main(String [] args) {
		String now = "" + System.currentTimeMillis();
		System.out.println(convertTimeToZulu(now));
	}
}
