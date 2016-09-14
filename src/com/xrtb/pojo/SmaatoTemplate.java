package com.xrtb.pojo;

import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;

public class SmaatoTemplate  {

	public static String  IMAGEAD_TEMPLATE = "" +
	"<ad xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:noNamespaceSchemaLocation=\\\"smaato_ad_v0.9.xsd\\\" modelVersion=\\\"0.9\\\">" +
	"<imageAd>" +
	"<clickUrl>__CLICKURL__</clickUrl>" +
	"<imgUrl>__IMAGEURL__</imgUrl>" +
	"<width>{creative_ad_width}</width>" +
	"<height>{creative_ad_height}</height>" +
	"<toolTip>__TOOLTIP__</toolTip>" +
	"<additionalText>__ADDITIONALTEXT__</additionalText>" +
	"<beacons>" +
	"<beacon>__PIXELURL__</beacon>" +
	"</beacons>" +
	"</imageAd>" +
	"</ad>";
	
	static String  TEXTAD_TEMPLATE = "" +
			"<ad>" +
			"<textAd>" +
			"<clickUrl>__CLICKURL__</clickUrl>" +
			"<clickTextl>__TEXT__</imgUrl>" +
			"<toolTip>__TOOLTIP__</toolTip>" +
			"<additionalText>__ADDITIONALTEXT__</additionalText>" +
			"<beacons>" +
			"<beacon>__PIXELURL__</beacon>" +
			"</beacons>" +
			"</textAd>" +
			"</ad>";
	
	static String RICHMEDIA_TEMPLATE = "" +
            "<ad xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:noNamespaceSchemaLocation=\\\"http://standards.smaato.com/ad/smaato_ad_v0.9.xsd\\\" modelVersion=\\\"0.9\\\">" +
            "<richmediaAd>" +
            "<content>" +
            "<![CDATA[ __JAVASCRIPT__ ]]>" +
            "</content>" +
            "<width>{creative_ad_width}</width>" +
            "<height>{creative_ad_height}</height>" +
            "<beacons>" +
            "<beacon>__PIXELURL__</beacon>" +
            "<beacon>__RICHMEDIABEACON__</beacon>" +
            "</beacons>" +
            "</richmediaAd>" +
            "</ad>";
	
}

