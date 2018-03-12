package com.xrtb.common;

import com.amazonaws.services.s3.model.S3Object;
import com.xrtb.blocks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ben on 7/17/17.
 */

public class AwsWorker implements Runnable {

    /** Logging object */
    static final Logger logger = LoggerFactory.getLogger(AwsWorker.class);
    private String type;
    private String name;
    private S3Object object;
    private long size;
    private String message;

    public AwsWorker(String type, String name, S3Object object, long size){
        this.type = type;
        this.name = name;
        this.object = object;
        this.size = size;
    }

    @Override
    public void run() {
        try {
            switch (type) {
                case "range":
                case "cidr":
                    NavMap map = new NavMap(name, object);
                    message = "Added NavMap " + name + ": has " + map.size() + " members";
                    break;
                case "set":
                    SimpleSet set = new SimpleSet(name, object);
                    message = "Initialize Set: " + name + " from S3, entries = " + set.size();
                    break;
                case "bloom":
                    Bloom b = new Bloom(name, object, size);
                    message = "Initialize Bloom Filter: " + name + " from S3, members = " + b.getMembers();
                    break;

                case "cuckoo":
                    Cuckoo c = new Cuckoo(name, object, size);
                    message = "Initialize Cuckoo Filter: " + name + " from S3, entries = " + c.getMembers();
                    break;
                case "multiset":
                    SimpleMultiset ms = new SimpleMultiset(name, object);
                    message = "Initialize Multiset " + name + " from S3, entries = " + ms.getMembers();
                    break;
                default:
                    message = "Unknown type: " + type;
            }
        } catch (Exception error) {
            logger.error("Error reading {}, problem: {}", name, error.toString());
        } finally {
            try {
                object.close();
            } catch (IOException e) {
                logger.error("Error closing object {}, problem: {}", name, e.toString());;
            }
        }
        logger.debug("*** {}",message);


    }

    @Override
    public String toString(){
        return message;
    }
}