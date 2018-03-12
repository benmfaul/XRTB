RTB4FREE
=====================

These are the Docker instructions for working with Bidder3, Crosstalk, Zerospike, Kafka and Zookeeper.

No Source Deployments
===========================

Docker Swarm
---------------------------
Use Docker swarm to run Crosstalk, Bidder3, Zerospike, Kafka and Zookeeper

1. Copy docker-compose.yml from Project's docker/ directory.

2. Start the swarm

   $docker swarm init
   
3. Start the network

   $docker network create --driver overlay rtb_net --subnet=10.0.9.0/24
   
4. Deploy

   $docker stack deploy -c docker-compose.yml c1x
   
Docker Compose
-------------------------------
Use Docker Compose to run Crosstalk, Bidder3, Zerospike, Kafka and Zookeeper in a single console window:

1. Copy docker-compose.yml from Project's docker/ directory.
   
2. Start the network

   $docker network create rtb_net
   
4. Deploy

   $docker-compose up 
   
Working with Source
---------------------------------
If you want to modify the code.

1. GIT clone, cd to the cloned directory.

2. Make your changes...

3. Run maven:

   $mvn assembly:assembly -DdescriptorId=jar-with-dependencies  -Dmaven.test.skip=true
   
4. Make the docker images locally:

   $docker build -t jacamars/zerospike:v1 -f Docker.zerospike .
   $docker build -t jacamars/rtb4free:v1 -f Docker.rtb4free .
   
   Note, change the tag jacamars to your repo.
   
5. If you need to push to the C1X repo:

   $docker push jacamars/rtb4free:v1
   $docker push jacamars/zerospike:v1
   
Changing Operational Parameters
-------------------------------------
The bidder uses a container based file in Campaigns/payday.json. If you need to change the parameters within it
do it in your own copy and use volumes command to mount into it. Example, suppose you made your own copy of payday.json
and modified it and you called it ./myconfig.json. You modify the bidder services section in docker-compose.yml to
mount. Note the volumes directive:

  bidder:
    image: "jacamars/rtb4free:v1"
    environment:
      BROKERLIST: "kafka:9092"
      PUBSUB: "zerospike"
      EXTERNAL: "http://localhost:8080"
    ports:
      - "8080:8080"
      - "8100:8100"
      - "7379:7379"
    volumes:
      - ./myconfig.json:Campaigns/payday.json
    networks:
      - rtb_net
    depends_on:
      - kafka
      - crosstalk
      - zerospike
    command: bash -c "./wait-for-it.sh kafka:9092 --timeout=120 && ./wait-for-it.sh zerospike:6000 --timeout 120 && sleep 1; ./rtb4free"

Zerospike uses a cache.db file located within the container. For operational use, the real cache.db must be mounted
using the volumes command. Example, suppose you wanted to use the file mycache.db in your current working directory:

  zerospike:
    image: "jacamars/zerospike:v1"
    environment:
      BROKERLIST: "kafka:9092"
    ports:
      - "6000:6000"
      - "6001:6001"
      - "6002:6002"
    volumes:
      - "./mycache.db:/cache.db"
    networks:
      - rtb_net
    depends_on:
      - kafka
    command: bash -c "./wait-for-it.sh kafka:9092 --timeout=120 && sleep 1; ./zerospike"

Changing port assignments is not encouraged. Stick to the defaults to keep from losing your mind. There are 
a lot of interdependencies.

HANDY DOCKER ACTIVITIES
=====================================

Running Containers
--------------------------------------

    $docker ps
    
Attach to a Container
---------------------------------------
Do a docker ps and then use the container-id or name:

    $docker exec -it <id-or-name> /bin/bash
    
Attach to the log
---------------------------------------
Do a docker ps and then use the container-id or name:

    $docker logs -f <id-or-name>
    
Delete an image
----------------------------------------
Do a docker ls firsr

    $docker image ls
    
Find the container id

    $docker image rm <image-id> --force
    
Correct Checksum Error
-----------------------------------------
If docker-compose complains about a checksum after you delete a container do this:

    $docker-compose rm

Connect Intellij Debugger to Bidder, Crosstalk or Zerospike
-----------------------------------------

Bidder:
In the service for the bidder in docker-compose.yml, use ./rtb4free-jmx instead of ./rtbf4free Then in your Intellij system, create a remote debug and connect using port 9000.

Zerospike
In the service for the zerospike in docker-compose.yml, use ./zerospike-jmx instead of ./zerospike Then in your Intellij system, create a remote debug and connect using port 9000.

Crosstalk:
In the service for the crosstalk in docker-compose.yml, use ./crosstalk-jmx instead of ./crosstalk Then in your Intellij system, create a remote debug and connect using port 9000.




CONFIGURING THE BIDDER.
=====================================
In order to run the bidder, you will need to load a campaign into the bidders memory and setup some operational parameters.
 These parameters are stored in a JSON file the bidder uses when it starts. There is a sample initialization file called 
"./Campaigns/payday.json' you can use to get started. The file describes the operational parameters of the bidder. 
Look in http://rtb4free.com/details_new.html for an in depth analysis of the configuration file. Also, once you get the
bidder running, you can use the System Consolse to change the parameters using the web interface, described here:
http://rtb4free.com/admin-mgmt.html

However, here is an example file, and a brief overview

{
  "app" : {
    "concurrency" : "1",
    "deadmanswitch":"accountingsystem",
    "threads": "128",
    "stopped" : false,
    "ttl" : 300,
    "pixel-tracking-url" : "$EXTERNAL/pixel",
    "clickurl" : "$EXTERNAL/click",
    "winurl" : "$EXTERNAL/rtb/win",
    "redirect-url" : "$EXTERNAL/redirect",
    "vasturl" : "$EXTERNAL/vast",
    "eventurl" : "$EXTERNAL/track",
    "postbackurl" : "$EXTERNAL/postback",
    "adminPort" : "0",
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
    "zeromq" : {
      "bidchannel" : "kafka://[$BROKERLIST]&topic=bids",
      "winchannel" : "kafka://[$BROKERLIST]&topic=wins",
      "requests" : "kafka://[$BROKERLIST]&topic=requests",
      "clicks" : "kafka://[$BROKERLIST]&topic=clicks",
      "pixels" : "kafka://[$BROKERLIST]&topic=pixels",
      "videoevents": "kafka://[$BROKERLIST]&topic=videoevents",
      "postbackevents": "kafka://[$BROKERLIST]&topic=postbackevents",
      "status" : "kafka://[$BROKERLIST]&topic=status",
      "reasons" : "kafka://[$BROKERLIST]&topic=reasons",
      "commands": "tcp://$PUBSUB:6001&commands",
      "responses": "tcp://$PUBSUB:6000&responses",
      "xfrport": "6002",
      "requeststrategy" : "100"
    },
    "template" : {
      "default" : "{creative_forward_url}",
      "exchange" : {
        "adx" : "<a href='$BID:8080/rtb/win/{pub_id}/%%WINNING_PRICE%%/{lat}/{lon}/{ad_id}/{creative_id}/{bid_id}'}'></a><a href='%%CLICK_URL_UNESC%%{redirect_url}></a>{creative_forward_url}",
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
        "smaato-builtin" : "{creative_forward_url}",
        "pubmatic" : "{creative_forward_url}"
      }
    },
    "campaigns" : [ "" ]
  },
  "seats" : [ {
    "name" : "google",
    "id" : "google-id",
    "bid" : "/rtb/bids/google=com.xrtb.exchanges.google.OpenRTB",
    "extension" : {
      "e_key" : "kB8RQtv1rlArbt1YFRoHUiCn3mtP3d88VdfRRT+ujMA=",
      "i_key" : "+b5gBm7mJcmZgF/YT4bxZoQUsU+vpwqm2sShqc5rcPk="
    }
  }, {
    "name" : "openx",
    "id" : "openx-id",
    "bid" : "/rtb/bids/openx=com.xrtb.exchanges.openx.OpenX",
    "extension" : {
      "e_key" : "7EC9482F7636405087F6EC9851ED0E3D376182163B6646F69E9527EB9F45BE12",
      "i_key" : "6E5240C73E4A48FF961E943925BA3C5D9E9810BB918E4FB5B949BE98CED2E9F6"
    }
  }, {
    "name" : "appnexus",
    "id" : "test-appnexus-id",
    "bid" : "/rtb/bids/appnexus=com.xrtb.exchanges.appnexus.Appnexus"
  }, {
    "name" : "adx",
    "id" : "adx-seat-id",
    "bid" : "/rtb/bids/adx=com.xrtb.exchanges.adx.DoubleClick",
    "extension" : {
      "e_key" : "YOUR_E_KEY",
      "i_key" : "YOUR_I_KEY"
    }
  }, {
    "name" : "c1xus",
    "id" : "c1xus",
    "bid" : "/rtb/bids/c1xus=com.xrtb.exchanges.C1XUS&usesPiggyBackWins"
  }, {
    "name" : "stroer",
    "id" : "stroer-id",
    "bid" : "/rtb/bids/stroer=com.xrtb.exchanges.Stroer"
  }, {
    "name" : "waardx",
    "id" : "waardx-id",
    "bid" : "/rtb/bids/waardx=com.xrtb.exchanges.Generic&!usesEncodedAdm"
  }, {
    "name" : "index",
    "id" : "index-id",
    "bid" : "/rtb/bids/index=com.xrtb.exchanges.Generic"
  }, {
    "name" : "intango",
    "id" : "intango-id",
    "bid" : "/rtb/bids/intango=com.xrtb.exchanges.Generic"
  }, {
    "name" : "vdopia",
    "id" : "vdopia-id",
    "bid" : "/rtb/bids/vdopia=com.xrtb.exchanges.Generic"
  }, {
    "name" : "vertamedia",
    "id" : "vertamedia-id",
    "bid" : "/rtb/bids/vertamedia=com.xrtb.exchanges.Generic&!usesEncodedAdm&usesPiggyBackWins"
  }, {
    "name" : "ventuno",
    "id" : "ventuno-id",
    "bid" : "/rtb/bids/ventuno=com.xrtb.exchanges.Generic"
  }, {
    "name" : "medianexusnetwork",
    "id" : "mnn-id",
    "bid" : "/rtb/bids/medianexusnetwork=com.xrtb.exchanges.Generic"
  }, {
    "name" : "wideorbit",
    "id" : "wideorbit-id",
    "bid" : "/rtb/bids/wideorbit=com.xrtb.exchanges.Generic"
  }, {
    "name" : "smartadserver",
    "id" : "smartadserver-id",
    "bid" : "/rtb/bids/smartadserver=com.xrtb.exchanges.Generic"
  }, {
    "name" : "c1x",
    "id" : "c1x",
    "bid" : "/rtb/bids/c1x=com.xrtb.exchanges.C1X&usesPiggyBackWins"
  }, {
    "name" : "axonix",
    "id" : "axonix-id",
    "bid" : "/rtb/bids/axonix=com.xrtb.exchanges.Generic"
  }, {
    "name" : "adventurefeeds",
    "id" : "adventurefeedid",These are the Docker instructions for working with Bidder3, Crosstalk, Zerospike, Kafka and Zookeeper.

No Source Deployments
===========================

Docker Swarm
---------------------------
Use Docker swarm to run Crosstalk, Bidder3, Zerospike, Kafka and Zookeeper

1. Copy docker-compose.yml from Project's docker/ directory.

2. Start the swarm

   $docker swarm init
   
3. Start the network

   $docker network create --driver overlay rtb_net --subnet=10.0.9.0/24
   
4. Deploy

   $docker stack deploy -c docker-compose.yml c1x
   
Docker Compose
-------------------------------
Use Docker Compose to run Crosstalk, Bidder3, Zerospike, Kafka and Zookeeper in a single console window:

1. Copy docker-compose.yml from Project's docker/ directory.
   
2. Start the network

   $docker network create rtb_net
   
4. Deploy

   $docker-compose up 
   
Working with Source
---------------------------------
If you want to modify the code.

1. GIT clone, cd to the cloned directory.

2. Make your changes...

3. Run maven:

   $mvn assembly:assembly -DdescriptorId=jar-with-dependencies  -Dmaven.test.skip=true
   
4. Make the docker images locally:

   $docker build -t jacamars/zerospike:v1 -f Docker.zerospike .
   $docker build -t jacamars/rtb4free:v1 -f Docker.rtb4free .
   
5. If you need to push to the C1X repo:

   $docker push 64.94.191.15:5000/bidder3
   $docker push 64.94.191.15:5000/zerospike
   
Changing Operational Parameters
-------------------------------------
The bidder uses a container based file in Campaigns/payday.json. If you need to change the parameters within it
do it in your own copy and use volumes command to mount into it. Example, suppose you made your own copy of payday.json
and modified it and you called it ./myconfig.json. You modify the bidder services section in docker-compose.yml to
mount. Note the volumes directive:

  bidder:
    image: "jacamars/rtb4free:v1"
    environment:
      BROKERLIST: "kafka:9092"
      PUBSUB: "zerospike"
      EXTERNAL: "http://localhost:8080"
    ports:
      - "8080:8080"
      - "8100:8100"
      - "7379:7379"
    volumes:
      - ./myconfig.json:Campaigns/payday.json
    networks:
      - rtb_net
    depends_on:
      - kafka
      - crosstalk
      - zerospike
    command: bash -c "./wait-for-it.sh kafka:9092 --timeout=120 && ./wait-for-it.sh zerospike:6000 --timeout 120 && sleep 1; ./rtb4free"

Zerospike uses a cache.db file located within the container. For operational use, the real cache.db must be mounted
using the volumes command. Example, suppose you wanted to use the file mycache.db in your current working directory:

  zerospike:
    image: "jacamars/zerospike:v1"
    environment:
      BROKERLIST: "kafka:9092"
    ports:
      - "6000:6000"
      - "6001:6001"
      - "6002:6002"
    volumes:
      - "./mycache.db:/cache.db"
    networks:
      - rtb_net
    depends_on:
      - kafka
    command: bash -c "./wait-for-it.sh kafka:9092 --timeout=120 && sleep 1; ./zerospike"

Changing port assignments is not encouraged. Stick to the defaults to keep from losing your mind. There are 
a lot of interdependencies.

HANDY DOCKER ACTIVITIES
=====================================

Running Containers
--------------------------------------

    $docker ps
    
Attach to a Container
---------------------------------------
Do a docker ps and then use the container-id or name:

    $docker exec -it <id-or-name> /bin/bash
    
Attach to the log
---------------------------------------
Do a docker ps and then use the container-id or name:

    $docker logs -f <id-or-name>
    
Delete an image
----------------------------------------
Do a docker ls firsr

    $docker image ls
    
Find the container id

    $docker image rm <image-id> --force
    
Correct Checksum Error
-----------------------------------------
If docker-compose complains about a checksum after you delete a container do this:

    $docker-compose rm

Connect Intellij Debugger to Bidder3, Crosstalk or Zerospike
-----------------------------------------

Bidder:
In the service for the bidder in docker-compose.yml, use ./rtb4free-jmx instead of ./rtbf4free Then in your Intellij system, create a remote debug and connect using port 9000.

Zerospike
In the service for the zerospike in docker-compose.yml, use ./zerospike-jmx instead of ./zerospike Then in your Intellij system, create a remote debug and connect using port 9000.

Crosstalk:
In the service for the crosstalk in docker-compose.yml, use ./crosstalk-jmx instead of ./crosstalk Then in your Intellij system, create a remote debug and connect using port 9000.



    "bid" : "/rtb/bids/adventurefeeds=com.xrtb.exchanges.Adventurefeeds"
  }, {
    "name" : "adprudence",
    "id" : "adprudenceid",
    "bid" : "/rtb/bids/adprudence=com.xrtb.exchanges.Adprudence"
  }, {
    "name" : "citenko",
    "id" : "citenkoid",
    "bid" : "/rtb/bids/citenko=com.xrtb.exchanges.Citenko"
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
    "name" : "smaato-builtin",
    "id" : "seat1",
    "bid" : "/rtb/bids/smaato-builtin=com.xrtb.exchanges.Smaato"
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
  }, {
    "name" : "xapads",
    "id" : "xapads-id",
    "bid" : "/rtb/bids/xapads=com.xrtb.exchanges.Generic&!usesEncodedAdm"
  }, {
    "name" : "admedia",
    "id" : "admedia-id",
    "bid" : "/rtb/bids/admedia=com.xrtb.exchanges.Generic&!usesEncodedAdm"
  }],
  "s3" : {
    "access_key_id" : "AKIAIIXPW3EUYOH76KOA",
    "secret_access_key" : "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",
    "region" : "us-east-1",
    "bucket" : "rtb-bidder-lists"
  },
  "lists" : [ ]
}

Bidder3 writes its logs to Kafka, topic is 'logs' shown in the app.zeromq object above.The "seats" object is a list of seat-ids used for each of the exchanges you are bidding on. The seat-id is assigned by the exchange - it's how they know whom is bidding. The name attribute defines the name of the exchange, as it will appear in all the logs. The id is the actual id name the bidder sends to the exchange as the seat id - how the exchange knows who you are. The bid attribute tells the bidder where the JAVA class is for that exchange. In the above example, 3 exchanges are described.

The "app" object sets up the rest of the configuration for the RTB4FREE server

The "geotags" object defines the location of two files used by RTB4FREE to determine state, county, and zipcode information from GPS coordinates found in the bid request. The geotags object is not required.

The app.password defines the password used by the root login on the campaign management and system administration page.

The app.ttl defines the time to live value for bid keys in Zerospike, in seconds. This means that after 5 minutes the key is deleted - and if the WIN notification comes in you will not process it - as the key is gone. You cannot let the keys pile up forever, Aerospile will run out of memory. This is a compromise between accuracy and performance.

The app.pixel-tracking-url field defines the URL that will be called when the ad is served up.

The app.winurl defines where the exchange is to send win notifications. It is customary to split win and bid processing across 2 domains, that share the same Aerospike cache. When a bid is made, a copy is stored in Aerospike, set to expire after some period of time (app.tt;). When the win notification comes in the bid needs to be retrieved to complete the transaction with the exchange. In systems with multiple bidders, there is no way to know which XRTB will receive the win thus you cannot store the bid information in local memory.

The app.redirect-url field defines the URL to use when the user clicks your advertisement.

The app.verbosity.nobid-reason field is for debugging and is used to tell you why the bidder did not bid. This is useful if things aren't working like you think it should. It creates a lot of output and it doubles the amount of time it takes to process a bid request. Operational useers should set this set to false. If set to true, the bidder log why the bidder chose to nobid on each creative, for each campaign.

The app.multibid flag denotes whether or not to support multiple bids for requests, the default is false.

The "campaigns" object is an array of campaign names (by adId) that will be initially loaded from Aerospike backed database and into the bidder's local memory. In the Campaigns/payday.json file, for demo purposes there is one campaign pre-loaded for you called "ben:payday". Note, this field accepts JAVA regular expressions. In the example the campaign that matches 'ben:payday' is loaded. To load all campaigns use '(.*). To load only campaigns prefixed with 'ben', then use 'ben(.*)'.

If you plan to bid (and win), you must have at least 1 campaign loaded into the bidder. If you have multiple campaigns, and a bid request matches 2 or more campaigns, the campaign to bid is chosen at random.

THEORY OF OPERATION
=====================================
Zerospike is used as the shared context  between all bidders. All shared data is kept in Zerospike, and all  bidders connect to this 
service to share data. Specifically, the response to a bid request, a 'bid', is stored
in Zerospike after it is made, because on the win notification, a completely separate bidder may process the win, and the
original bid must be retrieved as quickly as possible to complete the transaction. A database query is far to slow to accomplish
this. The frequency caps are also kept in Zerospike. 

ZeroMQ is used as the publish/subscribe system. Commands are sent to running bidders over ZeroMQ publish channel.
Likewise responses to commands are sent back on another ZeroMq channel, 'responses'. Clickthrough, wins, and pixel-file notification is sent on yet channels, as set forth in the app.zeromq object.

Shared Database
-------------------------------
A database of Users and their campaigns is kept in a  ConcurrentHashMap, that is stored in Zerospike as a Map. This
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




