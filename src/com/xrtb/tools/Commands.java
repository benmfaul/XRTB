package com.xrtb.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xrtb.bidder.ZPublisher;
import com.xrtb.commands.*;

import com.xrtb.jmq.MessageListener;
import com.xrtb.jmq.RTopic;

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

 //   RedissonClient redisson;
 //   DataBaseObject shared;

    static CountDownLatch latch;

    static String uuid = UUID.randomUUID().toString();
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
        String host = "localhost";
        String user = "ben";
        String pport = "6001";
        String sport = "6000";

        Commands c;

        if (args.length == 0) {
            System.out.println(usage());
            return;
        }

        getValue(0 , args, "-host", "localhost");
        getValue(0 , args, "-user", "ben");
        getValue(0 , args, "-p", "6001");
        getValue(0 , args, "-s", "6000");

        c = new Commands(host, Integer.parseInt(pport), Integer.parseInt(sport));

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-help":
                case "-h":
                    System.out.println(usage());
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
                case "-loadcamps":
                    who = getValue(i + 1, args, "-who", "*");
                    String ids = getValue(i + 1, args, "-ids", null);
                    c.sendLoadCampaign(who, user, ids);
                    i = args.length;
                    break;
                case "-delcamps":
                    who = getValue(i + 1, args, "-who", "*");
                    ids = getValue(i + 1, args, "-ids", null);
                    c.sendDeleteCampaign(who, user, ids);
                    i = args.length;
                    break;
                case "-getcamp":
                    who = getValue(i + 1, args, "-who", "*");
                    ids = getValue(i + 1, args, "-id", null);
                    c.sendGetCampaign(who, user, ids);
                    i = args.length;
                    break;
                case "-listcamps":
                    who = getValue(i + 1, args, "-who", "*");
                    c.sendListCampaigns(who);
                    i = args.length;
                    break;
                case "-listbidders":
                    who = getValue(i + 1, args, "-who", "*");
                    c.listBidders(who);
                    i = args.length;
                    break;
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
                    c.sendGetPrice(who, user, cid, crid);
                    i = args.length;
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
                    c.sendSetPrice(who, user, cid, crid, Double.parseDouble(price));
                    i = args.length;
                    break;
                default:
                    System.out.println("Unknown command: " + args[i]);
                    System.exit(0);
            }
        }

       // latch.await(5, TimeUnit.SECONDS);
       // System.exit(0);
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
                "Environment first:\n\n" +
                "[-user username]  sets the username (defaults to 'ben') then followed with:\n" +
                "[-host zerospikehost] [-p pubport] [-s subport] defaults to localhost 6001 6000 followed by:\n\n" +
                "-delcamps [-who * | bidderid] -id campid\n" +
                "-getcamp [who * | bidderid] -cid campid\n" +
                "-getprice [-who * | biddderid] -cid campid -crid creative id\n" +
                "-listbidders\n" +
                "-listcamps\n" +
                "-loadcamps [-who * | bidderid] -ids campid\n" +
                "-setprice [-who * | bidderid] -cid campid -crid creativeid -price n\n" +
                "-startbidder [who * | bidderid]\n" +
                "-stopbidder  [who * | bidderid]\n";
        return str;
    }

    /**
     * Instantiate a connection to localhost (Redisson)
     * Also contains the listener for responses.
     */
    public Commands(String host, int pport, int sport) throws Exception {

        String rcv = "tcp://" + host + ":" + pport + "&responses";
        String send = "tcp://" + host + ":" + sport + "&commands";
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

    /**
     * Get a list of campaigns in the bidders.
     * @param to String. The bidder to send to. Use * and null to ask everyone.
     */
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
     * Delete a campaign from bidders
     * @param who String. Whom to direct the command to.
     * @param id String. Te campaign to remove.
     * @throws Exception on redisson errors.
     */
    public void sendDeleteCampaign(String who, String owner, String id) throws Exception {
        if (who == null)
            who = "*";
        DeleteCampaign cmd = new DeleteCampaign(who,  owner, id);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Start a campaign (by loading into bidder memory
     * @param who String. Whom to direct the command to.
     * @param what String. Te campaign to add. Set to * or null to load them all.
     */
    public void sendLoadCampaign(String who, String owner, String what) throws Exception {
        if (who == null)
            who = "*";

        AddCampaignsList cmd = new AddCampaignsList(who, owner, what);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Start a campaign (by loading into bidder memory
     * @param who String. Whom to direct the command to.
     * @param what String. Te campaign to add. Set to * or null to load them all.
     */
    public void sendGetCampaign(String who, String owner, String what) throws Exception {
        if (who == null)
            who = "*";

        GetCampaign cmd = new GetCampaign(who, owner, what);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Get the price of a campaign/crid
     * @param who String. The bidder to send the command to,
     * @param cid String. The campaign we are looking for.
     * @param crid String. The creative we are looking at the price.
     */
    public void sendGetPrice(String who, String owner, String cid, String crid) {
        if (who == null) {
            who = "*";
        }
        GetPrice cmd = new GetPrice(who, owner, cid, crid);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * Set the price of a campaign/crid
     * @param who String. The bidder to send the command to,
     * @param cid String. The campaign we are looking for.
     * @param crid String. The creative we are looking to set the price.
     */

    public void sendSetPrice(String who, String owner, String cid, String crid, double price) {
        if (who == null) {
            who = "*";
        }
        SetPrice cmd = new SetPrice(who, owner, cid, crid, price);
        cmd.from = uuid;
        commands.add(cmd);
    }

    /**
     * List all the running bidders.
     * @throws Exception on redisson errors.
     */
    public void listBidders(String who) {
        if (who == null) {
            who = "*";
        }
        ListMembers cmd = new ListMembers(who);
        cmd.from = uuid;
        commands.add(cmd);
    }
}
