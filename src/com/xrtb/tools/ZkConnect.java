package com.xrtb.tools;


import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * A class for handling configuration, campaign and status data for RTB4FREE
 * @author Ben M. Faul.
 *
 */
public class ZkConnect {
	
	public static List<String> REGIONS = new ArrayList();
	static {
		REGIONS.add("us");
		REGIONS.add("eu");
		REGIONS.add("apac");
		REGIONS.add("test");
	}
	
	public static final int NODECREATED = 1;
	public static final int NODEDELETED = 2;
	public static final int DATACHANGED = 3;
	
	public static String STATUS = "/status";
	public static String CONFIG = "/config";
	public static String DATABASE = "/database";
	public static String LOG	  = "/logs";
	public static String UPDATES  = "/updates";
    private ZooKeeper zk;
    public String path;
    
    private CountDownLatch connSignal = new CountDownLatch(0);
    
    public static void main(String [] args) throws Exception {
    	int i = 0;
    	String cmd = null;
    	String target = null;
    	String fileName = "database.json";
    	String host = "localhost";
    	while(i<args.length) {
    		switch(args[i]) {
    		case "-walk":
    			cmd = "w";
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-init":
    			cmd = "init";
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-print":
    			cmd = "p";
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-file":
    			cmd = "f";
    			fileName = args[i+1];
    			i+=2;
    			break;
    		case "-target":
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-remove":
    			cmd = "r";
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-watch":
    			cmd = "z";
    			target = args[i+1];
    			i+=2;
    			break;
    		case "-zoo":
    			host = args[i+1];
    			i+=2;
    			break;
    		case "-h":
    		case "-help":
    			System.out.println("-file <filename>           Sets the file path for setting data specified with -target");
    			System.out.println("-init <path>               Initializes the path specified.");
    			System.out.println("-print <path>              Print the data at path.");
    			System.out.println("-remove <path>             Deletes (recursively); at path.");
    			System.out.println("-watch <path>              Data watch at path.");
    			System.out.println("-zoo <hostname>            Connect to hostname, default is localhost");
    			System.exit(0);
    		default:
    			System.out.println("Huh? " + args[i]);
    			System.exit(0);
    		}
    			
    	}
    	
    	ZkConnect x = new ZkConnect(host);
    	
    	if (cmd == null)
    		cmd = "w";
    	if (target == null)
    		target = "/rtb4free";
    	
    	if (cmd.equals("w"))
    		x.walkPath(target);
    	if (cmd.equals("i")) {
    		x.remove(target);
    		x.join("/rtb4free/regions/us","bidders",null);
    	}
    	if (cmd.equals("f")) {
    		String content = new String(Files.readAllBytes(Paths.get(fileName)));
    		x.updateNode(target, content.getBytes());
    	}
    	if (cmd.equals("p"))
    		System.out.println(x.getObject(target));
    
    	if (cmd.equals("r"))
    		x.remove(target);
    	
    	if (cmd.equals("z")) {
    		ZTester z = new ZTester(x);
    		x.addWatch(target, z);
    		while(true)
    			Thread.sleep(300000);
    	}
    }
    
    public void addWatch(String target, Zoolander z) throws Exception {
    	zk.exists(target, new Watcher() {
            public void process(WatchedEvent event) {
                  if (z != null)
					try {
						z.callBack(event.getPath(), event.getType());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            }
        });
    }
    
    /**
     * Join the bidder club, get the region's configuration
     * @param path String. The path, up to the region
     * @param name String. the shard name
     * @return String. The configuration for this region, or null
     * @throws Exception on Zookeeper errors
     */
    public String join(String path, String type, String name) throws Exception {
    	String fullPath = path + "/" + type + "/" + name;
    	String tpath = path + "/" + type;
    	if (nodeExists(fullPath)) {
    		this.path = fullPath;
    		this.remove(fullPath);
    		zk.create(fullPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		zk.create(fullPath + STATUS, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		updateNode(fullPath + STATUS, "".getBytes());
    		return getObject(tpath + CONFIG);
    	}
    	if (nodeExists(tpath)) {
    		this.path = fullPath;
    		zk.create(fullPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		zk.create(fullPath + STATUS, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		updateNode(fullPath + STATUS, "".getBytes());
    		return null;
    	}
    	/**
    	 * Doesn't exist, so create it
    	 */
    	String [] roots = path.split("/");
    	String spath = "/" + roots[1];
    	if (nodeExists(spath))
    		remove(spath);
    	zk.create(spath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    	zk.create(spath + "/regions", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    	for (int i=0;i<REGIONS.size();i++) {
        	createRegion(spath,  REGIONS.get(i));
    	}
    	this.remove(fullPath);
		zk.create(fullPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create(fullPath + STATUS, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		updateNode(fullPath + STATUS, "".getBytes());
    	this.path = fullPath;
    	return null; // join(path,type,name);
    }
    
    public void createRegion(String path, String region) throws Exception {
    	String spath = path + "/regions/" + region;
    	zk.create(spath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    	spath += "/bidders";
       	zk.create(spath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
       	zk.create(spath + CONFIG, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		updateNode(spath + CONFIG, "".getBytes());
		zk.create(spath + DATABASE, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		updateNode(spath + DATABASE, "".getBytes());
		
		spath = path + "/regions/" + region + "/crosstalk";
		zk.create(spath , null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create(spath +CONFIG, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		updateNode(spath + CONFIG, "".getBytes());
		zk.create(spath + STATUS, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		updateNode(spath + STATUS, "".getBytes());
    }
    
    public ZkConnect(String path) throws Exception {
    	connect(path);
    }

    //host should be 127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002
    public ZooKeeper connect(String host) throws Exception {
        zk = new ZooKeeper(host, 2101, new Watcher() {
            public void process(WatchedEvent event) {
                if (event.getState() == KeeperState.SyncConnected) {
                    connSignal.countDown();
                }
            }
        });
        connSignal.await();
        return zk;
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    public void createNode(String path, byte[] data) throws Exception
    {
        zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public void updateNode(String path, byte[] data) throws Exception
    {
        zk.setData(path, data, -1);
    }
    
    public void deleteNode(String path) throws Exception
    {
        zk.delete(path,  -1);
    }
    
    public boolean nodeExists(String path) throws Exception 
    {
    	Stat stat = zk.exists(path,  true);
    	if (stat == null)
    		return false;
    	else return true;
    }
    
    public String walkPath(String path) throws Exception {
    	List<String> list = zk.getChildren(path, null);
    	for (String s : list) {
    		System.out.println(path + "/" + s);
    		walkPath(path + "/" + s);
    	}
    	return null;
    }
    
    public void instantiateTree(String path) throws Exception {
    	String [] roots = path.split("/");

    	String newNode = "/" + roots[1];
        if (nodeExists(newNode)) {
        	remove(newNode);
        }
        createNode(newNode, null);
        for (int i=2;i<roots.length; i++) {
    		newNode += "/" + roots[i];
    		createNode(newNode, null);
    	}
    }
   
    
    public void remove(String path) throws Exception {
    	List<String> list = zk.getChildren(path, null);
    	if (list.size() == 0) {
    		Stat stat = zk.exists(path,  true);
    		zk.delete(path, stat.getVersion());
    		return;
    	}
    	for (String s : list) {
    		remove(path + "/" + s);
    	}
    	deleteNode(path);
    }
    
    public void remove() throws Exception {
    	remove(path);
    }
    
    public String getObject(String path) throws Exception {
    	List<String> list = zk.getChildren(path, null);
    	if (list.size()!=0)
    		throw new Exception("Directory node here.");
        byte[] data = zk.getData(path, true, zk.exists(path, true));
        return new String(data);
    }
    
    
    /**
     * Read a configuration file for the named bidder
     * @param path
     */
    public String readConfig(String path) throws Exception {
    	 return getObject(path + CONFIG);
    }
    
    public String readConfig() throws Exception {
    	return getObject(path + CONFIG);
    }
    
    /** 
     * Write a configuration file for the named bidder
     * @param path
     */
    public void writeConfig(String path, String data) throws Exception {
    	updateNode(path + CONFIG, data.getBytes());
    }
    
    public void writeConfig(String data) throws Exception {
    	updateNode(path + CONFIG, data.getBytes());
    }
    
    /**
     * Write a status block for the named bidder
     * @param path
     * @param status
     */
    public void writeStatus(String path, String data) throws Exception {
    	updateNode(path + STATUS, data.getBytes());
    }
    
    public void writeStatus(String data) throws Exception {
    	updateNode(path + STATUS, data.getBytes());
    }
    
    /**
     * Read a status block for a bidder
     * @param path
     * @return
     */
    public String readStatus(String path) throws Exception {
    	return getObject(path + STATUS);
    }
    
    public String readStatus() throws Exception {
    	return getObject(path + STATUS);
    }
    
    public String readDatabase(String path) throws Exception  {
    	return getObject(path + DATABASE);
    }
    
    public String readDatabase() throws Exception {
    	return getObject(path + DATABASE);
    }

}

class ZTester implements Zoolander, Runnable {
	Thread me;
	ZkConnect zk;
	public ZTester(ZkConnect zk) {
		this.zk = zk;
		me = new Thread(this);
		me.start();
		
	}

	public void run() {
		System.out.println("started");
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void callBack(String target, EventType etype) throws Exception {
		System.out.println(target + " got event " + etype.name() + ", num=" + etype.getIntValue());
		if (etype.getIntValue()==ZkConnect.DATACHANGED)
			System.out.println(zk.getObject(target));
		zk.addWatch(target, this);
	}
	
	
}
