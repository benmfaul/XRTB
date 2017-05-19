package com.xrtb.fraud;

public interface FraudIF {

	public FraudLog bid(String rt, String ip, String url, String ua, String seller, String crid) throws Exception;
	public boolean bidOnError();
}
