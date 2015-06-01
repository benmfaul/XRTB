XRTB
=====================

NOTE: THIS IS A WORK IN PROGRESS

===========================

A Real Time Broker (RTB) 2.2 bidding engine written in Java 1.8

This RTB project contains 3 major components: 1) A Real Time Bidding engine; 2) A Simulator for sending
test bids to the bidder; 3) A campaign manager for creating advertising campaigns.

This project is for those fairly familiar with RTB. With a basic understanding of RTB, this project will get you
up and running with a commercial grade bidder in a short period of time.

Note, a major component of a commercial RTB system is a database for doing all those production things like campaign management, bid tracking, win handling and click through accounting. This project doesn't include any of that, However, the XRTB uses a publish/subscribe system (in REDIS) that will allow you to connect these functions of the bidder into your own custom database.

A production bidding enterprise would most likely require multiple bidding engines running behind a firewall. This project does not provide NGINX or AWS Load Balancer (or similar) infrastructure for this, you will need to tailor the 
administration of the XRTB to deal with these production issues. The XRTB comes out of the box ready to run in
a multi-server bidding farm, you just need to lash it up and administer it.


BUILDING THE SYSTEM
=======================

You will need ANT, JAVA 1.8, JACKSON, JETTY, GSON and REDIS installed to build the system. The libraries required are already placed in the ./libs directory

If you use Eclipse or some other IDE, make your project and include the ./libs directory.

Note, there is a .gitignore file included.

Now use ant to build the system:

$ant

----> Will create ./libs/xrtb.jar    -- Which is the jar file of the project.

$ant javadoc

----> Will create javadoc documents in the ./javadoc directory.

$ant junitreport

----> Will run the JUNIT test cases and output the reports in the ./reports directory


INITIALIZING THE DISTRIBUTED DATABASE
=====================================
The RTB4FFREE uses a shared JAVA ConcurrentHashMap based in Redisson, that allows all bidders to have
access to the advertising campaigns. Likely this would be replaced by yiur own DBMSm but for RTB4FREE
we simply uSE THE SHARED OBKECT. Before RTB4FREE can be used, the database has to be loaded into REDIS first.
This is done with:

$ant load-database

This will load database.json from the current directory into the Redis
system running on a host. To change the parameters of DbTools, look at the JAVADOC in javadoc/tools/DbTools.html

The database in Redis is where all the User/Campaign records are stored, but, then to run these campaigns, 
you must tell thebidders to pull these campaigns into their local memory (usinga Redis command). The database in Redis is the static location for the Users and their Campaigns. To 'run' a campaign' you load it into the bidders local memory. All the bidders have a  ConcurrentHashMap that comprises this database, and it is shared across all the bidders.

In a commercial setting, you would likely replace this Database part of RTB4FREE with your own database management system

CONFIGURING THE BIDDER.
=====================================
After loading the database in Redis, you need to configure the bidder, start it, and load at least one campaign into 
the system

In order to run the bidder, you will need to load a campaign into the bidders memory and setup some operational parameters. These parameters are stored in a JSON file the bidder uses when it starts. There is a sample initialization file called "./Campaigns/payday.json' you can use to get started. The file describes the operational parameters of the bidder. There is a  README.md file in the ./Campaigns directory that explains the format of the campaign, and how to build your constraints.

{
    "instance":"this-systems-instance-name-here",
    "port": 8080,
    "seats": [
        {"name":"nexage", "id":"99999999", "bid":"/rtb/bids/nexage=com.xrtb.exchanges.Nexage"}
    ],
    "app": {
        "ttl": 300,
        "pixel-tracking-url": "http://localhost:8080/pixel",
        "winurl": "http://localhost:8080/rtb/win",
        "redirect-url": "http://localhost:8080/redirect",
        "geotags": {
            "states": "data/zip_codes_states.csv",
            "zipcodes": "data/unique_geo_zipcodes.txt"
        }, 
        "verbosity": {
            "level": 2,
            "nobid-reason": true
        },
        "redis": {
            "host": "localhost",
            "bidchannel": "bids",
            "winchannel": "wins",
            "requests": "requests",
            "logger":   "log",
            "clicks": "clicks",
            "port": 6379
        },
        "campaigns": [
            "ben:payday"
        ]
    }
}

The top level field "instance" defines the name of thebidder, and will be used in all logging methods. Note, RTB4FREE
writes its logs to REDIS, default channel "logs", which you can change with in the "app" object.

The "port" field defines the TCP port the XRTB server will utilize to handle bid requests.

The "geotags" object defines the location of two files used by RTB4FREE to determine state, county, and zipcode information from GPS coordinates found in the bid request.

The "seats" object is a list of seat-ids used for each of the exchanges you are bidding on. The seat-id is assigned by
the exchange - it's how they know whom is bidding//

The "app" object defines all the operational parameters used by XRTB.

The app.redis object defines the REDIS host to use and where to write bids, requests, logs and wins. ONLY the wins channel must be defined - and it must be defined! The others will write
to the bids, requests and logs if the channel has been defined.

The app.ttl defines the throttle percentage. Set to 100 and all bid requests will be considered. Set to 50 and 50% of the bid requests will be rejected out of hand.

The app.pixel-tracking-url field defines the URL that will be called when the ad is served up.

The app.winurl defines where the exchange is to send win notifications. It is customary to split win and bid processing
across 2 domains, that share the same REDIS cache. When a bid is made, a copy is stored in REDIS, set to expire after some period of time. When the win notification comes in the bid needs to be retrieved to complete the transaction with the exchange. In systems with multiple bidders, there is no way to know which XRTB will receive the win thus you cannot store the  bid information in local memory.

The app.redirect-url field defines the URL when the user clicks your advertisement.

The app.verbosity object defines the logging level for the XRTB program. Setting app.verbosity.level to 0 means only the
most critical messages are logged to REDIS log channel. Set the level ever higher to obtain more log information.

The app.verbosity.nobid-reason field is for debugging. Operational use set to false. If set to true, XRTB will print on STDOUT why the bidder chose to nobid. This is the only logmessage sent to STDOUT.

The "campaigns" object is an array of campaign names (by adId) that will be initially loaded from Redis backed database
and into the bidder's local memory. In theCampaigns/payday.json file, there is one campaign pre-loaded
for you called "ben:payday" If you plan to bid, you must have at least 1 campaign loaded into the bidder. If you have
multiple campaigns, and a bid request matches 2 or more campaigns, the campaign to bid is chosen at random.

A campaign looks like in JSON form looks likeL
         
  {
    "name": "ben",
    "origin": 1431197706849,
    "lastAccess": 1431197706849,
    "campaigns": [
      {
        "adId": "ben:payday",
        "price": 5.0,
        "adomain": "originator.com",
        "template": {
          "default": "999",
          "exchange": {
            "mopub": "\u003ca href\u003d\u0027mopub template here\u0027 \u003c/a\u003e",
            "mobclix": "\u003ca href\u003d\u0027mobclix template here\u0027 \u003c/a\u003e",
            "nexage": "\u003ca href\u003d\u0027{RTB_REDIRECT_URL}/{RTB_CAMPAIGN_ADID}/{pub}/{bid_id}?url\u003d{campaign_forward_url}\u0027\u003e\u003cimg src\u003d\u0027{RTB_PIXEL_URL}/{pub}/{ad_id}/{bid_id}/${AUCTION_PRICE}/{creative_id}\u0027 height\u003d\u00271\u0027 width\u003d\u00271\u0027\u003e\u003c/img\u003e\u003cimg src\u003d\u0027{campaign_image_url}\u0027 height\u003d\u0027{campaign_ad_height}\u0027 width\u003d\u0027{campaign_ad_width}\u0027\u003e\u003c/img\u003e\u003c/a\u003e"
          }
        },
        "attributes": [
          {
            "operator": 4,
            "value": [
              "chive.com",
              "junk.com"
            ],
            "lval": [
              "chive.com",
              "junk.com"
            ],
            "op": "NOT_MEMBER",
            "notPresentOk": true,
            "bidRequestValues": [
              "site",
              "domain"
            ]
          },
          {
            "operator": 3,
            "value": [
              "USA",
              "MEX"
            ],
            "lval": [
              "USA",
              "MEX"
            ],
            "op": "MEMBER",
            "notPresentOk": true,
            "bidRequestValues": [
              "user",
              "geo",
              "country"
            ]
          },
          {
            "operator": 3,
            "value": [
              "CA",
              "NY",
              "MA"
            ],
            "lval": [
              "CA",
              "NY",
              "MA"
            ],
            "op": "MEMBER",
            "notPresentOk": true,
            "bidRequestValues": [
              "rtb4free",
              "geocode",
              "state"
            ]
          }
        ],
        "creatives": [
          {
            "forwardurl": "http://localhost:8080/forward?{site_id}",
            "encodedFurl": "http%3A%2F%2Flocalhost%3A8080%2Fforward%3F%7Bsite_id%7D",
            "imageurl": "http://localhost:8080/images/320x50.jpg?adid\u003d{ad_id}\u0026#38;bidid\u003d{bid_id}",
            "encodedIurl": "http%3A%2F%2Flocalhost%3A8080%2Fimages%2F320x50.jpg%3Fadid%3D%7Bad_id%7D%26%2338%3Bbidid%3D%7Bbid_id%7D",
            "impid": "23skiddoo",
            "w": 320.0,
            "h": 50.0
          },
          {
            "forwardurl": "http://localhost:8080/forward?{site_id}",
            "encodedFurl": "http%3A%2F%2Flocalhost%3A8080%2Fforward%3F%7Bsite_id%7D",
            "imageurl": "http://localhost:8080/images/320x50.gif?adid\u003d{ad_id}\u0026#38;bidid\u003d{bid_id}",
            "encodedIurl": "http%3A%2F%2Flocalhost%3A8080%2Fimages%2F320x50.gif%3Fadid%3D%7Bad_id%7D%26%2338%3Bbidid%3D%7Bbid_id%7D",
            "impid": "66skiddoo",
            "w": 640.0,
            "h": 480.0
          }
        ],
        "date": [
          20130205,
          20200101
        ]
      }

Remember these campaigns are stored in the Redis database as an element of a User, and must be subsequently loaded into
the bidder in order for bidding to take place.

The "campaign.adm-template" field defines what the bid response ADM field will look like.

The ADM field is examined by the XRTB bidder to fill in fields you want sent to the exchange. This is done using macro
substitution fields. The XRTB fields you can substitute are:

        {campaign_forward_url}  Substitured from the campaig creatives forward url
        {bid_id}                Substituted from the bid's object id field.
        {ad_id}                 Substituted from the campaign id.
        {campaign_ad_price}     Substituted from the campaign's price.
        {campaign_ad_width}     Substituted from campaign creatives width
        {campaign_ad_height}    Substituted from campaign creatives height
        {creative_id}           Substituted from campaign's creative's id.
        {pub}                   Substituted from the bid request exchange.


Note, the RTB exchange will reflect the ADM back on the win notification, and you can ask for the RTB exchange to also
substitute fields as well. See the RTB 2.1 specification for supported macro names. These are the macros substituted by the Exchange, not the bidder. so these will return via the RTB win notification:

        {AUCTION_ID}        ID of the bid request; from “id” attribute.
        {AUCTION_BID_ID}    ID of the bid; from “bidid” attribute.
        {AUCTION_IMP_ID}    ID of the impression just won; from “impid” attribute.
        {AUCTION_SEAT_ID}   ID of the bidder’s seat for whom the bid was made.
        {AUCTION_AD_ID}     ID of the ad markup the bidder wishes to serve; from “adid” attribute.
        {AUCTION_PRICE}     Settlement price using the same currency and units as the bid.
        {AUCTION_CURRENCY}  The currency used in the bid (explicit or implied); for confirmation only.

The campaign-adm-template.default field sets forth the ADM field when bidding on an exchange that you have not further
defined an ADM field for. If each exchange ADM field will look the same, then just use this field.

The campaign-adm-template.exchanges is an array of objects that define specific ADM patterns for specific exchanges An
example would be "campaign-adm-template.exchanges[0] = {"nexage":"nexage template here"}

The campaign-creatives object is an array of creatives for use with the campaign. Multiple creatives allow you to support multiple sized ads.

The campaign-creatives.x field sets the width of the ad in pixels.

The campaign-creatives.y field sets the height of the ad in pixels.

The campaign-creatives.impid is a field you can use to assign a different accounting id for the creative.

The campaign-creatives.imageurl defines the location of the ad image itself. It must be encoded, and it too supports the
macro substitutions defined above.

The campaign-createive.forward-url field defines the campaign id , substituted with {ad_id}.

The campaign-adId is the advertisement ID.

The campaign-price is the price to use for the bid.

p>The campaign attributes object is an array of constraints that the bid request will tested with for equality, membership etc


Running the Bidder
=====================================
Now that the intitial configuration files are setup, now you start the bidder. From ant it's real simple, there is a target that uses Campaigns/payday.json as the configuration file:

    $ant xrtb

This will run RTB4FREE in the foreground. To run in the background use:

	$nohup ant xrtb &gt; /dev/null &



THEORY OF OPERATION
=====================================
Redis is used as the shared context  between all bidders. All shared data is kept in Redis, and all  bidders connect to this Redis instance to share data. Specifically, the response to a bid request, a 'bid', is stored
in Redis after it is made, because on the win notification, a completely separate bidder may process the win, and the
original bid must be retrieved as quickly as possible to complete the transaction. A database query is far to slow fo
this. This is the main use for Redis

Another use for Redis is its publish/subscribe system. Commands are sent to running bidders over a Redis channel.
Likewise responses to commands are sent back on another Redis channel. Clockthrough notification is sent on yet another
channel.

Redission Based Shared Database
-------------------------------
A database of Users and their campaigns is kept in a  ConcurrentHashMap, that is created from a Redisson object. This
allows the bidders to maintain a shared database. The HashMap is actually backed in Redis using the Redisson system.

Configuratuion
--------------------------------
A configuration file is used to set up the basic operating parameters of the bidder (such as Redis channels) and to load
any initial campaigns from the Database in Redis. Upon loading the configuration file into the Configuration class,
the campaigns are created, using a set of Node objects that describe the JSON name to look for in the RTB bid, and the
acceptable values for that constraint.

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
The BidRequest then produces a BidResponse that is usable for this bid request. The bid is first recorded in REDIS as a
map, then the JSON form is serialized and then returned to the Handler. The bid will then be written to the HTTP response. Note, it is possible to also record the bid requests and the bids in respective REDIS publish channels. This way these messages can be analyzed for further review.

Win the Auction
---------------
If the exchange accepts the bid, a win notification is sent to the bidder. The handler will take that notification, which is an encoded URI of information such as auction price, lat, lon, campaign attributes etc. and writes this information to the REDIS channel so that the win can be recorded by some downstream service. The ADM field of the original bid is returned to the exchange with the banner ad, the referer url
and the pixel url.

Mobile Ad Served
----------------
When the mobile user's screen receives the ad, the pixel URL is fired, and URI encoded GET is read by the Handler to
associate the loading of the page in the web browser with the winning bid and this information is sent to a REDIS 'clicks' channel, so that it can be reconciled by some downstream service with the originating bid.

User Clicks the Ad
------------------
When the mobile user clicks on the ad, the referrer URL is fired and this is also handled by the handler. The handler then uses the URI encoding to transmit the information to a REDIS channel, usually called 'clicks', for further processing and accounting downstream.

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


REDIS COMMANDS
==============
The RTB Bidding engine uses REDIS pub/sub for all communications. The basic commands are:

- Echo, return status.
- Load Campaign into Redis Database (not into the bidders.
- Delete Campaign from Redis Database.
- Start A Campaign, (load from Redis into Bidder memory)
- Stop a Campaign, (unload campaign fro Bidders memory.
- Stop a Bidder,  Causes a bidder to pause, it will
- Start Bidde, Restarts a stopped bidder.
- Exit 

The commands are Java objects, all  derived from the com.xrtb.commands.BasicCommand class The objects are serialized and then sent to the Redis 'commands' topic. The responses to the commands are transmitted back via the 'responses' topic.

In order to make communications with the bidding system easier, a program called tools.Commander is provided so
that you can easily send and receive commands with the bidding engines. In the following sub-sections each of these commands is discussed and the appropriate Commander use is shown. 

To start the Commander, use:

    $ant Commander

(Note, this presumes you are using Redis on localhost. If you are using a
different configuration, see the JAVADOC for the Commander.) The Commander will
reply with a prompt, then you specify the appropriate response on STDIN

    $ant Commander
    RTB4FREE Commander
    Command: (1=Echo, 2=Load, 3=Delete, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder)
    ?? 
 

You can also use REDIS to send the bidder(s) commands to add campaigns, delete campaigns, start the bidder, stop the bidder and retrieve statistics. Each command is a JSON string. Look in the package com.xrtb.commands for examples on the actual  command formats. However, here are the basics. The bidders listen on the 'commands' topic, and return values on
'responses' topic. Each of the commands and their responses may have additional fields. 
                
Echo
----    
The echo command will prompt the bidders to reply  with their status. The status will include a list of the
campaigns it is running as well as summary statistics about the use of the system. Example use:

    $ant Commander
    RTB4FREE Commander
    Command: (1=Echo, 2=Load, 3=Delete, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder
    ?? 1
    Echo to?
    

The command has 1 argument, 'to'. Put the instance name of which bidder you want to respond, otherwise, simply pressing cr will cause all bidders to respond. The bidding engines transmit copies of bid requests, bids, win notifications and clicks through separate channels, all defined in the
configuration file under the app.redis object.  

Here's a sample output:             
                
{
  "instance": "the-bidder-instance"                
  "campaigns": [
    {
      "adId": "ben:payday",
      "price": 5.0,
      "adomain": "originator.com",
      "template": {
        "default": "999",
        "exchange": {
          "mopub": "\u003ca href\u003d\u0027mopub template here\u0027 \u003c/a\u003e",
          "mobclix": "\u003ca href\u003d\u0027mobclix template here\u0027 \u003c/a\u003e",
          "nexage": "\u003ca href\u003d\u0027{RTB_REDIRECT_URL}/{RTB_CAMPAIGN_ADID}/{pub}/{bid_id}?url\u003d{campaign_forward_url}\u0027\u003e\u003cimg src\u003d\u0027{RTB_PIXEL_URL}/{pub}/{ad_id}/{bid_id}/${AUCTION_PRICE}/{creative_id}\u0027 height\u003d\u00271\u0027 width\u003d\u00271\u0027\u003e\u003c/img\u003e\u003cimg src\u003d\u0027{campaign_image_url}\u0027 height\u003d\u0027{campaign_ad_height}\u0027 width\u003d\u0027{campaign_ad_width}\u0027\u003e\u003c/img\u003e\u003c/a\u003e"
        }
      },
      "attributes": [
        {
          "operator": 4,
          "value": [
            "chive.com",
            "junk.com"
          ],
          "lval": [
            "chive.com",
            "junk.com"
          ],
          "op": "NOT_MEMBER",
          "notPresentOk": true,
          "bidRequestValues": [
            "site",
            "domain"
          ]
        },
        {
          "operator": 3,
          "value": [
            "USA",
            "MEX"
          ],
          "lval": [
            "USA",
            "MEX"
          ],
          "op": "MEMBER",
          "notPresentOk": true,
          "bidRequestValues": [
            "user",
            "geo",
            "country"
          ]
        },
        {
          "operator": 3,
          "value": [
            "CA",
            "NY",
            "MA"
          ],
          "lval": [
            "CA",
            "NY",
            "MA"
          ],
          "op": "MEMBER",
          "notPresentOk": true,
          "bidRequestValues": [
            "rtb4free",
            "geocode",
            "state"
          ]
        }
      ],
      "creatives": [
        {
          "forwardurl": "http://localhost:8080/forward?{site_id}",
          "encodedFurl": "http%3A%2F%2Flocalhost%3A8080%2Fforward%3F%7Bsite_id%7D",
          "imageurl": "http://localhost:8080/images/320x50.jpg?adid\u003d{ad_id}\u0026#38;bidid\u003d{bid_id}",
          "encodedIurl": "http%3A%2F%2Flocalhost%3A8080%2Fimages%2F320x50.jpg%3Fadid%3D%7Bad_id%7D%26%2338%3Bbidid%3D%7Bbid_id%7D",
          "impid": "23skiddoo",
          "w": 320.0,
          "h": 50.0
        },
        {
          "forwardurl": "http://localhost:8080/forward?{site_id}",
          "encodedFurl": "http%3A%2F%2Flocalhost%3A8080%2Fforward%3F%7Bsite_id%7D",
          "imageurl": "http://localhost:8080/images/320x50.gif?adid\u003d{ad_id}\u0026#38;bidid\u003d{bid_id}",
          "encodedIurl": "http%3A%2F%2Flocalhost%3A8080%2Fimages%2F320x50.gif%3Fadid%3D%7Bad_id%7D%26%2338%3Bbidid%3D%7Bbid_id%7D",
          "impid": "66skiddoo",
          "w": 640.0,
          "h": 480.0
        }
      ],
      "date": [
        20130205,
        20200101
      ]
    }
  ],
  "percentage": 100,
  "stopped": false,
  "bid": 0,
  "nobid": 0,
  "error": 0,
  "handled": 1,
  "unknown": 0,
  "cmd": 5,
  "to": "*",
  "msg": "undefined",
  "status": "ok",
  "type": "status"
}
??

Load User into Redis Database
---------------------------------
This command will load a JSON formatted User record (an array of campaigns) from file into the Redis database. 
A Campaign belongs to a user and has a unique adId.

$java -cp "libs/*" tools.Commands
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??2


Delete User from Redis Database
-----------------------------------
This command deletes a campaign from the Database.

$java -cp "libs/*" tools.Commands     
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Start Campaign, 5=Stop Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??3
Which User to delete:

Start A Campaign (Load from Redis into Bidders  Memory
------------------------------------------------------
This command loads a campaign from the Database into the bidder memory, thus starting it.

$java -cp "libs/*" tools.Commands     
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??4
Which campaign to start:campaign-name
Which bidders to notify:

Pressing <cr> for "Which bidders to notify", notifies them all.

Stop a Campaign (Unload Campaign from Bidders memory)
-----------------------------------------------------
This command unloads a campaign from the bidder memory, thus  stopping it.

$java -cp "libs/*" tools.Commands     
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??5
Which campaign to stop:campaign-name
Which bidders to notify:

Pressing <cr> for "Which bidders to notify", notifies them all.

Stop Bidding
-------------
Issuing this command will cause the bidder to return a NOBID to all bid requests sent to it.

$java -cp "libs/*" tools.Commands     
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??6
Which bidder to stop:

Pressing cr for "Which bidder to start" starts them all.

Start Bidding
-------------
Issuing this command will cause the bidder to begin responding to bid requests again.

$java -cp "libs/*" tools.Commands     
RTB4FREE Commander (1=Echo, 2=Load into DB, 3=Delete from DB, 4=Stop Campaign, 5=Start Campaign, 6=Start Bidder, 7=Stop Bidder, 8=Exit)
??7
Which bidder to start:

Pressing cr for "Which bidder to start" starts them all.
