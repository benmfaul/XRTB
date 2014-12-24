package com.xrtb.pojo;

/**
 * POJO for construction of no-bid object
 */
public class NoBid {
	/** Reason doesn't bid */
	String reason = "na";
	/** id of the bid request */
	String id;
	
	public NoBid() {
		
	}
	public NoBid(String id, String reason) {
		this.id = id;
		this.reason = reason;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (reason != null) {
			sb.append("\"reason\":\"");
			sb.append(reason);
			sb.append("\"");
		}

		if (id != null) {
			sb.append(",");
			sb.append("\"id\":\"");
			sb.append(id);	
			sb.append("\"");	
		}
		sb.append("}");
		return new String(sb);
	}
}
