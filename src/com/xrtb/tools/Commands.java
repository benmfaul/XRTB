package com.xrtb.tools;

import com.xrtb.RedissonClient;
import com.xrtb.bidder.ZPublisher;
import com.xrtb.commands.*;
import com.xrtb.common.Campaign;
import com.xrtb.common.Configuration;
import com.xrtb.db.DataBaseObject;
import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A simple class that sends and receives commands from RTB4FREE bidders.
 *
 * @author Ben M. Faul
 */

public class Commands {
    /** JSON object builder, in pretty print mode */

    /**
     * The topic for commands
     */
    ZPublisher commands;

    RedissonClient redisson;
    DataBaseObject shared;

    static CountDownLatch latch;

    static String uuid = "crosstalk: " + UUID.randomUUID().toString();
    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Main entry point, see description for usage.
     * -starbidder -n localhost:2000,2001 -all or -id number
     * -stopbidder -all or -id numer
     * -loaddb [filename]
     * -loadcamps -all or -id num -cid cid or 'cid, cid, cid'
     * -printdb [filename]
     * -listbidders
     * -listcamps -all or -id num
     * -echo -all or -id num
     *
     * @param args String[]. The array of arguments.
     */
    public static void main(String[] args) throws Exception {
        String host = null;
        String pport = "6000";
        String sport = "6001";
        String listen = "6002";

        Commands c;

        if (args.length == 0) {
            //System.out.println(usage());
            // return;
        }
        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-host":
                    host = args[i + 1];
                    i += 2;
                    break;
                case "-pub":
                    pport = args[i + 1];
                    i += 2;
                    break;
                case "-sub":
                    sport = args[i + 1];
                    i += 2;
                    break;
                case "-listen":
                    listen = args[i + 1];
                    i += 2;
                    break;
                default:
                    i++;
            }
        }

        latch = new CountDownLatch(1);

        if (host == null) {
            host = Configuration.GetEnvironmentVariable("$PUBSUB", "$PUBSUB", "localhost");
        }

        if (pport == null) {
            pport = Configuration.GetEnvironmentVariable("$PUBPORT", "$PUBPORT", "6000");
        }

        if (sport == null) {
            sport = Configuration.GetEnvironmentVariable("$SUBPORT", "$SUBPORT", "6001");
        }
        if (listen == null) {
            listen = Configuration.GetEnvironmentVariable("$INITPORT", "$INITPORT", "6002");
        }


        c = new Commands(host, Integer.parseInt(pport), Integer.parseInt(sport), Integer.parseInt(listen));

        i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-help":
                    System.out.println(usage());
                    System.exit(1);
                case "-cacheget":
                    String key = getValue(i + 1, args, "-key", null);
                    if (key == null) {
                        System.out.println("-key is required");
                    }
                    c.cacheGet(key);
                    System.exit(1);

                case "-cachedel":
                    key = getValue(i + 1, args, "-key", null);
                    if (key == null) {
                        System.out.println("-key is required");
                    }
                    c.cacheDel(key);
                    Thread.sleep(1000);
                    System.exit(1);
                case "-cacheset":
                    key = getValue(i + 1, args, "-key", null);
                    if (key == null) {
                        System.out.println("-key is required");
                    }
                    String value = getValue(i+1, args, "-value", null);
                    String to = getValue(i+1, args, "-timeout",null);
                    Integer timeout = null;
                    if (to != null) {
                        int tv = Integer.parseInt(to);
                        timeout = new Integer(tv);
                    }
                    c.cacheSet(key,value,timeout);
                    Thread.sleep(1000);
                    c.cacheGet(key);
                    System.exit(1);
                case "-startbidder":
                    String who = getValue(i + 1, args, "-who", "*");
                    c.sendStartBidder(who);
                    i = args.length;
                    break;
                case "-stopbidder":
                    who = getValue(i + 1, args, "-who", "*");
                    c.sendStopBidder(who);
                    i = args.length;
                    break;
                case "-loaddb":
                    c.loadDatabase(args[i + 1]);
                    i = args.length;
                    System.exit(1);
                case "-loadcamps":
                    who = getValue(i + 1, args, "-who", "*");
                    String ids = getValue(i + 1, args, "-ids", null);
                    c.sendLoadCampaign(who, ids);
                    i = args.length;
                    break;
                case "-printdb":
                    c.printDatabase();
                    i = args.length;
                    System.exit(1);
                case "-listcamps":
                    who = getValue(i + 1, args, "-who", "*");
                    c.sendListCampaigns(who);
                    i = args.length;
                    break;
                case "-listcamps-zero":
                    c.listZerospikeCamps();
                    i = args.length;
                    break;
                case "-delcamp":
                    who = getValue(i + 1, args, "-who", "*");
                    String id = getValue(i + 1, args, "-cid", null);
                    c.sendDeleteCampaign(who,id);
                    i = args.length;
                    break;
                case "-listbidders":
                    c.listBidders();
                    i = args.length;
                    System.exit(1);
                case "-getprice":
                    who = getValue(i + 1, args, "-who", "*");
                    String cid = getValue(i + 1, args, "-cid", null);
                    if (cid == null) {
                        System.out.println("Must have a -cid");
                        System.exit(1);
                    }
                    String crid = getValue(i + 1, args, "-crid", null);
                    if (crid == null) {
                        System.out.println("Must have a -crid");
                        System.exit(1);
                    }
                    c.sendGetPrice(who, cid, crid);
                    break;
                case "-setprice":
                    who = getValue(i + 1, args, "-who", "*");
                    cid = getValue(i + 1, args, "-cid", null);
                    if (cid == null) {
                        System.out.println("Must have a -cid");
                        System.exit(1);
                    }
                    crid = getValue(i + 1, args, "-crid", null);
                    if (crid == null) {
                        System.out.println("Must have a -crid");
                        System.exit(1);
                    }
                    String price = getValue(i + 1, args, "-price", null);
                    if (price == null) {
                        System.out.println("Must have a -price");
                        System.exit(1);
                    }
                    c.sendSetPrice(who, cid, crid, Double.parseDouble(price));
                    break;
                default:
                    System.out.println("Unknown command: " + args[i]);
                    i = args.length;
            }
        }

        latch.await(5, TimeUnit.SECONDS);
        System.exit(0);
    }

    /**
     * Get value from arguments.
     * @param start int. The place to start looking in the args list.
     * @param args List. The list of string srguments.
     * @param what String. Argument we are looking for.
     * @param def String. The default value. Can be null.
     * @return String. The value in the args + 1 from where the what string was found.
     */
    public static String getValue(int start, String[] args, String what, String def) {
        for (int i = start; i < args.length; i++) {
            if (args[i].equals(what)) {
                return args[i + 1];
            }
        }
        return def;
    }

    /**
     * Print the usgage for the application.
     * @return String. The help string.
     */
    public static String usage() {
        String str = "" +
                "-cacheget -key thekey\n" +
                "-cacheset -key thekey -value thevalue -timeout nsecs\n" +
                "-delcamp [who * | bidderid] -cid campid\n" +
                "-getprice -who [* | biddderid] -cid campid -crid creative id\n" +
                "-listbidders\n" +
                "-listcamps\n" +
                "-loadcamps [-who * | bidderid] -ids campid\n" +
                "-loaddb filename\n" +
                "-printdb\n" +
                "-setprice -who [* | bidderid] -cid campid -crid creativeid -price nn\n" +
                "-startbidder [* | bidder-id]\n" +
                "-stopbidder  [* | bidder-id]\n";
        return str;
    }

    /**
     * Instantiate a connection to localhost (Redisson)
     * Also contains the listener for responses.
     */
    public Commands(String host, int pport, int sport, int listen) throws Exception {

        String rcv = "tcp://" + host + ":" + sport + "&responses";
        String send = "tcp://" + host + ":" + pport + "&commands";
        redisson = new RedissonClient();
        redisson.setSharedObject(host,pport,sport,listen);
        while(redisson.loadComplete()==false)
            Thread.sleep(1000);
        shared = DataBaseObject.getInstance(redisson);

       RTopic responses = new RTopic(rcv);
        responses.addListener(new MessageListener<BasicCommand>() {
                    @Override
            public void onMessage(String channel, BasicCommand msg) {
                try {

                    if (msg.from != null && msg.from.equals(uuid) == true)
                        return;

                    String content = DbTools.mapper
                            .writer()
                            .withDefaultPrettyPrinter()
                            .writeValueAsString(msg);
                    System.out.println("<<<<<" + content + "\n");
                    if (latch != null)
                        latch.countDown();
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        });
        System.out.println("Commands: " + send);
        System.out.println("Responses: " + rcv);
        commands = new ZPublisher(send);
    }

    public void sendListCampaigns(String to) {
        if (to == null)
            to = "*";
        ListCampaigns cmd = new ListCampaigns(to);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Send a stop bidder command
     * @param who String, Whom to send the command to. * and null are to everyone
     */
    public void sendStopBidder(String who) {
        if (who == null) {
            who = "*";
        }
        StopBidder cmd = new StopBidder(who);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Send a start bidder command
     * @param who String, Whom to send the command to. * and null are to everyone
     */
    public void sendStartBidder(String who) {
        if (who == null) {
            who = "*";
        }
        StartBidder cmd = new StartBidder(who);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Add more users to the redis database
     * @param fileName String, The file name of the data to load in as the database.
     */
    public void loadDatabase(String fileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            List<Campaign> list = mapper.readValue(content,
                    mapper.getTypeFactory().constructCollectionType(List.class, Campaign.class));
            shared.putCampaigns(list);
           System.out.println(content);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * Print the database
     * @throws Exception on redisson errors.
     */
    public void printDatabase() throws Exception {
        List<Campaign> list = shared.getCampaigns();
        String str = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(list);
        System.out.println(str);

    }

    /**
     * Delete a campaign from bidders
     * @param who String. Whom to direct the command to.
     * @param id String. Te campaign to remove.
     * @throws Exception on redisson errors.
     */
    public void deleteCampaign(String who, String id) throws Exception {
        if (who == null)
            who = "*";
        DeleteCampaign cmd = new DeleteCampaign(who, id);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Start a campaign (by loading into bidder memory
     * @param who String. Whom to direct the command to.
     * @param what String. Te campaign to add. Set to * or null to load them all.
     */
    public void sendLoadCampaign(String who, String what) throws Exception {
        if (who == null)
            who = "*";
        if (what == null || what.equals("*")) {
            List<Campaign> list = shared.getCampaigns();
            what = "";
            for (Campaign c : list) {
                what += c.adId + " ";
            }


        }
        AddCampaignsList cmd = new AddCampaignsList(who, what);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Get the price of a campaign/crid
     * @param who String. The bidder to send the command to,
     * @param cid String. The campaign we are looking for.
     * @param crid String. The creative we are looking at the price.
     */
    public void sendGetPrice(String who, String cid, String crid) {
        if (who == null) {
            who = "*";
        }
        GetPrice cmd = new GetPrice(who, cid, crid);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Set the price of a campaign/crid
     * @param who String. The bidder to send the command to,
     * @param cid String. The campaign we are looking for.
     * @param crid String. The creative we are looking to set the price.
     */

    public void sendSetPrice(String who, String cid, String crid, double price) {
        if (who == null) {
            who = "*";
        }
        SetPrice cmd = new SetPrice(who, cid, crid, price);
        cmd.from = uuid;
        commands.add(cmd);
    }


    public void sendDeleteCampaign(String who, String cid) {
        if (who == null) {
            who = "*";
        }
        DeleteCampaign cmd = new DeleteCampaign(who, cid);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * List all the running bidders.
     * @throws Exception on redisson errors.
     */
    public void listBidders() throws Exception {
        List<String> bidders = redisson.getList(NameNode.BIDDERSPOOL);
        System.out.println(bidders);
    }

    /**
     * Return the value of key
     * @param key String key
     * @throws Exception on redisson errors
     */
    public void cacheGet(String key) throws Exception {
        Object x = redisson.get(key);
        System.out.println(x);
    }

    public void cacheSet(String key, String value, Integer to) throws Exception {
        if (to == null)
            redisson.set(key,value);
        else {
            long v = to.intValue();
            redisson.set(key, value, v);
        }
    }

    public void cacheDel(String key) throws Exception {
        redisson.del(key);
    }

    public void listZerospikeCamps() {
        try {
            List<Campaign> list = shared.getCampaigns();
            for (Campaign c : list) {
                System.out.println(c.adId);
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
