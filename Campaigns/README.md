The Campaigns JSON file sets forth the configuration used by the bidder at startup. A copy of payday.json is included so you can reference from here. Using Javascript notation, the configuration object has these parts:

config.instance = "Sample payday loan campaigns";   // this defines the header used in the logger.
config.port = 8080;									// defines the HTTP port the bidder listens on
config.seats.smaato = "1111";						// the seat bid id of your bidder as known by the exchange, in this case 'smaato'
config.seats.nexaage = "99999999";					// The seat bid id for your bidder as known by nexage.

config.app.ttl = 200,
config.app["pixel-tracking-url"] =  "http://testcps3.tapinsystems.net:3000/pixel";     	// the URL of your pixel tracking site.
config.app.winurl = "http://localhost:9090/xrtb/win";									// The url the exchange uses for win notifications.
confug.app["redirect-url"] = "http://testcps3.tapinsystems.net:3000/redirect";			// the URL that is used for XXXXXXX
config.app.verbosity.level = 2;
config.app.nobid-reason: true;
config.app.redis.host =  "localhost";
config.app.redis.bidchannel: "bids";
config.app.redis.winchannel: "wins";
config.app.redis.port": 6379;

config.app.redis[redis-bids"].host = "localhost";
config.app.redis[redis-bids"]writerequests = true;
config.app.redis[redis-bids"].channel = "requests";
config.app.redis[redis-bids"].port": 6379;

config.app.campaigns[] = []; 						// the list of campaigns. Each campaign is an object following the form below

campaign = {};
campaign.adm_template = {}





                "campaign-adm-template": {
                    "default": "<a href='{RTB_REDIRECT_URL}/{RTB_CAMPAIGN_ADID}/{pub}/{bid_id}?url={campaign_forward_url}'><img src='{RTB_PIXEL_URL}/{pub}/{ad_id}/{bid_id}/${AUCTION_PRICE}/{creative_id}' height='1' width='1'><img src='{campaign_image_url}' height='{campaign_ad_height}' width='{campaign_ad_width}'></a>",
                    "exchange": {
                        "mopub": "<mopub template here>",
                        "mobclix": "<mobclix template here>"
                    }
                },
                "campaign-attributes": [
                    {
                        "site.domain": {
                            "values": [
                                "chive.com",
                                "junk.com"
                            ],
                            "op": "NOT_MEMBER"
                        }
                    },
                    {
                        "user.geo.country": {
                            "values": ["USA","MEX"],
                            "op": "MEMBER"
                        }
                    }
                ],
                "campaign-adomain": "originator.com",
                "campaign-name": "campaign-1-full-test",
                "campaign-date": [
                    20130205,
                    20200101
                ],
                "campaign-target": "bullseye",
                "campaign-creatives": [
                    {
                        "forwardurl": "http://rtb4.tapinsystems.net/?{siteid}",
                        "imageurl": "http://d21a3h018cqvjt.cloudfront.net/rtbiq/IQ_070913_320x50.gif?adid={adid}&#38;bidid={oid}",
                        "impid": "23skiddoo",
                        "w": 320,
                        "h": 50
                    },
                    {
                    	"forwardurl": "http://rtb4.tapinsystems.net/?{siteid}",
                        "impid": "66skiddoo",
                         "imageurl": "http://d21a3h018cqvjt.cloudfront.net/rtbiq/IQ_070913_640x480.gif?adid={adid}&#38;bidid={oid}",
                        "w": 640,
                        "h": 480
                    }
                ],
                "campaign-impid": "23skiddoo",
                "campaign-adId": "id123",
                "campaign-campaignId": "campaignFromHell",
                "campaign-targetingId": "bullseye",
                "campaign-price": 5.0,
                "campaign-bidsPerDay": 10000,
                "campaign-siteTargetingId": "abc"
            }
        ]
    }
}