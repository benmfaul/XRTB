package com.xrtb.bidder;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * A class to keep track of users logged in to the system
 * @author Ben M. Faul
 *
 */


public class CustomListener implements HttpSessionListener {

	static List<HttpSession> sessions = new ArrayList();
	
	@Override
	public void sessionCreated(HttpSessionEvent ev) {
		HttpSession session = ev.getSession();
		String id = session.getId();

		sessions.add(session);
		//System.out.println("SESSION: " + id + " was created");
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent ev) {
		HttpSession session = ev.getSession();
		String id = session.getId();

		sessions.remove(session);
		//System.out.println("SESSION: " + id + " was destroyed");
		
	}
	
	public static List<Map> getSessions() {
		List<Map> list = new ArrayList();
		for (HttpSession s : sessions) {
			Map data = new HashMap();
			data.put("username",s.getAttribute("username"));
			data.put("id", s.getId());
			data.put("page", s.getAttribute("path"));	
			list.add(data);
		}
		return list;
	}

}
