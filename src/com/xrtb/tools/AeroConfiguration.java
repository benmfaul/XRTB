package com.xrtb.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.aerospike.client.AerospikeClient;
import com.aerospike.redisson.AerospikeHandler;
import com.aerospike.redisson.RedissonClient;

/**
 * Load the contents of a file into Aerospike. Useful for configuration files.
 * @author Ben M. Faul
 *
 */
public class AeroConfiguration {

	/**
	 * The application. -set key filename, or -get key
	 * @param args String[]. The arguments.
	 * @throws Exception on errors.
	 */
	public static void main(String args[]) throws Exception {
		String name = null;
		String cmd = null;
		String contents = null;
		String spike = "localhost";

		int i = 0;
		if (args.length > 0) {
			while (i < args.length) {
				switch(args[i]) {
				case "-h":
					System.out.println(
							"-aero <host:port>       [Sets the host:port string of the cache        ]");
					System.out.println(
							"-set  <name> <filename> [Set <name> to contents of <filename> contents ]");
					System.out.println(
							"-get  <name>            [Print the contents of name                   ]");
					System.exit(0);
				case "-aero":
					spike = args[i + 1];
					i += 2;
					break;
				case "-set":
					cmd = args[i];
					name = args[i+1];
					contents = new String(Files.readAllBytes(Paths.get(args[i+2])), StandardCharsets.UTF_8);
					i+=3;
					break;
				case "-get":
					cmd = args[i];
					name = args[i+1];
					i+=2;
					break;
				}
			}
		}
		if (cmd == null) {
			System.out.println("Please use either -get or -set");
			System.exit(0);;
		}
		
		AerospikeHandler ae = AerospikeHandler.getInstance(spike, 3000,300);
		RedissonClient redisson = new RedissonClient(ae);
		if (cmd.equals("-get")) {
			contents = redisson.get(name);
			System.out.println(contents);
		} 
		if (cmd.equals("-set")) {
			redisson.set(name, contents);
		}

		System.exit(0);
	}

}
