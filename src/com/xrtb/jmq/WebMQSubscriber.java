package com.xrtb.jmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebMQSubscriber {
	
	ObjectMapper mapper = new ObjectMapper();
	
	public WebMQSubscriber(HttpServletResponse response, String port, String topics) {
		  // Prepare our context and subscriber
		
		
        Context context = ZMQ.context(1);
        Socket subscriber = context.socket(ZMQ.SUB);

        subscriber.connect("tcp://localhost:" + port);
        
        String [] parts = topics.split(",");
        for (String topic : parts) {
        	subscriber.subscribe(topic.getBytes());
        }

        while (!Thread.currentThread ().isInterrupted ()) {
            // Read envelope with address
            String address = subscriber.recvStr ();
            // Read message contents
            String contents = subscriber.recvStr ();
            Map m = new HashMap();
            m.put("topic", address);
            m.put("message", contents);
           
            try {
            	contents = mapper.writeValueAsString(m);
				response.getWriter().println(contents);
				response.flushBuffer();       	
			} catch (IOException e) {
				//e.printStackTrace();
				break;
			}               
        }
        subscriber.close ();
        context.term ();
	}
}
