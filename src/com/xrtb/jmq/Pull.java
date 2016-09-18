package com.xrtb.jmq;

import javax.servlet.http.HttpServletResponse;

import org.zeromq.ZMQ;

public class Pull {

	public Pull(HttpServletResponse response, String port, String timeout, String limit) throws Exception {
		int lim = 1;
		if (limit != null) {
			lim = Integer.parseInt(limit);
		}
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket rcv = context.socket(ZMQ.PULL);
		rcv.bind("tcp://*:" + port);
		if (timeout != null) {
			int t = Integer.parseInt(timeout);
			rcv.setReceiveTimeOut(t);
		}
		
		int k = 0;
		while(k < lim || lim == 0) {
			String str = rcv.recvStr();		
			response.getWriter().println(str);
			k++;
		}
		
		rcv.close();
		context.term();
	}
}