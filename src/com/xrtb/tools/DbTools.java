package com.xrtb.tools;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.aerospike.client.AerospikeClient;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.common.Campaign;

import com.xrtb.db.DataBaseObject;
import com.xrtb.db.Database;
import com.xrtb.db.User;

/**
 * Simple program to manipulate the Redisson map used as the database in
 * RTB4FREE Arguments:
 * <p>
 * [-redis host:port] Sets the redis host/port, uses localhost as default
 * <p>
 * [-clear] Drop the database in Redis
 * <p>
 * [-dump] Print the contents of the database.
 * <p>
 * [-load fname] Load the file into the database
 * <p>
 * [-write fname] Write the database to the file (in JSON form)
 * <p>
 * eg java -jar xrtb.jar tools.DbTools -clear -load mydb.json -dump
 * <p>
 * By default, no arguments loads database.json into the redisson database
 * 
 */

public class DbTools {
	/** jackson object mapper */
	public static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	/** The redisson backed shared map that represents this database */
	ConcurrentMap<String, User> map;
	Set<String> set;
	com.aerospike.redisson.RedissonClient redisson;
	DataBaseObject dbo;

	/**
	 * Execute the database commands. With no arguments loads database.json into
	 * the Redisson map stored in Redis on local host.
	 * 
	 * @param args
	 *            String[]. List of arguments.
	 * @throws Exception
	 *             on I/O or Redis errors.
	 */
	public static void main(String args[]) throws Exception {
		String db = "database.json";
		String blist = "blacklist.json";
		String spike = "localhost:3000";

		int i = 0;
		DbTools tool = null;
		if (args.length > 0) {
			while (i < args.length) {
				if (args[i].equals("-h")) {
					System.out.println(
							"-aero <host:port>           [Sets the host:port string of the cache                     ]");
					System.out.println(
							"-clear                      [Clears the cache database                                  ]");
					System.out.println(
							"-print                      [Print the cache database to stdout                         ]");
					System.out.println(
							"-db <file-name>             [Loads the cache from a JSON file (default: ./database.json]]");
					System.out.println(
							"-load-blacklist <file-name> [Loads the blacklist from a list of domains                 ]");
					System.out.println(
							"-write <filename>           [Writes cache of database to the named file                 ]");
					System.out.println(
							"-write-blacklist <filename> [Writes redis blacklist to the named file                   ]");
					System.exit(0);
				} else if (args[i].equals("-db")) {
					i++;
					db = args[i];
					i++;
				} else if (args[i].equals("-bl")) {
					i++;
					blist = args[i];
					i++;
				} else if (args[i].equals("-aero")) {
					spike = args[i + 1];
					i += 2;
				} else if (args[i].equals("-clear")) {
					i++;
					if (tool == null)
						tool = new DbTools(spike);
					tool.clear();
				} else if (args[i].equals("-print")) {
					if (tool == null)
						tool = new DbTools(spike);
					tool.printDatabase();
					i++;
				} else if (args[i].equals("-write")) {
					if (tool == null)
						tool = new DbTools(spike);
					tool.saveDatabase(args[i + 1]);
					i += 2;
				} else if (args[i].equals("-write-blacklist")) {
					if (tool == null)
						tool = new DbTools(spike);
					tool.writeBlackList(args[i + 1]);
					i += 2;
				} else if (args[i].equals("-load-blacklist")) {
					if (tool == null)
						tool = new DbTools(spike);
					tool.loadBlackList(args[i + 1]);
					i += 2;
				}
			}
		}
		tool = new DbTools(spike);
		tool.clear();
		tool.loadDatabase(db);
		tool.saveDatabase(db);
		tool.printDatabase();

		System.exit(0);
	}

	/**
	 * Drop the database in memory
	 */
	public void clear() {
		map.clear();
	}

	public void shutdown() {

	}

	/**
	 * Simple constructor. Used to setup redisson to local host.
	 * 
	 * @param redis
	 *            String. The redis host:port string definition.
	 * @throws Exception
	 *             on Redis connection errors.
	 */

	public DbTools(String path) throws Exception {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String parts[] = path.split(":");
		int port = 3000;
		String host = parts[0];
		if (parts.length > 1) {
			port = Integer.parseInt(parts[1]);
		}
		AerospikeClient spike = new AerospikeClient(host, port);
		redisson = new com.aerospike.redisson.RedissonClient(spike);
		dbo = DataBaseObject.getInstance(redisson);
		;

		map = redisson.getMap("users-database");
		set = redisson.getSet(DataBaseObject.MASTER_BLACKLIST);
	}

	/**
	 * Load a JSON string database into the Redisson (redis) map.
	 * 
	 * @param db
	 *            String. The JSON file to load into redis.
	 * @throws Exception
	 *             on I/O errors.
	 */
	public void loadDatabase(String db) throws Exception {
		dbo.clear();
		List<User> x = read(db);

		for (Object o : x) {
			User u = (User) o;
			dbo.put(u);
		}
		System.out.println("Database init complete.");
	}

	public void loadBlackList(String blackListFile) throws Exception {
		dbo.clearBlackList();
		List<String> list = readBlackList(blackListFile);
		for (String s : list) {
			dbo.addToBlackList(s);
		}
	}

	/**
	 * Delete a user from the redis database.
	 * 
	 * @param user
	 *            Name of the user to delete
	 * @throws Exception
	 *             on Redisson errors
	 */
	public void deleteUser(String user) throws Exception {
		dbo.remove(user);
	}

	/**
	 * Remove a campaign by ad id.
	 * 
	 * @param adId
	 *            String. The ad id.
	 * @throws Exception
	 *             if the adid does not exist.
	 */
	public void deleteCampaign(String adId) throws Exception {
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			User u = map.get(key);
			for (int i = 0; i < u.campaigns.size(); i++) {
				Campaign c = u.campaigns.get(i);
				if (c.adId.equals(adId)) {
					u.campaigns.remove(i);
					dbo.put(u);
					return;
				}
			}
		}
		throw new Exception("No such campaign");
	}

	/**
	 * Print the contents of the REDIS database to stdout.
	 */
	public void printDatabase() throws Exception {
		List<String> users = dbo.listUsers();
		for (String who : users) {
			User u = dbo.get(who);
			System.out.println("====> " + who);
			String str = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(u);
			System.out.println(str);
		}
	}

	/**
	 * Save the redis database to disk of the given name.
	 * 
	 * @param db
	 *            String. The name of the file to contain the database.
	 * @throws Exception
	 *             on IO errors.
	 */
	public void saveDatabase(String db) throws Exception {
		write(db);
	}

	/**
	 * Read the database.json file into this object.
	 * 
	 * @param db
	 *            String. The JSON string database to load into Redis.
	 * @return List. A list of users in the database file.
	 * @throws Exception
	 *             on file errors.
	 */
	public List<User> read(String db) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(db)), StandardCharsets.UTF_8);

		System.out.println(content);

		List<User> users = mapper.readValue(content,
				mapper.getTypeFactory().constructCollectionType(List.class, User.class));
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			User u = map.get(it.next());
			for (Campaign c : u.campaigns) {
				c.owner = u.name;
			}
			dbo.put(u);
		}
		return users;
	}

	public List<String> readBlackList(String fname) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(fname)), StandardCharsets.UTF_8);
		System.out.println(content);
		List<String> list = mapper.readValue(content, List.class);
		dbo.addToBlackList(list);
		return list;
	}

	/**
	 * Write the database object to the database.json file.
	 * 
	 * @param dbName
	 *            String. The filename to contain the Redis database.
	 * @throws Exception
	 *             on file errors.
	 */
	public void write(String dbName) throws Exception {
		List<User> list = new ArrayList();

		List<String> users = dbo.listUsers();
		for (String user : users) {
			User u = dbo.get(user);
			for (Campaign c : u.campaigns) {
				c.owner = u.name;
			}
			list.add(u);
		}

		String content = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(list);
		Files.write(Paths.get(dbName), content.getBytes());
		System.out.println(content);
	}

	public void writeBlackList(String blist) throws Exception {
		List<String> list = dbo.getBlackList();
		String content = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(list);
		Files.write(Paths.get(blist), content.getBytes());
		System.out.println(content);
	}

}
