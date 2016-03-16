package com.xrtb.tools;

import java.net.InetAddress;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A program that fixes all the localhost indexes in the rtb4free,com web site
 * 
 * @author Ben M. Faul
 *
 */
public class WebPatch {
	/** List of files to modify */
	static List<String> files = new ArrayList();
	static {
		files.add("database.json");
		files.add("stub.json");
		files.add("Campaigns/extendedDevice-test.json");
		files.add("Campaigns/payday.json");
		files.add("Campaigns/README.md");
		files.add("Campaigns/rtbfree-payday.json");
		files.add("Campaigns/Source.txt");
		files.add("web/login.html");
		files.add("web/exchange.html");
		files.add("web/admin.html");
		files.add("web/crosstalk.html");
		files.add("web/videostub.html");
		files.add("XXXwww/crosstalk.html");
		files.add("XXXwww/admarkup.html");
		files.add("XXXwww/blog_link.html");
		files.add("XXXwww/clickmapper.html");
		files.add("XXXwww/index.html");
		files.add("XXXwww/video-sample.html");
		files.add("XXXwww/banner-sample.html");
		files.add("XXXwww/details.html");
		files.add("XXXwww/faq.html");
		files.add("XXXwww/vast.html");
		files.add("XXXwww/elastic.html");
		files.add("XXXwww/integration.html");
		files.add("XXXwww/privatex/x_index.html");
		files.add("XXXwww/privatex/x_details.html");
	}

	public static void main(String[] args) throws Exception {
		boolean write = false;
		WebPatch p = new WebPatch();
		String fix = "";
		String address = "";
		String redis = "localhost:7379";
		String brand = "RTB4FREE";

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-www":
				fix = args[++i];
				i++;
				break;
			case "-address":
				address = args[++i];
				i++;
				break;
			case "-brand":
				brand = args[++i];
				i++;
				break;
			case "-redis":
				redis = args[++i];
				i+=2;
				break;
			default:
				System.err.println("Huh? " + args[i]);
				return;
			}
		}
		
		if (address.length() ==0) {
			System.err.println("You must specify at least -address");
			return;
		}

		String computername = null;
		try {
			computername = InetAddress.getLocalHost().getHostName();
		} catch (Exception error) {
			computername = address;
		}
		System.out.println("System Name = " + computername);
		if (computername.contains("ben") == false) {
			write = true;
			System.out.println("*** NOTE *** FILES WILL BE MODIFIED ***");
		} else {
			System.out.println("*** NO FILES WILL BE MODIFIED HERE ***");
		}
		for (String file : files) {
			file = file.replace("XXX", fix);
			String content = null;
			try {
				content = new String(Files.readAllBytes(Paths.get(file)));
				StringBuilder sb = new StringBuilder(content);
				int z = p.perform("localhost:7379", redis, sb);
				int k = p.perform("localhost", address, sb);		
				int x = p.perform("__BRAND__", brand, sb);
				if (write)
					Files.write(Paths.get(file), sb.toString().getBytes());
				System.out.println(file + " had " + k + " replacements for localhost");
				System.out.println(file + " had " + x + " replacements for __BRAND__");
				System.out.println(file + " had " + z + " replacements for localhost:7379");
			} catch (Exception error) {
				System.out.println(file + " does not exist, SKIPPED...");
			}
		}

	}

	public int perform(String from, String to, StringBuilder sb) {
		int k = 0;
		while (patch(from, to, sb)) {
			k++;
		}
		return k;
	}

	public boolean patch(String from, String to, StringBuilder sb) {
		int index = sb.indexOf(from);
		if (index > -1) {

			sb.replace(index, index + from.length(), to);
			return true;

		}
		return false;
	}
}
