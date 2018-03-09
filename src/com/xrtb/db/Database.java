package com.xrtb.db;

import com.xrtb.RedissonClient;
import com.xrtb.common.Campaign;
import com.xrtb.pojo.BidRequest;
import com.xrtb.tools.DbTools;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A class that makes a simple database for use by the Campaign admin portal
 * @author Ben M. Faul
 *
 */
public enum Database {
	
	INSTANCE; 
	
	public static final String USERS_DATABASE = "users-database";
	/** The name of the database file */
	transient public static String DB_NAME = "database.json";
	/** The file's encoding */
	final static Charset ENCODING = StandardCharsets.UTF_8;
	/** A list of users, this is the root node of the database (a list of users, which has a name and a map of campaigns */
	//public List<User> users;
	
	static volatile DataBaseObject shared = null;
	static volatile RedissonClient redisson;

	public void clear() throws Exception {
		shared.clear();
	}

	/**
	 * Return the redisson client object.
	 * @return RedissonClient. The client used for shared access
	 */
	public static RedissonClient getRedissonClient() {
		return redisson;
	}

	/**
	 * Return the shared object
	 * @return DataBaseObject. The shared database object.
	 */
	public static DataBaseObject getShared() {
		return shared;
	}

	public static Database getInstance()  {
		return INSTANCE;
	}
	
	/**
	 * Open (and create if necessary) the database file
	 */
	public static Database getInstance(RedissonClient r) {
		redisson = r;
		try {
			
			shared = DataBaseObject.getInstance(redisson);
			BidRequest.blackList = shared.set;

			List<Campaign> list = shared.getCampaigns();
					for (Campaign c : list) {
						c.encodeAttributes();
						c.encodeCreatives();
					}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return INSTANCE;
	}
	
	public void reload() {
		try {
			
			shared = DataBaseObject.getInstance(redisson);
			BidRequest.blackList = shared.set;


            shared = DataBaseObject.getInstance(redisson);
            BidRequest.blackList = shared.set;

            List<Campaign> list = shared.getCampaigns();
            for (Campaign c : list) {
                c.encodeAttributes();
                c.encodeCreatives();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Return a campaign of the given name and adId.
	 * @param id String. the adId of the campaign to return.
	 * @return Campaign. The campaign to return.
	 */
	public Campaign getCampaign(String id) throws Exception {
        List<Campaign> list = shared.getCampaigns();
			for (Campaign c : list) {
				if (c.adId.equals(id))  {
					return c;
				}
			}
		return null;
	}

    /**
     * Return the list of campaigns in the database
     * @return List. The list of campaigns.
     * @throws Exception on access errors.
     */
	public List<Campaign> getCampaigns() throws Exception {
        return shared.getCampaigns();
    }

    /**
     * Edit a campaign in place
     * @param x Campaign. The campaign to replace into the list.
     * @return Campaign. Returns x if adId was found and x is swapped in. else returns null.
     * @throws Exception on access errors.
     */
    public Campaign editCampaign(Campaign x) throws Exception  {
        List<Campaign> list = shared.getCampaigns();
        for (Campaign c : list) {
            if (c.adId.equals(x.adId))  {
                list.remove(c);
                list.add(c);
                update(list);
                return x;
            }
        }
        return null;
    }

	
	/**
	 * Create a stub campaign from 'stub.json'
	 * @param name String. The user name.
	 * @param id String. The adId to use for this campaign.
	 * @return Campaign. The campaign that was created.
	 * @throws Exception on file errors.
	 */
	public Campaign createStub(String name, String id) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get("stub.json")));
		Campaign c = new Campaign(content);
		c.adId = name + ":" + id;
		return c;
	}
	
	/**
	 * Update the user map in the global map
	 * @param u User. The user object to put into global context.
	 */
	public  void update(List<Campaign> u) throws Exception {
		shared.put(u);
	}

	
	/**
	 * Delete a campaign for the specified user, of the provided adId
	 * @param adId String. The adid of the campaign to delete.
	 * @return List. The resulting list of campaigns of this user.
	 * @throws Exception on file errors.
	 */
	public List deleteCampaign(String adId) throws Exception {
	    List<Campaign> list = shared.getCampaigns();
		for (int i=0; i< list.size();i++) {
			Campaign c = list.get(i);
			if (c.adId.equals(adId)) {
				list.remove(i);
				update(list);
				return list;
			}
		}
		return null;
	}

	
	/**
	 * Read the database.json file into this object.
	 * @param db String. The name of the JSON database file to read
	 * @return List. A list of user objects in the database.
	 * @throws Exception on file errors.
	 */
	public List<Campaign> read(String db) throws Exception {
		String content = new String(Files.readAllBytes(Paths.get(db)));

		return DbTools.mapper.readValue(content,
					DbTools.mapper.getTypeFactory().constructCollectionType(List.class, Campaign.class));
	}
	
	/**
	 * Write the database object to the database.json file.
	 * @throws Exception on file errors.
	 */
	public void write() throws Exception{
		String content = DbTools.mapper.writer().withDefaultPrettyPrinter().writeValueAsString(shared.getCampaigns());
	    Files.write(Paths.get(DB_NAME), content.getBytes());
	    
	}	
	
}
