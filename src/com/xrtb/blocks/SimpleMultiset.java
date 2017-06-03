package com.xrtb.blocks;

import java.io.BufferedReader;



import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


/**
 * Implements the Multiset counter for UTF-8 strings. Builds a Guava Multiset from file or S3 object.
 * @author Ben M. Faul
 *
 */
public class SimpleMultiset extends LookingGlass {
	Multiset ms = HashMultiset.create();
	int size = 0;

	/**
	 * Constructor for the File/S3 object to Bloom filter.
	 * @param name String. The name of the bloom filter.
	 * @param file String, the file name.
	 * @throws Exception on File Errors.
	 */
	public SimpleMultiset(String name, String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		System.out.print("Initialize  Multiset: " + name + " from " + file + ", enttries = ");
		while(br.readLine() != null) {
			size++;
		}
		br.close();
		
		br = new BufferedReader(new FileReader(file));
		makeFilter(br);
		System.out.println(ms.size() +  " elements");
		
		symbols.put(name, ms);
	}
	
	/**
	 * Constructor for the S3 version of the Multiset filter.
	 * @param name String. The name of the object.
	 * @param object S3Object. The object that contains the file.
	 * @throws Exception on S3 errors.
	 */
	public SimpleMultiset(String name, S3Object object) throws Exception {
		InputStream objectData = object.getObjectContent();
		BufferedReader br=new BufferedReader(new InputStreamReader(objectData));
		makeFilter(br);
		System.out.println(size + " elements");
		
		symbols.put(name, ms);
	}
	
	/**
	 * Reads a file or S3 object line by line and loads the filter.
	 * @param br BufferedReader. The line-by-line reader.
	 * @throws Exception on I/O errors.
	 */
	void makeFilter(BufferedReader br) throws Exception {
		for (String line; (line = br.readLine()) != null;) {
			line = line.trim();
			ms.add(line);
		}
		br.close();
	}

	/**
	 * Returns the Multiset filter for your use.
	 * @return Multiset. The Guava multiset of the contents of this file.
	 */
	public Multiset getMultiset() {
		return ms;
	}
	
	/**
	 * Returns the number of elements.
	 * @return int. The number of elements in the filter.
	 */
	public int getMembers() {
		return size;
	}
}
