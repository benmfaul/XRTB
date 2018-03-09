package com.xrtb.exchanges;

import com.xrtb.bidder.SelectedCreative;
import com.xrtb.common.Campaign;
import com.xrtb.common.Creative;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.BidResponse;
import com.xrtb.pojo.Impression;

import java.io.InputStream;
import java.util.List;

public class Stroer extends BidRequest {
	
	public Stroer() {
		super();
		parseSpecial();
	}
	
	/**
	 * Make a Atomx bid request using a String.
	 * @param in String. The JSON bid request for smartyads
	 * @throws Exception on JSON errors.
	 */
	public Stroer(String  in) throws Exception  {
		super(in);
		parseSpecial();
    }	
	
	/**
	 * Make a Atomx bid request using an input stream.
	 * @param in InputStream. The contents of a HTTP post.
	 * @throws Exception on JSON errors.
	 */
	public Stroer(InputStream in) throws Exception {
		super(in);
		parseSpecial();
	}
	
	/**
	 * Create a new Atomx Exchange object from this class instance.
	 * @param in InputStream. The JSON Input.
	 * @throws Exception on parse errors.
	 */
	@Override
	public Stroer copy(InputStream in) throws Exception  {
		Stroer copy =  new Stroer(in);
		copy.usesEncodedAdm = usesEncodedAdm;
		return copy;
	}
	
	
	/**
	 * Process special Atomx stuff, sets the exchange name.
	 */
	@Override
	public boolean parseSpecial() {
		setExchange( "stroer" );
		usesEncodedAdm = false;
		return true;
	}
	
	
	@Override
	public BidResponse buildNewBidResponse(Impression imp, Campaign camp, Creative creat, double price, 
			String dealId,  int xtime) throws Exception {
		
		BidResponse response = new BidResponse(this,  imp, camp,  creat,
				 this.id,  price,  dealId,  xtime);
		
		
		if (creat.extensions == null || creat.extensions.size() == 0)
			throw new Exception(camp.adId + "/"+ creat.impid + " is missing required extensions for Stroer SSP");
		
		String avr = creat.extensions.get("avr");
		String avn = creat.extensions.get("avn");
		
		if (avr == null || avn == null)
			throw new Exception(camp.adId + "/" + creat.impid + " is missing required avn or avr extension for Stroer SSP");
		
		String rets = getExtension(avr,avn);
		
		StringBuilder sb = response.getResponseBuffer();
		int index = sb.indexOf("\"crid");
		if (index < 0)
			throw new Exception("Could not insert extension, response has no crid");
		
		sb.insert(index,rets);
		
		return response;
	}
	
	@Override
	public BidResponse buildNewBidResponse(List<SelectedCreative> multi, int xtime) throws Exception {
		String avr = null;
		String avn = null;
		
		BidResponse response = new BidResponse(this, multi, xtime);
		StringBuilder sb = response.getResponseBuffer();
		
		for (int i=0; i<multi.size();i++) {
			SelectedCreative x = multi.get(i);
			Creative c = x.getCreative();
			if (c.extensions == null || c.extensions.size() == 0)
				throw new Exception(x.getCampaign().adId + "/" + c.impid + " is missing required extensions for Stroer SSP");
			
			avr = c.extensions.get("avr");
			avn = c.extensions.get("avn");
			
			if (avr == null || avn == null)
				throw new Exception(x.getCampaign().adId + "/" + c.impid + " is missing required avn or avr extension for Stroer SSP");
			
			String rets = getExtension(avr,avn);
			
			int index = sb.indexOf("\"crid");
			if (index < 0)
				throw new Exception("Could not insert extension, response missing a crid");
			
			sb.insert(index,rets);
		}
		
		return response;
	}
	
	/**
	 * Makes sure the Stroer keys are available on the creative
	 * @param creat Creative. The creative in question.
	 * @param errorString StringBuilder. The error handling string. Add your error here if not null.
	 * @returns boolean. Returns true if the Exchange and creative are compatible.
	 */
	@Override
	public boolean checkNonStandard(Creative creat, StringBuilder errorString) {
		if (creat.extensions == null || creat.extensions.get("avr") == null || creat.extensions.get("avn") == null) {
			if (errorString != null) {
				errorString.append(creat.impid);
				errorString.append(" ");
				errorString.append("Missing extenstions for Stroer");
			}
			return false;
		}
		return true;
	}
	
	private String getExtension(String avr, String avn) {
		/**
		 * Now patch the extension in.
		 */
		StringBuilder sb = new StringBuilder("\"ext\":{\"");
		sb.append("avn\":\"");
		sb.append(avn);
		sb.append("\",\"avr\":\"");
		sb.append(avr);
		sb.append("\"},");
		
		return sb.toString();
	}
}
