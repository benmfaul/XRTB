package tools;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.redisson.Config;
import org.redisson.Redisson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.xrtb.db.User;

/**
 * A simple program that wotks with the Redisson database map.
 * @author Ben M. Faul
 *
 */

public class DbTools {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	ConcurrentMap<String,User> map;
	String dbName;
	
	/**
	 * Simple program to send and receive commands from the RTB4FREE bidders.
	 * @param args String[]. Arg 0 will contain the JSON database filename, otherwise, database.json in cwd is ued.
	 * @throws Exception on file or Redis errors.
	 */
	public static void main(String args[]) throws Exception {
		String db = "database.json";
		if (args.length != 0) {
			db = args[0];
		}
		DbTools tool = new DbTools();
		tool.loadDatabase(db);
		tool.saveDatabase(db);;
		tool.printDatabase();
		
	}
	
	/**
	 * Simple constructor. Used to setup redisson to local host.
	 * @throws Exception on Redis connection errors.
	 */
	
	public DbTools() throws Exception {
		Config cfg = new Config();
		cfg.useSingleServer()
    	.setAddress("localhost"+":"+6379)
    	.setConnectionPoolSize(10);
		Redisson redisson = Redisson.create(cfg);
		map = redisson.getMap("users-database");
	}
	
	/**
	 * Load a JSON string database into the Redisson (redis) map.
	 * @param db String. The JSON file to load into redis.
	 * @throws Exception on I/O errors.
	 */
	public void loadDatabase(String db) throws Exception {
		map.clear();
		List<User> x = read(db);
		
		for (Object o : x) {
			User u = (User)o;
			map.put(u.name, u);
		}
		System.out.println("Database init complete.");
	}
	
	/**
	 * Print the contents of the REDIS database to stdout.
	 */
	public void printDatabase() {
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String key = it.next();
			User u = map.get(key);
			System.out.println("====> " + key);
			System.out.println(gson.toJson(u));
		}
	}
	
	/**
	 * Save the redis database to disk of the given name.
	 * @param db String. The  name of the file to contain the database.
	 * @throws Exception on IO errors.
	 */
	public void saveDatabase(String db) throws Exception  {
		write(db);
	}
	

	/**
	 * Read the database.json file into this object.
	 * @param db String. The JSON string database to load into Redis.
	 * @return List. A list of users in the database file.
	 * @throws Exception on file errors.
	 */
	public List<User> read(String db) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(db)));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		return users;
	}
	
	/**
	 * Write the database object to the database.json file.
	 * @param dbName String. The filename to contain the Redis database.
	 * @throws Exception on file errors.
	 */
	public void write(String dbName) throws Exception{
		List<User> list = new ArrayList();
		
		List camps = new ArrayList();
		Set set = map.keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			User u = map.get(it.next());
			list.add(u);
		}
		String content = gson.toJson(list);
	    Files.write(Paths.get(dbName), content.getBytes());
	}
	
	
}
