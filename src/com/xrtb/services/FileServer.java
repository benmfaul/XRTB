package com.xrtb.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

/**
 * A file server, TCP based, that listens on a well known socket, any clients connecting then are provided with a socket they
 * caan receive a copy of the unexpired keys in the cache database on file.
 */

public class FileServer implements Runnable {

    /** The object map on file */
    private HTreeMap objects;
    /** The server socket we use to listen on. */
    private ServerSocket ss;
    /** The tcp socket we will build the server socket with. */
    private int  port;
    /** The error logger */
    private Logger logger;
    /** My thread */
    private Thread me;

    /**
     * A Server that returns unexpired key/data from the database.
     * @param logger Logger. The error logger.
     * @param objects HTreeMap. The map of data on disk.
     * @param port int. The port we will listen for clients to connect.
     * @throws Exception on network and file errors.
     */
    public FileServer( Logger logger, HTreeMap objects, int port) throws Exception {
        this.objects = objects;
        this.port = port;
        this.logger = logger;
        ss = new ServerSocket(port);

        logger.info("File server listening on port: {}", port);
        me = new Thread(this);
        me.start();
    }

    /**
     * The place where we wait for clients to connect on.
     */
    public void run() {
        while (true) {
            try {
                Socket clientSock = ss.accept();
                new Relay(logger,objects,clientSock);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * The worker that takes a connected socket to a client, and returns the key/data from the file.
 */
class Relay implements Runnable {
    /** My thread */
    Thread me;
    /** The data on disk. */
    HTreeMap objects;
    /** The socket to the client */
    Socket client;
    /** The error logger */
    Logger logger;
    /** A JSON constructor using Jackson */
    ObjectMapper mapper = new ObjectMapper();

    /**
     * Create a file relay for the database.
     * @param logger Logger. Error logger object.
     * @param objects HTreeMap. The data on disk.
     * @param client Socket. The connection to the subscriber.
     */
    public Relay(Logger logger, HTreeMap objects, Socket client) {
        this.logger = logger;
        this.objects = objects;
        this.client = client;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        me = new Thread(this);
        me.start();
    }

    /**
     * THe thread that does the work. Iterates through the mapped database and returns all the unexpired data. The
     * clients will create their caches from this. TBD. Not very efficient. But works well enough. It is only
     * used once, at the beginning of the client connecting to the broker. After this, all updates are in real time
     * over the pub/sub channel.
     */
    public void run() {
        Set<HTreeMap.Entry> elements = objects.getEntries();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(client.getOutputStream(), "UTF-8");
        } catch (Exception error) {
            error.printStackTrace();
            logger.error("Encountered error transferring to {}, error: {}",client,error.toString());
            return;
        }

        logger.info("Starting transfer to {}, elements: {}", client,elements.size());
        long time = System.currentTimeMillis();
        int count = 0;
        for (Map.Entry x : elements) {
            count++;
            try {
                String buffer = mapper.writeValueAsString(x.getValue());
                String preamble = String.format("% 6d", buffer.length());
                osw.append(preamble, 0, preamble.length());
                osw.append(buffer);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error in transfer to {}, error: {}", client,e.toString());
                try {
                    osw.close();
                } catch (IOException e1) {
                }
                return;
            }
        }
        try {
            osw.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        time /= 1000;
        logger.info("Transfer to {}, of {} objects, complete in {} seconds.",client,count,time);
    }
}