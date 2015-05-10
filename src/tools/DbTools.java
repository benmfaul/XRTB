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
import com.xrtb.common.Campaign;
import com.xrtb.db.User;


public class DbTools {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	ConcurrentMap<String,User> map;
	String dbName;
	
	public static void main(String args[]) throws Exception {
		String db = "database.json";
		if (args.length != 0) {
			db = args[0];
		}
		DbTools tool = new DbTools();
		tool.loadDatabase(db);
		//tool.saveDatabase(db);;
		tool.printDatabase();
		
	}
	
	public DbTools() throws Exception {
		Config cfg = new Config();
		cfg.useSingleServer()
    	.setAddress("localhost"+":"+6379)
    	.setConnectionPoolSize(10);
		Redisson redisson = Redisson.create(cfg);
		map = redisson.getMap("users-database");
	}
	
	public void loadDatabase(String db) throws Exception {
		map.clear();
		List<User> x = read(db);
		
		for (Object o : x) {
			User u = (User)o;
			map.put(u.name, u);
		}
		System.out.println("Database init complete.");
	}
	
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
	
	public void saveDatabase(String db) throws Exception  {
		write(db);
	}
	

	/**
	 * Read the database.json file into this object.
	 * @throws Exception on file errors.
	 */
	public List<User> read(String db) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(db)));
		List<User> users = gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
		return users;
	}
	
	/**
	 * Write the database object to the database.json file.
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
