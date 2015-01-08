XRTB
=====================

NOTE: THIS IS A WORK IN PROGRESS

===========================

A Real Time Broker (RTB) 2.2 bidding engine written in Java 1.8

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

THEORY OF OPERATION
============================

1. All configuration items are placed in a file (in the example above, ./Campaigns/payday.json is used).
The top level field "instance" defines the name of the bidder, and will be used in all logging methods. Note,
XRTB writes its logs to REDIS, default channel "logs", which you can change with in the "app" object.

The "port" field defines the TCP port the XRTB server will utilize to handle bid requests.

The "seats" object is a list of seat-ids used for each of the exchanges you are bidding on. The seat-id is assigned
by the exchange - it's how they know whom is bidding.

The "app" object defines all the operational parameters used by XRTB.

The app.redis object defines the REDIS host to use and where to write bids, requests, logs and wins. ONLY the wins channel must be defined - and it must be defined! The others will write to the bids, requests and logs if the channel
has been defined.

The app.ttl defines the throttle percentage. Set to 100 and all bid requests will be considered. Set to 50 and 50% 
of the bid requests will be rejected out of hand.

the app.pixel-tracking-url field defines the URL that will be called when the ad is served up.

The app.winurl defines where the exchange is to send win notifications. It is customary to split win and bid processing across 2 domains, that share the same REDIS cache. When a bid is made, a copy is stored in REDIS, set to expire after
some period of time. When the win notification comes in the bid needs to be retrieved to complete the transaction 
with the exchange. In systems with multiple bidders, there is no way to know which XRTB will receive the win thus
you cannot store the bid information in local memory.

The app.redirect-url field defines the URL when the user clicks your advertisement.

The app.verbosity object defines the logging level for the XRTB program. Setting app.verbosity.level to 0 means only
the most critical messages are logged to REDIS log channel. Set the level ever higher to obtain more log information.

The app.verbosity.nobid-reason field is for debugging. Operational use set to false. If set to true, XRTB will print on STDOUT why the bidder chose to nobid. This is the only log message sent to STDOUT.

The "campaigns" object is an array of objects, each one representing a campaign. If you plan to bid, you must have at
least 1 campaign defined. If you have multiple campaigns, and a bid request matches 2 or more campaigns, the campaign
chosen to bid is chosen at random.

The "campaign.adm-template" field defines what the bid response ADM field will look like. This is how you define those
fields you want to send to the exchange on the bid.

The ADM field is examined by the XRTB bidder to fill in fields you want sent to the exchange. This is done using
macro substitution fields. The XRTB fields you can substitute are:
	
		{RTB_REDIRECT_URL}		Substituted from
		{RTB_CAMPAIGN_ADID}		Substituted from
		{RTB_PIXEL_URL}			Substituted from
	
		{campaign_forward_url} 	Substitured from the campaig creatives forward url
		{bid_id}					Substituted from the bid's object id field.
		{ad_id}						Substituted from the campaign id.
		{campaign_ad_price}		Substituted from the campaign's price.
		{campaign_ad_width}		Substituted from campaign creatives width
		{campaign_ad_height}		Substituted from campaign creatives height
		{creative_id}				Substituted from campaign's creative's id.
		{pub}						Substituted from the bid request exchange.
		
Note, the RTB exchange will reflect the ADM back on the win notification, and you can ask for the RTB exchange
to also substitute fields as well. See the RTB 2.1 specification for supported macro names. These are the macros
substituted by the Exchange, not the bidder. so these will return via the RTB win notification.

		{AUCTION_ID} ID of the bid request; from “id” attribute.
		{AUCTION_BID_ID} ID of the bid; from “bidid” attribute.
		{AUCTION_IMP_ID} ID of the impression just won; from “impid” attribute.
		{AUCTION_SEAT_ID} ID of the bidder’s seat for whom the bid was made.
		{AUCTION_AD_ID} ID of the ad markup the bidder wishes to serve; from “adid” attribute.
		{AUCTION_PRICE} Settlement price using the same currency and units as the bid.
		{AUCTION_CURRENCY}  The currency used in the bid (explicit or implied); for confirmation only.

The campaign-adm-template.default field sets forth the ADM field when bidding on an exchange that you have not further
defined an ADM field for. If each exchange ADM field will look the same, then just use this field.

The campaign-adm-template.exchanges is an array of objects that define specific ADM patterns for specific exchanges.
An example would be "campaign-adm-template.exchanges[0] = {"nexage":"nexage template here"}

The campaign-creatives object is an array of creatives for use with the campaign. Multiple creatives allow you to
support multiple sized ads.

The campaign-creatives.x field sets the width of the ad in pixels.

The campaign-creatives.y field sets the height of the ad in pixels.

The campaign-creatives.impid is a field you can use to assign a different accounting id for the creative.

The campaign-creatives.imageurl defines the location of the ad image itself. It must be encoded, and it too supports
the macro substitutions defined above.

The campaign-createive.forward-url field defines the campaign id , substituted with {ad_id}.

The campaign-adId is the advertisement ID.

The campaign-price is the price to use for the bid.
                

