XRTB
=====================

A Real Time Broker (RTB) 2.1 bidding engine written in Java 1.8

This RTB project contains 3 major components: 1) A Real Time Bidding engine; 2) A Simulator for sending
test bids to the bidder; 3) A click through counter for receiving click through data.

This project is for those fairly familiar with RTB. With a basic understanding of RTB, this project will get you
up and running with a commercial grade bidder in a short period of time.

Note, a major component of a commercial RTB system is a database for doing all those production things like campaign management, bid tracking, win handling and click through accounting. This project doesn't include any of that, However, the XRTB uses a publish/subscribe system (in REDIS) that will allow you to connect these functions of the bidder into your own custom database.

A production bidding enterprise would most likely require multiple bidding engines running behind a firewall. This project
does not provide NGINX or AWS Load Balancer (or similar) infrastructure for this, you will need to tailor the 
administration of the XRTB to deal with these production issues. The XRTB comes out of the box ready to run in
a multi-server bidding farm, you just need to lash it up and administer it.


BUILDING THE SYSTEM
=======================

You will need ANT, JAVA 1.8, JACKSON, JETTY and REDIS installed to build the system. The libraries required are already 
placed in the ./libs directory

If you use Eclipse or some other IDE, make your project and include the ./libs directory.

Note, there is a .gitignore file included.

Now use ant to build the system:

$ant

----> Will create ./libs/xrtb.jar    -- Which is the jar file of the project.

$ant javadoc

----> Will create javadoc documents in the ./javadoc directory.

$ant junitreport

----> Will run the JUNIT test cases and output the reports in the ./reports directory


RUNNING THE BIDDING SYSTEM
===========================

In order to run the bidder, you will need a sample campaign. The campaigns are stored in a JSON file that the bidder loads when it starts. There is a sample campaign called "./Campaigns/payday.json' you can use to get started. The campaign describes
the various constraints you are looking for (such as price, size, etc), the form of the tracking pixel, and the returned HTML
to the exchange (the 'bid'). There is a README.md file in the ./Campaigns directory that explains the format of the campaign, and how to build your constraints.

Change directory to ./libs

There is a  "./libs/Run-xrtb.sh" file in this directory. Simply execute this script. Note, the script loads the 
../Campaigns/payday.json. This file contains the operational paraameters for the bidder AND sample campaigns. In the case
of payday.json, there is only one campaign defined, but you may define any number of campaigns. Note, it is also possible to load and unload campaigns using the REDIS publish/subscribe channel. Read the com.xrtb.commands.Basic.java JAVADOC page for more information on how to send the commands through REDIS.

$sh ./Run-xrtb.sh

RUNNING THE SIMULATOR
============================

The simulator provides you with the ability to send test bids to your campaigns loaded in the bidder. You fill out an HTML
page for what the bid should look like, press Test button and the bid request is sent. The bidders response JSON is
returned plus a visual display of your tracking pixel.

The simulator reads a sample campaign construct in the ./web directory called "./web/config.json" file. This file sets up those parameters you can change through the web page. This file looks exactly like the ../Campaigns file. So look in the
README.md in ./Campaigs directory for more information.

**** WARNING: DO NOT MODIFY ./Campaigns/payday.json ALL OF THE TEST CASES DEPEND ON THIS FILE ******



