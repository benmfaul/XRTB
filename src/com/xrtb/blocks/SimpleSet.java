package com.xrtb.blocks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.Sets;

/**
 * A guava set. Reads a file or S3 object and shoves the lines into a guava set.
 * @author Ben M. Faul
 *
 */
public class SimpleSet extends LookingGlass {
	Set<String> set = Sets.newHashSet();
	String message;

	/**
	 * A simple set. Reads from file, shoves into Set, line by line.
	 * @param name String. The name of the object.
	 * @param file String. The file name.
	 * @throws Exception on I/O errors.
	 */
	public SimpleSet(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		message = "Initialize Simple Membership: " + file + " as " + name;

		makeSet(br);
		
		symbols.put(name,set);
	}
	
	/**
	 * A simple set. Reads S3 object and put into the Set.
	 * @param name String. The name of the object.
	 * @param object S3Object. The S3 object to read.
	 * @throws Exception on S3 or I/O options.
	 */
	public SimpleSet(String name, S3Object object) throws Exception {
		InputStream objectData = object.getObjectContent();
		BufferedReader br=new BufferedReader(new InputStreamReader(objectData));
		message = "Initialize Simple Membership: " + object.getBucketName() + " as " + name;
		makeSet(br);
		
		symbols.put(name, this);
	}
	
	/**
	 * Make the set from a buffered reader.
	 * @param br BufferedReader. Read line-by-line and place in the set.
	 * @throws Exception on I/O errors.
	 */
	void makeSet(BufferedReader br) throws Exception {
		String[] parts = null;
		int i;
		for (String line; (line = br.readLine()) != null;) {
			parts = eatquotedStrings(line);
			for (i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceAll("\"", "");
			}
			set.add(parts[0]);
		}
		br.close();
	}
	
	/**
	 * Return the simple set size
	 * @return int. Returns the size.
	 */
	public int size() {
		return set.size();
	}
	
	/**
	 * Return the Set.
	 * @return Set. The Guava set.
	 */
	public Set<String> getSet() {
		return set;
	}
}
