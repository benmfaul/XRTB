package com.xrtb.blocks;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


import com.amazonaws.services.s3.model.S3Object;
import com.github.mgunlogson.cuckoofilter4j.CuckooFilter;
import com.google.common.hash.Funnels;


/**
 * Implements the BloomFilter for UTF-8 strings. Builds a Guava bloom filter from file or S3 object.
 * @author Ben M. Faul
 *
 */
public class Cuckoo extends LookingGlass {
	CuckooFilter<CharSequence> cuckooFilter;;

	/**
	 * Constructor for the File/S3 object to Bloom filter.
	 * @param name String. The name of the bloom filter.
	 * @param file String, the file name.
	 * @throws Exception on File Errors.
	 */
	public Cuckoo(String name, String file) throws Exception {
		File f = new File(file);
		long size = f.length();
		BufferedReader br = new BufferedReader(new FileReader(file));
		makeFilter(br,size);
		
		symbols.put(name, cuckooFilter);
	}
	
	/**
	 * Constructor for the S3 version of the Bloom filter.
	 * @param name String. The name of the object.
	 * @param object S3Object. The object that contains the file.
	 * @throws Exception on S3 errors.
	 */
	public Cuckoo(String name, S3Object object, long size) throws Exception {
		InputStream objectData = object.getObjectContent();
		BufferedReader br=new BufferedReader(new InputStreamReader(objectData));
		makeFilter(br,size);
		symbols.put(name, cuckooFilter);
	}
	
	/**
	 * Reads a file or S3 object line by line and loads the filter.
	 * @param br BufferedReader. The line-by-line reader.
	 * @throws Exception on I/O errors.
	 */
	void makeFilter(BufferedReader br, long sz) throws Exception {
		String[] parts;
		int i;

		
		String line = br.readLine();
		line = line.trim();
		i = 0;
		
		parts = eatquotedStrings(line);
		for (i = 0; i < parts.length; i++) {
			parts[i] = parts[i].replaceAll("\"", "");
		}
		long size = parts[0].length() - 5;
		size = sz / size;
		double fpp = 0.03; // desired false positive probability
		cuckooFilter = new CuckooFilter.Builder<>(Funnels.stringFunnel(Charset.forName("UTF-8")), size).build();
		cuckooFilter.put(parts[0]);
		
	
		while ((line = br.readLine()) != null) {
			parts = eatquotedStrings(line);
			for (i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceAll("\"", "");
			}
			cuckooFilter.put(parts[0]);
		}
		br.close();
	}

	/**
	 * Returns the Bloom filter for your use.
	 * @return BloomFilter. The Guava bloom filter of the contents of this file.
	 */
	public CuckooFilter getCuckoo() {
		return cuckooFilter;
	}
	
	/**
	 * Returns the number of elements.
	 * @return int. The number of elements in the filter.
	 */
	public long getMembers() {
		return cuckooFilter.getCount();
	}
}
