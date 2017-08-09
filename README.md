XRTB
=====================

NOTE: THIS IS A WORK IN PROGRESS

===========================

A Real Time Bidding (RTB) 2.3 engine written in Java 1.8

This RTB project contains 3 major components: 1) A Real Time Bidding engine; 2) A Test page for sending
test bids to the bidder; 3) A campaign manager for creating advertising campaigns.

This project is for those fairly familiar with RTB. With a basic understanding of RTB, this project will get you up and running with a commercial grade bidder in a short period of time.

Note, a major component of a commercial RTB system is a database for doing all those production things like campaign management, bid tracking, win handling and click through accounting. This project doesn't include any of that, However, the XRTB uses a publish/subscribe system (ZeroMQ) that will allow you to connect these functions of the bidder into your own custom database.

A production bidding enterprise would most likely require multiple bidding engines running behind a firewall. This project does not provide NGINX or AWS Load Balancer (or similar) infrastructure for this, you will need to tailor the 
administration of the XRTB to deal with these production issues. The XRTB comes out of the box ready to run in
a multi-server bidding farm, you just need to lash it up and administer it.


BUILDING THE SYSTEM
=======================
This is a maven project.

You will need Maven installed to build the system. The libraries required are automatically retrieved by Maven. To see the dependencies, Look in the pom.xml file

If you use Eclipse make sure you use this as a maven project

Note, there is a .gitignore file included.

Build the Site
----------------------------------------
Now use Maven to build the system

$mvn site

----> This will compile the sources, generate the API docs and the JUNIT tests.

The API documentation in target/site/apidocs/allclasses-frame.html
The Surefire reports are in target/site/surefire-report.html

Create the All Inclusive Jar File
--------------------------------------
Now create the all inclusive jar file:

$mvn assembly:assembly -DdescriptorId=jar-with-dependencies  -Dmaven.test.skip=true

Now you have to decide whether to use Aerosspike (for multi-bidder support) or Cache2k (standalone)

MAKE YOUR LOCAL CONFIGURATION FILES
=============================================
RTB4FREE has three configuration files. The log4j.properties file controls the logging of the application. 
The sample database is called "database.json" which is used to initialize the 
Aerospike database, or, if not using Aerospike, to act as the database for a stand a lone bidder. The second 
configuration file is Campaigns/payday.json which sets up the operational parameters for your bidder. 
Neither of these files exist after you do the GIT clone (or subsequent GIT pull). You need to make these two 
files on your instance by copying the samples:

$cd XRTB
$mkdir logs
$cp sampledb.json database.json
$cp Campaigns/samplecfg.json Campaigns/payday.json
									
If you forget this step, RTB4FREE will not start. These files are kept local on your instance so that changes 
you make to Campaigns/payday.json and database.json don't block your ability to GIT pull to get updates for the bidder.


THERE ARE 2 VERSIONS OF RTB4FREE
=============================================
RTB4FREE comes in 2 versions. One is a standalone system, and is intended for running on a single instance. It requires
no Aerospike support. Instead it uses an embedded Cache2k cache. Use this version if you just want to play around with the
system. If you plan to build a production DSP with multiple bidders, use the Aerospike Enabled.


CACHE2K (NO AEROSPIKE) ENABLED RTB4FREE
=============================================
This is a stand-alone system, and requires no Aerospike support. Notwithstanding any ability to scale to multiple
bidders - it is the fastest version of RTB4FREE. 

Instead of a distributed Aerospike-based cache, the Cache2k system is embedded in the bidder.

Step 1
--------------------------------------------------
Modify the Campaigns/payday.json file as follows: Look for the "aerospike" object Change it's name
to NOaerospike. When done it will look like this:

"NOaerospike": {
	"host": "localhost",
	"port": 3000
},

Step 2
--------------------------------------------------
Modify localhost in Campaigns/payday.json and ./database.json. If you are going to test everything with localhost, you
can skip this step. Otherwise, you need to change pixel-Tracking, winUrl and redirect-url in payday.json and localhost 
entries in database,json. Fortunately, we have a build in program for that. Presume your IP address is 192.188.62.6. 
This will change the all files for you:

$cd XRTB
$tools/config-website -address 192.188.62.6



Step 3
-------------------------------------------------
Start the RTB4FREE FREE bidder and test it:

In one window do:

$cd XRTB
$tools/rtb4free

In another window send it a bid request:

$cd XRTB/shell
$./curltest.sh

You should see the JSON returned for the bid request. An example is shown here:

{"seatbid":[{"seat":"seat1","bid":[{"impid":"35c22289-06e2-48e9-a0cd-94aeb79fab43-1","id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","price":1.0,"adid":"ben:payday","nurl":"http://localhost:8080/rtb/win/smaato/${AUCTION_PRICE}/42.378/-71.227/ben:payday/23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43","cid":"ben:payday","crid":"23-1-skiddoo","iurl":"http://localhost:8080/images/320x50.jpg?adid=ben:payday&bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43","adomain": ["originator.com"],"adm":"<ad xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"smaato_ad_v0.9.xsd\" modelVersion=\"0.9\"><imageAd><clickUrl>http://localhost:8080/redirect/exchange=smaato/ben:payday/creative_id=23-1-skiddoo/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43?url=http://localhost:8080/contact.html?99201&amp;adid=ben:payday&amp;crid=23-1-skiddoo</clickUrl><imgUrl>http://localhost:8080/images/320x50.jpg?adid=ben:payday&amp;bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43</imgUrl><width>320</width><height>50</height><toolTip></toolTip><additionalText></additionalText><beacons><beacon>http://localhost:8080/pixel/exchange=smaato/ad_id=ben:payday/creative_id=23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43</beacon></beacons></imageAd></ad>"}]}],"id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","bidid":"35c22289-06e2-48e9-a0cd-94aeb79fab43"}


AEROSPIKE (MULTI BIDDER) ENABLED RTB4FREE
=============================================
This is the multi-bidder enabled version of RTB4FREE. If you plan to run more than one bidder instance,
or a separate instance to handle win notifications you need to use this version of RTB4FREE.

Step 1
--------------------------------------------------
Get Aerospike up and running somewhere on your network. Look here: www.aerospike.com

Step 2
--------------------------------------------------
Modify localhost in Campaigns/payday.json and ./database.json. If you are going to test everything with localhost, you
can skip this step. Otherwise, you need to change pixel-Tracking, winUrl and redirect-url in payyday.json and localhost 
entries in database,json. Fortunately, we have a build in program for that. Presume your IP address is 192.188.62.6 and Aerospike
is running on the same bidder: This will change the all files for you:

$cd XRTB
$tools/config-website -address 192.188.62.6

Or, if Aerospike is on a different host, add the additional -aero parameter to include that host. For example, presume
Aerospike is running on 192.188.62.66:

$cd XRTB
$tools/config-website -address 192.188.62.6 -aero 192.188.62.66

Step 3
---------------------------------------------------
Load the database.json into Aerospike. If Aerospike is on the same server as the bidder:

$cd XRTB
$tools/load-database

Or, if Aerospike is running on a different host, say 192.188.62.66 use:

$cd XRTB
$tools/load-database -db database.json -aero 192.188.62.66:3000

Step 5
----------------------------------------------------
Start the RTB4FREE bidder and test it:

In one window do:

$cd XRTB
$tools/rtb4free

In another window send it a bid request:

$cd XRTB/shell
$./curltest.sh

You should see the JSON returned for the bid request. An example is shown here:

{"seatbid":[{"seat":"seat1","bid":[{"impid":"35c22289-06e2-48e9-a0cd-94aeb79fab43-1","id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","price":1.0,"adid":"ben:payday","nurl":"http://localhost:8080/rtb/win/smaato/${AUCTION_PRICE}/42.378/-71.227/ben:payday/23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43","cid":"ben:payday","crid":"23-1-skiddoo","iurl":"http://localhost:8080/images/320x50.jpg?adid=ben:payday&bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43","adomain": ["originator.com"],"adm":"<ad xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"smaato_ad_v0.9.xsd\" modelVersion=\"0.9\"><imageAd><clickUrl>http://localhost:8080/redirect/exchange=smaato/ben:payday/creative_id=23-1-skiddoo/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43?url=http://localhost:8080/contact.html?99201&amp;adid=ben:payday&amp;crid=23-1-skiddoo</clickUrl><imgUrl>http://localhost:8080/images/320x50.jpg?adid=ben:payday&amp;bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43</imgUrl><width>320</width><height>50</height><toolTip></toolTip><additionalText></additionalText><beacons><beacon>http://localhost:8080/pixel/exchange=smaato/ad_id=ben:payday/creative_id=23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43</beacon></beacons></imageAd></ad>"}]}],"id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","bidid":"35c22289-06e2-48e9-a0cd-94aeb79fab43"}


RUNNING RTB4FREE AS A SERVICE (SYSTEMD)
=====================================
You can run RTB4FREE as an systemd service. There is an systemd script located at ./XRTB/rtb4free.service.

$sudp cp rtb4free.service /etc/systemd/system
$sudo systemctl daemon-reload

Start the Bidder
-------------------------------
$sudo systemctl start rtb4free

Stop the Bidder
--------------------------------
sudo systemctl stop rtb4free

The Log File
--------------------------------
Log file is located at /var/log/rtb4free.log


THE AEROSPILE-DISTRIBUTED DATABASE
=====================================
The AEROSPIKE enabled RTB4FFREE uses a shared JAVA ConcurrentHashMap backed in Aerospike, that allows all bidders to have
access to the advertising campaigns. Likely this would be replaced by your own DBMS, but for RTB4FREE
we simply use the shared object. Before RTB4FREE can be used, the database has to be loaded into Aerospike first.
This is done with:

	$cd XRTB
	$tools/load-database

This will load XRTB/database.json file into the Aerospike system running on localhost. To change the parameters of DbTools, 
look at the JAVADOC in target/site/tools/DbTools.html

The database in Aerospke is where all the User/Campaign records are stored, but, then to run these campaigns, 
you must tell the bidders to pull these campaigns into their local memory (using a ZeroMQ command). The database  is the static location for the Users and their Campaigns. To 'run' a campaign' you load it into the bidders local memory. All the bidders have a  ConcurrentHashMap that comprises this database, and it is shared across all the bidders.

In a commercial setting, you would likely replace this Database part of RTB4FREE with your own database management system.

For information on how to configure campaigns in the RTB4FREE bidder, look on the RTB4FREE web site
here"

CONFIGURING THE BIDDER.
=====================================
In order to run the bidder, you will need to load a campaign into the bidders memory and setup some operational parameters.
 These parameters are stored in a JSON file the bidder uses when it starts. There is a sample initialization file called 
"./Campaigns/payday.json' you can use to get started. The file describes the operational parameters of the bidder. 
Look in http://rtb4free.com/details_new.html for an in depth analysis of the configuration file. Also, once you get the
bidder running, you can use the System Consolse to change the parameters using the web interface, described here:
http://rtb4free.com/admin-mgmt.html

However, here is an example file, and a brief over

{
  "forensiq" : {
    "threshhold" : 0,
    "ck" : "none",
    "endpoint" : "",
    "bidOnError" : "false"
  },
  "app" : {
    "stopped" : false,
    "ttl" : 300,
    "deadmanswitch" : null,
    "multibid" : false,
    "pixel-tracking-url" : "http://localhost:8080/pixel",
    "winurl" : "http://localhost:8080/rtb/win",
    "redirect-url" : "http://localhost:8080/redirect",
    "adminPort" : 0,
    "adminSSL" : false,
    "password" : "startrekisbetterthanstarwars",
    "verbosity" : {
      "level" : -3,
      "nobid-reason" : false
    },
    "geotags" : {
      "states" : "",
      "zipcodes" : ""
    },
    "aerospike" : {
      "host" : "localhost",
      "maxconns" : 300,
      "port" : 3000
    },
    "zeromq" : {
      "bidchannel" : "tcp://*:5571&bids",
      "responses" : "tcp://*:5575&responses",
      "nobid" : "",
      "winchannel" : "tcp://*:5572&wins",
      "requests" : "file://logs/request&time=30",
      "logger" : "tcp://*:5574&logs",
      "clicks" : "tcp://*:5573&clicks",
      "subscribers" : {
        "hosts" : [ "localhost", "192.168.1.167" ],
        "commands" : "5580"
      },
      "status" : "file://logs/status&time=30"
    },
    "template" : {
      "default" : "{creative_forward_url}",
      "exchange" : {
        "adx" : "<a href='locahost:8080/rtb/win/{pub_id}/%%WINNING_PRICE%%/{lat}/{lon}/{ad_id}/{creative_id}/{bid_id}'}'></a><a href='%%CLICK_URL_UNESC%%{redirect_url}></a>{creative_forward_url}",
        "mopub" : "<a href='mopub template here' </a>",
        "mobclix" : "<a href='mobclix template here' </a>",
        "nexage" : "<a href='{redirect_url}/exchange={pub}/ad_id={ad_id}/creative_id={creative_id}/price=${AUCTION_PRICE}/lat={lat}/lon={lon}/bid_id={bid_id}?url={creative_forward_url}'><img src='{creative_image_url}' height='{creative_ad_height}' width='{creative_ad_width}'></a><img src='{pixel_url}/exchange={pub}/ad_id={ad_id}/creative_id={creative_id}/{bid_id}/price=${AUCTION_PRICE}/lat={lat}/lon={lon}/bid_id={bid_id}' height='1' width='1'>",
        "smartyads" : "{creative_forward_url}",
        "atomx" : "{creative_forward_url}",
        "adventurefeeds" : "{creative_forward_url}",
        "gotham" : "{creative_forward_url}",
        "epomx" : "{creative_forward_url}",
        "citenko" : "{creative_forward_url}",
        "kadam" : "{creative_forward_url}",
        "taggify" : "{creative_forward_url}",
        "cappture" : "cappture/{creative_forward_url}",
        "republer" : "{creative_forward_url}",
        "admedia" : "{creative_forward_url}",
        "ssphwy" : "{creative_forward_url}",
        "privatex" : "<a href='{redirect_url}/{pub}/{ad_id}/{creative_id}/${AUCTION_PRICE}/{lat}/{lon}?url={creative_forward_url}'><img src='{pixel_url}/{pub}/{ad_id}/{bid_id}/{creative_id}/${AUCTION_PRICE}/{lat}/{lon}' height='1' width='1'><img src='{creative_image_url}' height='{creative_ad_height}' width='{creative_ad_width}'></a>",
        "smaato" : "richMediaBeacon='%%smaato_ct_url%%'; script='{creative_forward_url}'; clickurl='{redirect_url}/exchange={pub}/{ad_id}/creative_id={creative_id}/price=${AUCTION_PRICE}/lat={lat}/lon={lon}/bid_id={bid_id}?url={creative_forward_url}'; imageurl='{creative_image_url}'; pixelurl='{pixel_url}/exchange={pub}/ad_id={ad_id}/creative_id={creative_id}/{bid_id}/price=${AUCTION_PRICE}/lat={lat}/lon={lon}/bid_id={bid_id}';",
        "pubmatic" : "{creative_forward_url}"
      }
    },
    "campaigns" : [ {
      "name" : "ben",
      "id" : "ben:payday"
    } ]
  },
  "ssl" : {
    "setKeyStorePath" : "data/keystore.jks",
    "setKeyStorePassword" : "password",
    "setKeyManagerPassword" : "password"
  },
  "seats" : [ {
    "name" : "adventurefeeds",
    "id" : "adventurefeedid",
    "bid" : "/rtb/bids/adventurefeeds=com.xrtb.exchanges.Adventurefeeds"
  },
  {
    "name" : "adprudence",
    "id" : "adprudenceid",
    "bid" : "/rtb/bids/adprudence=com.xrtb.exchanges.Adprudence"
  }, {
    "name" : "citenko",
    "id" : "citenkoif",
    "bid" : "/rtb/bids/citenko=com.xrtb.exchanges.Inspector"
  }, {
    "name" : "kadam",
    "id" : "kadamid",
    "bid" : "/rtb/bids/kadam=com.xrtb.exchanges.Kadam"
  }, {
    "name" : "gotham",
    "id" : "gothamid",
    "bid" : "/rtb/bids/gotham=com.xrtb.exchanges.Gotham"
  }, {
    "name" : "atomx",
    "id" : "atomxseatid",
    "bid" : "/rtb/bids/atomx=com.xrtb.exchanges.Atomx"
  }, {
    "name" : "smartyads",
    "id" : "smartypants",
    "bid" : "/rtb/bids/smartyads=com.xrtb.exchanges.Smartyads"
  }, {
    "name" : "nexage",
    "id" : "99999999",
    "bid" : "/rtb/bids/nexage=com.xrtb.exchanges.Nexage"
  }, {
    "name" : "privatex",
    "id" : "5555555",
    "bid" : "/rtb/bids/privatex=com.xrtb.exchanges.Privatex"
  }, {
    "name" : "fyber",
    "id" : "seat1",
    "bid" : "/rtb/bids/fyber=com.xrtb.exchanges.Fyber"
  }, {
    "name" : "smaato",
    "id" : "seat1",
    "bid" : "/rtb/bids/smaato=com.xrtb.exchanges.Smaato"
  }, {
    "name" : "epomx",
    "id" : "seat1",
    "bid" : "/rtb/bids/epomx=com.xrtb.exchanges.Epomx"
  }, {
    "name" : "cappture",
    "id" : "capptureseatid",
    "bid" : "/rtb/bids/cappture=com.xrtb.exchanges.Cappture"
  }, {
    "name" : "taggify",
    "id" : "taggifyid",
    "bid" : "/rtb/bids/taggify=com.xrtb.exchanges.Taggify"
  }, {
    "name" : "republer",
    "id" : "republerid",
    "bid" : "/rtb/bids/republer=com.xrtb.exchanges.Republer"
  }, {
    "name" : "admedia",
    "id" : "admediaid",
    "bid" : "/rtb/bids/admedia=com.xrtb.exchanges.AdMedia"
  }, {
    "name" : "ssphwy",
    "id" : "ssphwyid",
    "bid" : "/rtb/bids/ssphwy=com.xrtb.exchanges.SSPHwy"
  }, {
    "name" : "pubmatic",
    "id" : "pubmaticid",
    "bid" : "/rtb/bids/pubmatic=com.xrtb.exchanges.Pubmatic"
  } ],
  "lists" : [ ]
}

RTB4FREE writes its logs to ZeroMQ, default channel "tcp://*:5574", topic is 'logs'shown in the app.zeromq object above.The "seats" object is a list of seat-ids used for each of the exchanges you are bidding on. The seat-id is assigned by the exchange - it's how they know whom is bidding. The name attribute defines the name of the exchange, as it will appear in all the logs. The id is the actual id name the bidder sends to the exchange as the seat id - how the exchange knows who you are. The bid attribute tells the bidder where the JAVA class is for that exchange. In the above example, 3 exchanges are described.

The "app" object sets up the rest of the configuration for the RTB4FREE server

The "geotags" object defines the location of two files used by RTB4FREE to determine state, county, and zipcode information from GPS coordinates found in the bid request. The geotags object is not required.

The app.aerospike object defines the Aerospike host the bidders will to use and where to write bids, requests, logs and wins. ONLY the wins channel must be defined - and it must be defined or you will nor receive any win notifications! Otherwise, if you want to see the requests, define the channel, likewise for bids and clicks.

The app.password defines the password used by the root login on the campaign management and system administration page.

The app.ttl defines the time to live value for bid keys in Aerospike, in seconds. This means that after 5 minutes the key is deleted - and if the WIN notification comes in you will not process it - as the key is gone. You cannot let the keys pile up forever, Aerospile will run out of memory. This is a compromise between accuracy and performance.

The app.pixel-tracking-url field defines the URL that will be called when the ad is served up.

The app.winurl defines where the exchange is to send win notifications. It is customary to split win and bid processing across 2 domains, that share the same Aerospike cache. When a bid is made, a copy is stored in Aerospike, set to expire after some period of time (app.tt;). When the win notification comes in the bid needs to be retrieved to complete the transaction with the exchange. In systems with multiple bidders, there is no way to know which XRTB will receive the win thus you cannot store the bid information in local memory.

The app.redirect-url field defines the URL to use when the user clicks your advertisement.

The app.verbosity.nobid-reason field is for debugging and is used to tell you why the bidder did not bid. This is useful if things aren't working like you think it should. It creates a lot of output and it doubles the amount of time it takes to process a bid request. Operational useers should set this set to false. If set to true, the bidder log why the bidder chose to nobid on each creative, for each campaign.

The app.multibid flag denotes whether or not to support multiple bids for requests, the default is false.

The "campaigns" object is an array of campaign names (by adId) that will be initially loaded from Aerospike backed database and into the bidder's local memory. In the Campaigns/payday.json file, for demo purposes there is one campaign pre-loaded for you called "ben:payday". Note, this field accepts JAVA regular expressions. In the example the campaign that matches 'ben:payday' is loaded. To load all campaigns use '(.*). To load only campaigns prefixed with 'ben', then use 'ben(.*)'.

If you plan to bid (and win), you must have at least 1 campaign loaded into the bidder. If you have multiple campaigns, and a bid request matches 2 or more campaigns, the campaign to bid is chosen at random.

THEORY OF OPERATION
=====================================
Aerospike is used as the shared context  between all bidders. All shared data is kept in Aerospike, and all  bidders connect to this Aerospike instance to share data. Specifically, the response to a bid request, a 'bid', is stored
in Aerospike after it is made, because on the win notification, a completely separate bidder may process the win, and the
original bid must be retrieved as quickly as possible to complete the transaction. A database query is far to slow to accomplish
this. This is the main use for Aerospike

ZeroMQ is used as the publish/subscribe system. Commands are sent to running bidders over ZeroMQ publish channel.
Likewise responses to commands are sent back on another ZeroMq channel, 'responses'. Clickthrough, wins, and pixel-file notification is sent on yet channels, as set forth in the app.zeromq object.

Shared Database
-------------------------------
A database of Users and their campaigns is kept in a  ConcurrentHashMap, that is stored in Aerospike as a Map. This
allows the bidders to maintain a shared database. 

Configuration
--------------------------------
A configuration file is used to set up the operating parameters of the bidder (such as Aerospike host and ZeroMQ 
addresses), located at ./XRTB/SampleCampaigns/payday.json;  and is used to load any initial campaigns from the Database Aerospike. Upon loading the configuration file into the Configuration class, the campaigns are created, using a set of 
Node objects that describe the JSON name to look for in the RTB bid, and the acceptable values for that constraint.

For details look here: http://rtb4free.com/admin-mgmt.html#configuration-section

Receive Bid
-----------
When the RTBBidder starts, it creates a an HTTP handler based on Jetty that handles all the HTTP requests coming into
the bidder. The handler will process mundane gets/posts to retrieve resources like images and javascript files placed in
the ./web directory. In addition, the bidder will produce a BidRequest object from the JSON payload of the HTTP post. The URI will determine the kind of exchange, e.g. Nexage.

Note, each bid request is on a thread started by JETTY, For each one of these threads, N number of threads will be created for N campaigns. The number of total threads is limited by a configuration parameter "maxConnections". When max connections is reached, the bid request will result in a no-bid.

Campaign Select
---------------
Once the Handler determines the bid request and instantiates it, the BidRequest object will then determine which, if any of the campaigns are to be selected. If no campaign was selected, the Handler will return an HTTP 204 code to indicate no reply. Each of the campaigns is loaded into a future task to hold it, and then the tasks are started. When the tasks join, 0 or more of the campaigns may match the bid request. In this case, the campaign is chosen among the set at random

Note, the RTBServer will place an X-REASON header in the HTTP that explains why the bidder did not bid on the
request. Also note, the RTBServer always places an X-TIME header in the HTPP that describes the time the bidder spent processing a bid request (in milliseconds).

Create Bid Response
-------------------
The BidRequest then produces a BidResponse that is usable for this bid request. The bid is first recorded in Aerospike as a
map, then the JSON form is serialized and then returned to the Handler. The bid will then be written to the HTTP response. Note, it is possible to also record the bid requests and the bids in respective ZeroMQ publish channels. This way these messages can be analyzed for further review.

Win the Auction
---------------
If the exchange accepts the bid, a win notification is sent to the bidder. The handler will take that notification, which is an encoded URI of information such as auction price, lat, lon, campaign attributes etc. and writes this information to the ZeroMQ channel so that the win can be recorded by some downstream service. The ADM field of the original bid is returned to the exchange with the banner ad, the referer url
and the pixel url.

Ad Served
----------------
When the user's screen receives the ad, the pixel URL is fired, and URI encoded GET is read by the Handler to
associate the loading of the page in the web browser with the winning bid and this information is sent to a ZeroMQ 'clicks' channel, so that it can be reconciled by some downstream service with the originating bid.

User Clicks the Ad
------------------
When the user clicks on the ad, the referrer URL is fired and this is also handled by the handler. The handler then uses the URI encoding to transmit the information to a ZeroMQ channel, usually called 'clicks', for further processing and accounting downstream.

USING THE SIMULATOR WEB PAGE
============================
After starting the RTB server you can send it a test bid by pointing your browser to  http://localhost:8080/xrtb/simulator/exchange.

(The test page presumes you are using the Campaigns/payday.json campaign definition file. The test page will let you change the advertiser domain and the geo.country field in a canned bid. You can also add other constraints to customize the bid request.

If you like, you can use the JavaScript window to set the values of the bid request. For example, you can override the id  by using bid.id = '11111'; in the window. Push the Free Form "Show" button and the bid is shown as a JavaScript object. Make the changes you want in that object and then hit the 'Test'  button.

Press the Test button - The X-TIME of the bid/response will be shown. The NURL will be returned and the image of the
advertisement is displayed. Below this you can see the contents of the bid/response. If the server bid, You can send a win notification by pressing the "Send Win" button. This will also
cause the pixel handler to transmit a 'pixel loaded'  notification that the image was returned to the browser.

Clicking the ad sends you to a dummy ad page, and also causes the handler to transmit a 'click notification' notifying
you that the user actually clicked on the ad.

To see a no bid, input GER for bid.user.geo.country. The X-REASON will then be displayed on the page.


Accessing the Site
======================

There is a test page located at http://localhost:8080

It provides a system console, a campaign manager, and bid test page.

For information on the ZerpMQ based commands for the RTB4FREE bidder look here: http://rtb4free.com/details_new.html#ZEROMQ




