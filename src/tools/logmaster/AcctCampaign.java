package tools.logmaster;

import java.util.HashSet;
import java.util.Set;

public class AcctCampaign {

	public HashSet<AcctCreative> creatives = new HashSet();
	public int bids;
	public int wins;
	public double bidPrice;
	public double winPrice;
	
	public String  name;
	
	public AcctCampaign() {
		
	}
	
	public AcctCampaign(String name) {
		this.name = name;
	}
}
