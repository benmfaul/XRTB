
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZkConnect {
    private ZooKeeper zk;
    private CountDownLatch connSignal = new CountDownLatch(0);

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
        zk.setData(path, data, zk.exists(path, true).getVersion());
    }

    public void deleteNode(String path) throws Exception
    {
        zk.delete(path,  zk.exists(path, true).getVersion());
    }
    
    public boolean nodeExists(String path) throws Exception 
    {
    	Stat stat = zk.exists(path,  true);
    	if (stat == null)
    		return false;
    	else return true;
    }

    public static void main (String args[]) throws Exception
    {
    	String content = new String(Files.readAllBytes(Paths.get("/home/ben/RTB/XRTB/local/twerkmedia-us.json")), StandardCharsets.UTF_8);
    	
        ZkConnect connector = new ZkConnect();
        ZooKeeper zk = connector.connect("rtb4free.com");
        String newNode = "/rtbfiles";
        if (connector.nodeExists(newNode)) {
        	connector.deleteNode(newNode);
        }
        connector.createNode(newNode, null);
        newNode = "/rtbfiles/file1";
        connector.createNode(newNode, content.getBytes());
        List<String> zNodes = zk.getChildren("/", true);
        for (String zNode: zNodes)
        {
           System.out.println("ChildrenNode " + zNode);   
        }
        byte[] data = zk.getData(newNode, true, zk.exists(newNode, true));
        System.out.println("GetData before setting");
        System.out.println(new String(data));
       // for ( byte dataPoint : data)
       // {
        //    System.out.print ((char)dataPoint);
       // }

     /*   System.out.println("GetData after setting");
        connector.updateNode(newNode, "Modified data".getBytes());
        data = zk.getData(newNode, true, zk.exists(newNode, true));
        for ( byte dataPoint : data)
        {
            System.out.print ((char)dataPoint);
        } */
      //  connector.deleteNode(newNode);
    }

}

