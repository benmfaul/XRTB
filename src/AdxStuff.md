Generally: https://developers.google.com/ad-exchange/rtb/downloads/openrtb-proto.txt

===========
id					BidRequest.id
bcat				BidRequest.AdSlot.excluded_sensitive_category + BidRequest.AdSlot.excluded_product_category (Refer to enum ContentCategory.)

==========
Imp Mapping

id					BidRequest.AdSlot.id

==================
imp.banner Mapping

pos					BidRequest.AdSlot.SlotVisibility
h					BidRequest.AdSlot.height
mimes				
w					BidRequest.AdSlot.width

=================
imp.video Mapping

linearity			NOT SUPPORTED IN ADX. Will not be used.
protocols			NOT SUPPORTED                           

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


==================
site


AdCategoryMapper	Maps between AdX's ad (product, restricted and sensitive) categories, and OpenRTB's IAB-based OpenRtb.ContentCategory.
AdPositionMapper	Maps between AdX's NetworkBid.BidRequest.AdSlot.SlotVisibility and OpenRTB's OpenRtb.BidRequest.Impression.AdPosition.
BannerMimeMapper	Maps between AdX's NetworkBid.BidRequest.Video.CompanionSlot.CreativeFormat and OpenRTB's mime types for banners.
CompanionTypeMapper	Maps between AdX's NetworkBid.BidRequest.Video.CompanionSlot.CreativeFormat and OpenRTB's OpenRtb.BidRequest.Impression.Video.CompanionType.
ContentRatingMapper	Maps between AdX's detected_content_label and OpenRTB's contentrating.
CreativeAttributeMapper	Maps between AdX creative attributes and OpenRTB's OpenRtb.CreativeAttribute.DeviceTypeMapper	
Maps between AdX's NetworkBid.BidRequest.Mobile.MobileDeviceType and OpenRTB's OpenRtb.BidRequest.Device.DeviceType.DoubleClickLinkMapper	
Extension mapper for DoubleClick "Link"extensions: each OpenRTB object will have an extension that's just a reference for the corresponding node in the native message (which also happens to be protobuf-based, so we can do this).
DoubleClickOpenRtbMapper	
Mapping between the DoubleClick and OpenRTB models.
ExpandableDirectionMapper	
Maps between AdX's excluded_attribute and OpenRTB's OpenRtb.BidRequest.Impression.Banner.ExpandableDirection.
ExtMapper	Extension mapper for DoubleClickOpenRtbMapper.
GenderMapper	Maps between AdX's NetworkBid.BidRequest.UserDemographic.Gender and OpenRTB's gender.
IFramingStateMapper	Maps between AdX's IFramingState and OpenRTB's topframe.
MapperUtil	Utilities for Mappers.
NullDoubleClickOpenRtbMapper	Dummy implementation of OpenRtbMapper, maps all messages to null.
VideoMimeMapper	Maps between AdX's NetworkBid.BidRequest.Video.VideoFormat and OpenRTB's mime types for video.
VideoStartDelayMapper	Maps between AdX's videoad_start_delay and OpenRTB's startdelay.