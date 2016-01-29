import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.db.User;


public class TimeTest {
	
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static void main(String args[]) throws Exception {
		
		String content = null;
		Year year = new Year(2016);
		
		FileReader fr = new FileReader("logs/accounting"
				+ ""
				+ ""); 
		BufferedReader bufr = new BufferedReader(fr); 
		while((content = bufr.readLine()) != null) {
			Record record = mapper.readValue(content, Record.class);
			record.process();
			
			year.process(record);
		}
		
		year.print();
	}
}

class Year {
	List<Month> months = new ArrayList();    // warning 1 is the first, 0 is unused
	int year;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public Year(int year) {
		this.year = year;
		for (int i=0;i<13;i++) {
			months.add(new Month(i));
		}
	}
	
	public void process(Record r) {
		int which = r.footprint.get(Record.MONTH);
		Month m = months.get(which);
		m.process(r);
	}
	
	public void print() {
		System.out.println(year);
		for (int i=1; i< 2 /*months.size()*/;i++) {
			Month month = months.get(i);
			month.print();
			add(month);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		System.out.println("\n\nYEAR TOTALS");
		System.out.println("Year     Pixels    Clicks   Bids     Wins    Bid Price    Win Price");
		String result = String.format("%4d  %8d %8d %8d %8d %12.4f %12.4f",year,pixels,clicks,bids,wins,bidP, winP);
		System.out.println(result);
	}
	
	public void add(Month h) {
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = winPrice.add(h.bidPrice);	
	}
}

class Month {
	List<ADate> dates = new ArrayList();
	int name;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public Month(int month) {
		name = month;
		for (int i=0;i<31;i++) {
			dates.add(new ADate(i));
		}
	}
	
	public void process(Record r) {
		ADate date = dates.get(r.footprint.get(Record.DATE));
		date.process(r);
	}
	
	public void print() {
		System.out.println("Month: " + name);
		System.out.println("Day       Pixels    Clicks   Bids     Wins    Bid Price    Win Price");
		for (int i=1; i<dates.size();i++) {
			ADate date = dates.get(i);
			date.print();
			add(date);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		String result = String.format("TOTAL %8d %8d %8d %8d %12.4f %12.4f",pixels,clicks,bids,wins,bidP, winP);
		System.out.println(result);
	}
	
	public void add(ADate h) {
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = winPrice.add(h.bidPrice);	
	}
}

class ADate {
	List<Hour> hours = new ArrayList();
	int name;
	
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	public ADate(int date) {
		name = date;
		for (int i=0;i<24;i++) {
			hours.add(new Hour(i));
		}
	}
	public void process(Record r) {
		int which = r.footprint.get(Record.HOUR);
		Hour h = hours.get(which);
		h.process(r);
	}
	
	public void print() {
	//	System.out.println("Date: " + name + "\n");
		
		//System.out.println("Hour       Pixels    Clicks   Bids     Wins    Bid Price    Win Price");
		for (int i=0; i<hours.size();i++) {
			Hour hour = hours.get(i);
			// hour.print();
			add(hour);
		}
		
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		String result = String.format("%4d  %8d %8d %8d %8d %12.4f %12.4f",name,pixels,clicks,bids,wins,bidP, winP);
		System.out.println(result);
	}
	
	public void add(Hour h) {
		pixels += h.pixels;
		clicks += h.clicks;
		bids += h.bids;
		wins += h.wins;
		winPrice = winPrice.add(h.winPrice);
		bidPrice = winPrice.add(h.bidPrice);	
	}
}

class Hour {
	long pixels;
	long clicks;
	long bids;
	long wins;
	BigDecimal winPrice = new BigDecimal(0);
	BigDecimal bidPrice = new BigDecimal(0);
	
	int name;
	
	public Hour(int hour) {
		name = hour;
	}
	
	public void process(Record r) {
		
		if (r.pixels != 0) {
			System.out.println("OK!");
		}
		pixels += r.pixels;
		clicks += r.clicks;
		bids += r.bids;
		wins += r.wins;
		winPrice = winPrice.add(new BigDecimal(r.winPrice));
		bidPrice = winPrice.add(new BigDecimal(r.bidPrice));	
	}
	
	public void print() {
		double bidP = bidPrice.doubleValue();
		double winP = winPrice.doubleValue();
		String result = String.format("%2d    %8d %8d %8d %8d %12.4f %12.4f",name,pixels,clicks,bids,wins,bidP, winP);
	//	System.out.println(result);
		
	}
}

class Record {
	public static int YEAR = 0;
	public static int MONTH = 1;
	public static int DATE = 2;
	public static int HOUR = 3;
	public long pixels;
	public long bids;
	public long wins;
	public long clicks;
	public double bidPrice;
	public double winPrice;
	public String name;
	public String campaignName;
	public String accountName;
	public long time;
	
	
	
	static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",
            Locale.US);
	static {
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	
	@JsonIgnore
	transient List<Integer> footprint = new ArrayList();
	
	public Record() {
		
	}
	
	public void process() {
		Date date = new Date(time);
		String result = format.format(date);
		String [] parts = result.split("-");
		footprint.add(Integer.parseInt(parts[0]));
		parts[1] = parts[1].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(parts[1]));
		
		String []  nparts = parts[2].split("T");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(nparts[0]));
		
		nparts = nparts[1].split(":");
		nparts[0] = nparts[0].replaceFirst("^0+(?!$)", "");
		footprint.add(Integer.parseInt(nparts[0]));
		
	}
}
