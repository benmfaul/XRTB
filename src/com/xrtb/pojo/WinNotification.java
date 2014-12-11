package com.xrtb.pojo;

public class WinNotification {
	private String reason;
	private String id;
	
	public String getString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (reason != null) {
			sb.append("\"reason\":\"");
			sb.append(reason);
		}
		if (id != null) {
			sb.append("\"id\":\"");
			sb.append(id);	
		}
		sb.append("}");
		return new String(sb);
	}
}
