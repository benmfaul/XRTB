package com.xrtb.blocks;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


import com.amazonaws.services.s3.model.S3Object;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;


/**
 * Implements the BloomFilter for UTF-8 strings. Builds a Guava bloom filter from file or S3 object.
 * @author Ben M. Faul
 *
 */
public class Bloom extends LookingGlass {
	BloomFilter<CharSequence> bloomFilter;
	int size;
	
	/**
	 * Constructor for the File/S3 object to Bloom filter.
	 * @param name String. The name of the bloom filter.
	 * @param file String, the file name.
	 * @throws Exception on File Errors.
	 */
	public Bloom(String name, String file) throws Exception {
		File f = new File(file);
		long size = f.length();
		BufferedReader br = new BufferedReader(new FileReader(file));
		makeFilter(br,size);
		
		symbols.put(name, bloomFilter);
	}
	
	/**
	 * Constructor for the S3 version of the Bloom filter.
	 * @param name String. The name of the object.
	 * @param object S3Object. The object that contains the file.
	 * @throws Exception on S3 errors.
	 */
	public Bloom(String name, S3Object object, long size) throws Exception {
		InputStream objectData = object.getObjectContent();
		BufferedReader br=new BufferedReader(new InputStreamReader(objectData));
		makeFilter(br,size);
		
		symbols.put(name, bloomFilter);
	}
	
	/**
	 * Reads a file or S3 object line by line and loads the filter.
	 * @param br BufferedReader. The line-by-line reader.
	 * @throws Exception on I/O errors.
	 */
	void makeFilter(BufferedReader br, long size) throws Exception {
		String[] parts;
		int i;
		long sz;
		
		double fpp = 0.03; // desired false positive probability
		
		String line = br.readLine();
		sz = line.length() - 5;
		sz = size / sz;
		parts = eatquotedStrings(line);
		this.size = 1;
		for (i = 0; i < parts.length; i++) {
			parts[i] = parts[i].replaceAll("\"", "");
		}
		
		bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")), sz,fpp);
		bloomFilter.put(parts[0]);
		
		while ((line = br.readLine()) != null) {
			parts = eatquotedStrings(line);
			for (i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceAll("\"", "");
			}
			bloomFilter.put(parts[0]);
			this.size++;
		}
		br.close();
	}
	
	/**
	 * Returns the Bloom filter for your use.
	 * @return BloomFilter. The Guava bloom filter of the contents of this file.
	 */
	public BloomFilter getBloom() {
		return bloomFilter;
	}
	
	/**
	 * Check if this key is possibly in the bloom filter
	 * @param key String. The key to test for.
	 * @return boolean. Returns false if it is not in the filter. Returns true if it possibly is in there.
	 */
	public boolean isMember(String key) {
		return bloomFilter.mightContain(key);
	}
	
	/**
	 * Returns the number of elements.
	 * @return int. The number of elements in the filter.
	 */
	public long getMembers() {
		return size;
	}
}
