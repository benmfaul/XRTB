package com.xrtb.common;

import com.xrtb.pojo.Bid;
import com.xrtb.pojo.BidRequest;
import com.xrtb.pojo.WinObject;

public interface LoggerIF {
	public void writeBid(Bid b);
	public void writeRequest(BidRequest br);
	public void writeWin(WinObject w);
}
