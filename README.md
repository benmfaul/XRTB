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

You will need Maven and Aerospike installed to build the system. The libraries required are automatically retrieved by Maven. To see the dependencies, Look in the pom.xml file

If you use Eclipse make sure you use this as a maven prokect

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


QUICKLY BUILD, RUN AND TEST THE SYSTEM
=============================================
Here's the absolutely fastest way to get RTB4FREE running on your local machine. Note, Aerospike server
must also be running on the local machine as well. You can change the configuration up later 
using this as a guide: http://rtb4free.com/details.html#CONFIGURATION

The following brings the RTB4FREE system up and running on your local machine in 5 minutes, presuming you have Git Aerospike, Maven, and Java 1.8 installed on your local machine and Aerospike is already running.

In one window:

	$git clone https://github.com/benmfaul/XRTB.git
	cd XRTB
	$mvn assembly:assembly -DdescriptorId=jar-with-dependencies  -Dmaven.test.skip=true
	$tools/load-database
	$tools/rtb4free

				
You should see the RTB4FREE bidder come up, and print some logging information. The configuration data file
is in Campaigns/payday.json. It is here where you will change the location of your Aerospike system if you are
not running on localhost. Look at the RTB4FREE web site: http://rtb4free.com/details.html#CONFIGURATION
for details on how to configure the system.

------------------------------------------------------------
In another window

	$cd XRTB
	$sh shell/curltest.sh
                
You should see the JSON returned for the bid request. An example is shown here:
{"seatbid":[{"seat":"seat1","bid":[{"impid":"35c22289-06e2-48e9-a0cd-94aeb79fab43-1","id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","price":1.0,"adid":"ben:payday","nurl":"http://localhost:8080/rtb/win/smaato/${AUCTION_PRICE}/42.378/-71.227/ben:payday/23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43","cid":"ben:payday","crid":"23-1-skiddoo","iurl":"http://localhost:8080/images/320x50.jpg?adid=ben:payday&bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43","adomain": ["originator.com"],"adm":"<ad xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"smaato_ad_v0.9.xsd\" modelVersion=\"0.9\"><imageAd><clickUrl>http://localhost:8080/redirect/exchange=smaato/ben:payday/creative_id=23-1-skiddoo/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43?url=http://localhost:8080/contact.html?99201&amp;adid=ben:payday&amp;crid=23-1-skiddoo</clickUrl><imgUrl>http://localhost:8080/images/320x50.jpg?adid=ben:payday&amp;bidid=35c22289-06e2-48e9-a0cd-94aeb79fab43</imgUrl><width>320</width><height>50</height><toolTip></toolTip><additionalText></additionalText><beacons><beacon>http://localhost:8080/pixel/exchange=smaato/ad_id=ben:payday/creative_id=23-1-skiddoo/35c22289-06e2-48e9-a0cd-94aeb79fab43/price=${AUCTION_PRICE}/lat=42.378/lon=-71.227/bid_id=35c22289-06e2-48e9-a0cd-94aeb79fab43</beacon></beacons></imageAd></ad>"}]}],"id":"35c22289-06e2-48e9-a0cd-94aeb79fab43","bidid":"35c22289-06e2-48e9-a0cd-94aeb79fab43"}


RUNNING AS A SERVICE (SYSTEMD)
=====================================
You can run RTB4FREE as an systtemd service. There is an systemd script located at ./XRTB/rtb4free.service.

copy the rtb4free.service file to /etc/systemd/system
sudo systemctl daemon-reload

Start the Bidder
-------------------------------

$sudo systemctl start rtb4free

Stop the Bidder
--------------------------------

sudo systemctl stop rtb4free

The Log File
--------------------------------

Log file is located at /var/log/rtb4free.log

INITIALIZING THE DISTRIBUTED DATABASE
=====================================
The RTB4FFREE uses a shared JAVA ConcurrentHashMap backed in Aerospike, that allows all bidders to have
access to the advertising campaigns. Likely this would be replaced by your own DBMS, but for RTB4FREE
we simply uSE THE SHARED OBJECT. Before RTB4FREE can be used, the database has to be loaded into Aerospike first.
This is done with:

	$cd XRTB
	$tools/load-database

This will load XRTB/database.json file into the Aerospike
system running on localhost. To change the parameters of DbTools, look at the 
JAVADOC in target/site/tools/DbTools.html

The database in Aerospke is where all the User/Campaign records are stored, but, then to run these campaigns, 
you must tell the bidders to pull these campaigns into their local memory (using a ZeroMQ command). The database  is the static location for the Users and their Campaigns. To 'run' a campaign' you load it into the bidders local memory. All the bidders have a  ConcurrentHashMap that comprises this database, and it is shared across all the bidders.

In a commercial setting, you would likely replace this Database part of RTB4FREE with your own database management system.

For information on how to configure campaigns in the RTB4FREE bidder, look on the RTB4FREE web site
here"

CONFIGURING THE BIDDER.
=====================================
After loading the database in aerospike, you need to configure the bidder, start it, and load at least one campaign into the system

In order to run the bidder, you will need to load a campaign into the bidders memory and setup some operational parameters. These parameters are stored in a JSON file the bidder uses when it starts. There is a sample initialization file called 
"./Campaigns/payday.json' you can use to get started. The file describes the operational parameters of the bidder. 
There is a  README.md file in the ./Campaigns directory that explains the format of the campaign, and how to build your constraints.

{
    "seats": [
        {
        	"name":"nexage", "id":"99999999", "bid":"/rtb/bids/nexage=com.xrtb.exchanges.Nexage"
        },
        {
        	"name":"privatex", "id":"5555555", "bid":"/rtb/bids/privatex=com.xrtb.exchanges.Privatex"
        },
        {
        	"name":"fyber", "id":"seat1", "bid":"/rtb/bids/fyber=com.xrtb.exchanges.Fyber"
        }
    ],
    "app": {
    	"password": "iamthepassword",
    	"connections":100,
        "ttl": 300,
        "pixel-tracking-url": 	"http://localhost:8080/pixel",
        "winurl": 				"http://localhost:8080/rtb/win",
        "redirect-url": 		"http://localhost:8080/redirect",
        "verbosity": {
            "level": -5,
            "nobid-reason": false
        },
        "geotags": {
        	"states": "data/zip_codes_states.csv",
			"zipcodes": "data/unique_geo_zipcodes.txt"
		},  
		"zeromq": {
			"bidchannel": "tcp://*:5571&bids",
			"winchannel": "tcp://*:5572&wins",
			"clicks":     "tcp://*:5573&clicks",
			"logger":     "tcp://*:5574&logs",
			"responses":  "tcp://*:5575&responses",
			"pixels":     "tcp://*:5576&pixels",
			"NOforensiq":   "file://logs/forensiq",
			"NOrequests":   "file://logs/request",
			"subscribers": {
    			"hosts": ["localhost","192.168.1.167"],
    			"commands": "5580"
    		}			
		},
       "aerospike": {
            "host": "localhost",
            "port": 6379
        },
        
        "campaigns": [
			"ben:payday",
			"ben:fyber"
        ]
    }
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

The app.verbosity object defines the logging level for the XRTB program. Setting app.verbosity.level to 1 means only the most critical messages are logged to ZeroMQ log channel. Set the level ever higher to obtain more log information. The logs are published to the ZeroMQ publish topic defined by zeromq.logger field. However, if you want the logger to also print on STDOUT too, set the log level to a negative value. For example, "level": -5 means to log all kind of stuff, and also print it in STDOUT.

The app.verbosity.nobid-reason field is for debugging and is used to tell you why the bidder did not bid. This is useful if things aren't working like you think it should. It creates a lot of output and it doubles the amount of time it takes to process a bid request. Operational useers should set this set to false. If set to true, the bidder log why the bidder chose to nobid on each creative, for each campaign.

The "campaigns" object is an array of campaign names (by adId) that will be initially loaded from Aerospike backed database and into the bidder's local memory. In the Campaigns/payday.json file, for demo purposes there is one campaign pre-loaded for you called "ben:payday". Note, this field accepts JAVA regular expressions. In the example the campaign that matches 'ben:payday' is loaded. To load all campaigns use '(.*). To load only campaigns prefixed with 'ben', then use 'ben(.*)'.

If you plan to bid (and win), you must have at least 1 campaign loaded into the bidder. If you have multiple campaigns, and a bid request matches 2 or more campaigns, the campaign to bid is chosen at random.

More extensive documentation to show you how to configure the database.json file creating commands, look
here: http://rtb4free.com/details.html#CONFIGURATION

Running the Bidder
=====================================
Now that the intitial configuration files are setup, now you start the bidder. From ant it's real simple, there is a target that uses Campaigns/payday.json as the configuration file:

    $tools/rtb4free

This will run RTB4FREE in the foreground. To run in the background use:

	$tools/rtb4free > /dev/null &



THEORY OF OPERATION
=====================================
Aerospike is used as the shared context  between all bidders. All shared data is kept in Aerospike, and all  bidders connect to this Aerospike instance to share data. Specifically, the response to a bid request, a 'bid', is stored
in Aerospike after it is made, because on the win notification, a completely separate bidder may process the win, and the
original bid must be retrieved as quickly as possible to complete the transaction. A database query is far to slow fo
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

For details look here: http://rtb4free.com/details.html#CONFIGURATION

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


For information on the ZerpMQ based commands for the RTB4FREE bidder look here: http://rtb4free.com/details.html#ZEROMQ




