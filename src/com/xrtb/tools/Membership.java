package com.xrtb.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jetty.util.ConcurrentHashSet;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.xrtb.bidder.Controller;
import com.xrtb.common.Configuration;

/**
 * A Membership class fdor use with the Bidder.
 * @author Ben M. Faul
 *
 */
public class Membership extends LookingGlass {

	// The set that contains the members
	volatile Set<String> tree = new TreeSet<String>();
	
	// Might be aerospike backed
	AerospikeClient client;
	
	// The name of the symbol
	String name;
	
	// Aerospike keyl
	Key key;
	
	// Aerospike bin
	Bin bin1;
	
	/**
	 * Generic constructor
	 */
	public Membership() {
		
	}
	
	/**
	 * Create a membership from the file provided. We expect a simple list, one entry per line.
	 * @param name String. The name of the Membership
	 * @param file String. The filename containing the goodies.
	 * @throws Exception on File I/O errors.
	 */
	public Membership(String name, String file) throws Exception {
		this.name = name;
		myMap = null;
		readData(file);
	}
	
	/**
	 * Read data and shove into the tree.
	 * @param file String. The filename
	 * @throws Exception on I/O errors.
	 */
	void readData(String file) throws Exception {		
		BufferedReader br = new BufferedReader(new FileReader(file));

		String[] parts = null;	
		String message = "Initialize Simple Membership: " + file + " as " + name;
		for (String line; (line = br.readLine()) != null;) {
			parts = eatquotedStrings(line);
			for (int i=0;i<parts.length;i++) {
				parts[i] = parts[i].replaceAll("\"","");
			}
			tree.add(parts[0]);
		}
		
		symbols.put(name, this);
		// System.out.format("[%s] - %d - %s - %s - %s\n",Controller.sdf.format(new Date()), 1, Configuration.instanceName, this.getClass().getName(),message);
	}
	
	/**
	 * Constructor for loading a membership list into Aerospike
	 * @param name String. The symbol name.
	 * @param fileName String. The filename with data.
	 * @param client AerospikeClient. Pointer to your aerospike client.
	 * @throws Exception on file and aerospike errors.
	 */
	public Membership(String name, String fileName, AerospikeClient client) throws Exception {
		this.name = name;
		this.client = client;
		readData(fileName);
		key = new Key("test", "database", "rtb4free");
		bin1 = new Bin(name, tree);
		client.put(null, key, bin1);
		tree = null;
	}
	
	/**
	 * Read only constructor (does not load the data) for use with Aerospike.
	 * @param name String. The name of the symbol.
	 * @param client AerospikeClient. The aerospike to use.
	 */
	public Membership(String name, AerospikeClient client) {
		this.name = name;
		this.client = client;
		key = new Key("test", "database", "rtb4free");
		bin1 = new Bin(name, tree);
		client.put(null, key, bin1);
		tree = null;
	}
	
	/**
	 * Does the key exist in the member
	 * @param key Object. The key we are looking for.
	 * @return Boolean. Returns the object if the object exists in the membership, otherwise returns null.
	 */
	@Override
	public Object query(Object tkey) {
		if (client == null) {
			if (tree.contains(tkey))
				return tkey;
		} else {
			Record record = client.get(null, key);
			Set<String> receivedList = (Set<String>) record.getValue(name);
			return receivedList.contains(tkey);
		}
		return null;
	}
	
	/**
	 * Let's test this mess.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		AerospikeClient client = new AerospikeClient("localhost",3000);
		Membership m = new Membership("c1x-cookies", "/home/ben/Downloads/c1x_cookies.csv", client);
		System.out.println(m.query("9786B01215534DEB9AAC2D5FEE23A497"));
	}
}
