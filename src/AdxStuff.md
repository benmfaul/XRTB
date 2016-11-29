Generally: https://developers.google.com/ad-exchange/rtb/downloads/openrtb-proto.txt

===========
id					BidRequest.id
bcat				BidRequest.AdSlot.excluded_sensitive_category + BidRequest.AdSlot.excluded_product_category (Refer to enum ContentCategory.)

==========
Imp Mapping

id					BidRequest.AdSlot.id
*** WARNING RTB Only supports 1 impression, while Google may have multiple ad slots...

==================
imp.banner Mapping

battr				BidRequest.AdSlot.excluded_attributes
pos					BidRequest.AdSlot.SlotVisibility
h					BidRequest.AdSlot.height
mimes				----> UNKNOWN. All will be presumed.
w					BidRequest.AdSlot.width

=================
imp.video Mapping

linearity			NOT SUPPORTED IN ADX. Will not be used.
protocols			NOT SUPPORTED. We will presume VAST 2 and 3, and Wrapped 2 and 3, which is what Adx provides anyway                        

battr				BidRequest.AdSlot.excluded_attributes
h					BidRequest.AdSlot.height
maxduration  		BidRequest.Video.max_duration
minduration  		BidRequest.Video.min_duration
pos					BidRequest.AdSlot.SlotVisibility
w					BidRequest.AdSlot.width	

==========
Device Mapping:

dnt					AdX does not support this field.

carrier				BidRequest.Mobile.carrier
devicetype			BidRequest.Mobile.device_type


geo.lat				BidRequest.[encrypted_]hyperlocal_set.center_point.latitude]
geo.lon				BidRequest.[encrypted_]hyperlocal_set.center_point.longitude]
geo.city			BidRequest.geo_criteria_id via geo-table.csv], See Appendix A for a link to the codes. (http://www.unece.org/cefact/locode/service/location.htm).
geo.country			BidRequest.geo_criteria_id via geo-table.csv]
geo.zip				BidRequest.postal_code

h					BidRequest.Mobile.height
ifa					BidRequest.Mobile.advertising_id (decrypted and converted to string)
language			BidRequest.language
make				BidRequest.Mobile.brand
model				BidRequest.Mobile.model
os					BidRequest.Device.platform
osv					catenate  BidRequest.Device.os.versionMajor + . + BidRequest.Device.os.versionMinor + . BidRequest.Device.versionMicro
w					BidRequest.Mobile.width

==================
app

name,bundle			BidRequest.Mobile.App.name
id					BidRequest.Mobile.App.id
url					BidRequest.url

==================
site

id					BidRequest.seller_network_id
url					BidRequest.url
