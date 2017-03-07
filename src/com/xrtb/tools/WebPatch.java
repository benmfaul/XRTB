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
		files.add("Campaigns/README.md");
		files.add("Campaigns/Source.txt");
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
		
		files.add("XXXwww/SSI/bidloader.ssi");
		files.add("XXXwww/SSI/brand_icon.ssi");
		files.add("XXXwww/SSI/brand_name.ssi");
		files.add("XXXwww/SSI/clickloader.ssi");
		files.add("XXXwww/SSI/index_links.ssi");
		files.add("XXXwww/SSI/logger.ssi");
		files.add("XXXwww/SSI/winloader.ssi");
	}

	public static void main(String[] args) throws Exception {
		boolean write = false;
		WebPatch p = new WebPatch();
		String fix = "";
		String address = "";
		String controller = "localhost";
		String redis = "localhost";           /// patch for payday.json
		String webdis = "localhost:7379";
		String brand = "RTB4FREE";
		String auth = null;
		String demo = null;
		String shard = null;
		String service = null;
		String memory = null;

		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-h":
				System.out.println("-www <directory>          [The parent directory in front of the www, default is ./]");
				System.out.println("-address <host>           [The address of the RTB (do not specify the port]");
				System.out.println("-brand <brand-name>       [The Brandname used to replace RTB4FREE]");
				System.out.println("-aero <aero-host>       [The hostname of where redis lives (do not specify the port]");
				System.out.println("-webdis <redis-host:port> [The hostname of where webdis lives]");
				System.out.println("-service homedir         [The Login home directoy (for /home/ben use -service ben, default is ubuntu)]");
				System.out.println("-demo true | false        [Whether to set the demo mode in login.html and admin.html]" );
				System.exit(1);
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
			case "-demo":
				demo = args[++i];
				i++;
				break;
			case "-webdis":
				webdis = args[++i];
				i++;
				break;
			case "-redis":
			case "-aero":
				redis = args[++i];
				i++;
				break;	
			case "-shard":
				shard = args[++i];
				i++;
				break;
			case "-service":
				service = args[++i];
				i++;
				break;
			case "-auth":
				auth = args[++i];
				i++;
				if (auth.equals("null"))
					auth = null;
				break;	
			case "-controller":
				controller = args[++i];
				i++;
				break;
			case "-memory":
				memory = args[++i];
				i++;
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
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String content = null;
		StringBuilder sb = new StringBuilder();
		
		//System.out.println("------- 5 -----------");
		// demo logins allowed
		if (demo != null) {
			content = new String(Files.readAllBytes(Paths.get("web/admin.html")));
			content = content.replace("rtb4free_demo=false", "rtb4free_demo="+demo);
			Files.write(Paths.get("web/admin.html"), content.getBytes());
			
			content = new String(Files.readAllBytes(Paths.get("web/login.html")));
			content = content.replace("rtb4free_demo=false", "rtb4free_demo="+demo);
			Files.write(Paths.get("web/login.html"), content.getBytes());
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// shard
		if (shard != null || memory != null) {
			content = null;
			if (shard != null) {
				content = new String(Files.readAllBytes(Paths.get("rtb4free.service")));
				content = content.replace("-s zulu", "-s " + shard);
				Files.write(Paths.get("rtb4free.service"), content.getBytes());
			
				content = new String(Files.readAllBytes(Paths.get("rtb4free.conf")));
				content = content.replace("-s zulu", "-s " + shard);
			}
			
			if (memory != null) {
				if (content == null)
					content = new String(Files.readAllBytes(Paths.get("rtb4free.service")));
				content = content.replace("-Xmx4096m", memory);
			}
			Files.write(Paths.get("rtb4free.conf"), content.getBytes());
		}
		////////////////////////////////////////////////////////////////////////////////////
		
		// service home directory
		if (service != null) {
			content = new String(Files.readAllBytes(Paths.get("rtb4free.service")));
			content = content.replace("home/ubuntu", "home/" + service);
			Files.write(Paths.get("rtb4free.service"), content.getBytes());
			
			content = new String(Files.readAllBytes(Paths.get("rtb4free.conf")));
			content = content.replace("home/ubuntu", "home/" + service);
			Files.write(Paths.get("rtb4free.conf"), content.getBytes());
			
			
		}
		
		if (address != null) {
			content = new String(Files.readAllBytes(Paths.get("Campaigns/payday.json")));
			content = content.replace("localhost:8080", address + ":8080");
			Files.write(Paths.get("Campaigns/payday.json"), content.getBytes());
		}
		
		int z;
		for (String file : files) {
			//System.out.println("Processing: " + file);
			file = file.replace("XXX", fix);
			content = null;
			try {
				content = new String(Files.readAllBytes(Paths.get(file)));
				sb = new StringBuilder(content);
				z = p.perform("localhost:7379", webdis, sb);
				//System.out.println("------->Patch z");
				int k = p.perform("localhost", address, sb);	
				//System.out.println("------->Patch k");
				int x = p.perform("RTB4FREE", brand, sb);
				//System.out.println("------->Patch x");
				p.perform("8080:8080","8080",sb);                // hack, fix this
				if (write)
					Files.write(Paths.get(file), sb.toString().getBytes());
				System.out.println(file + " had " + k + " replacements for localhost:8080");
				System.out.println(file + " had " + x + " replacements for __BRAND__");
				System.out.println(file + " had " + z + " replacements for localhost:7379");
			} catch (Exception error) {
				System.out.println(file + " does not exist, SKIPPED...");
			}
		}

	}

	public int perform(String from, String to, StringBuilder sb) throws Exception {
		if (from.equals(to))
			return 0;
		
		int k = 0;
		while (patch(from, to, sb) != false) {
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
