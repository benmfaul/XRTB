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
 * Simple program to manipulate the Redisson map used as the database in RTB4FREE
 * Arguments:
 * <p>
 * [-redis host:port]		Sets the redis host/port, uses localhost as default
 * <p>
 * [-clear]					Drop the database in Redis
 * <p>
 * [-dump]					Print the contents of the database.
 * <p>
 * [-load fname]			Load the file into the database
 * <p>
 * [-write fname]			Write the database to the file (in JSON form)
 * <p>
 * eg java -jar xrtb.jar tools.DbTools -clear -load mydb.json -dump
 * <p>
 * By default, no arguments loads database.json into the redisson database
 * 
 */

public class DbTools {
	/** JSON object builder, in pretty print mode */
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	/** The redisson backed shared map that represents this database */
	ConcurrentMap<String,User> map;
	/** The redisson proxy object behind the map */
	Redisson redisson;
	/** The redisson configuration object */
	Config cfg = new Config();
	

	/**
	 * Execute the database commands. With no arguments loads database.json into
	 * the Redisson map stored in Redis on local host.
	 * 
	 * @param args String[]. List of arguments.
	 * @throws Exception on I/O or Redis errors.
	 */
	public static void main(String args[]) throws Exception {
		String db = "database.json";
		String redis = "localhost:6379";
		if (args.length != 0) {
			db = args[0];
		}
		
		int i = 0;
		DbTools tool = null;
		if (args.length > 0) {
			while( i <args.length) {
				if (args[i].equals("-redis")) {
					redis = args[i+1];
					i+= 2;
				} else
				if (args[i].equals("-clear")) {
					i++;
					if (tool == null)
						tool = new DbTools(redis);
					tool.clear();
				} else
				if (args[i].equals("-dump")) {
					if (tool == null)
						tool = new DbTools(redis);
					i++;
				} else
				if (args[i].equals("-load")) {
					if (tool == null)
						tool = new DbTools(redis);
					tool.loadDatabase(args[i+1]);
					i+=2;
				} else
				if (args[i].equals("-write")) {
					if (tool == null)
						tool = new DbTools(redis);
					tool.saveDatabase(args[i+1]);
					i+=2;
				}
			}
		} else {
			tool = new DbTools(redis);
			tool.loadDatabase(db);
			tool.saveDatabase(db);
			tool.printDatabase();
		}
		
		tool.shutdown();
	}
	
	/**
	 * Drop the database in memory
	 */
	public void clear() {
		map.clear();
	}
	
	public void shutdown() {
		redisson.shutdown();
	}
	
	/**
	 * Simple constructor. Used to setup redisson to local host.
	 * 
	 * @param redis String. The redis host:port string definition.
	 * @throws Exception on Redis connection errors.
	 */
	
	public DbTools(String redis) throws Exception {
		cfg.useSingleServer()
    	.setAddress(redis)
    	.setConnectionPoolSize(10);
		redisson = Redisson.create(cfg);
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
