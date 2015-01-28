package com.xrtb.geo;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.node.DoubleNode;

import com.xrtb.common.Node;
import com.xrtb.pojo.BidRequest;

/**
 * A subclass of Node, this handles GEO specific queries. You can use this as a template for your own extensions. You create the constructor with the
 * same signature as this class. Any specialized values can be passed in the type parameter. Here it is a string, but it can be any class your node subbclass
 * ,ight need for specific processing.
 * <p>
 * Note, in your campaign you need to specify the class that will be used. For example, an example membet of attributes list in the campaign that
 * employs this node would look like this:
 * <pre>
 *	{ 	
 *		"extension": "com.xrtb.geo.GeoNode",
 *		"subtype":"STATE",
 *		"value": ["CA","NY", "MA"],     
 *		"op": "MEMBER"  
 * }
 * </pre>
 * @author Ben M. Faul
 *
 */

public class GeoNode extends Node {

	/** Use to indicate STATE selection */
	public static final int STATE = 1;
	/** Use to indicate COUNTY selection */
	public static final int COUNTY = 2;
	/** Use to indicate ZIPCODE selection */
	public static final int ZIPCODE = 3;
	/** Use to indicate city selection */
	public static final int CITY = 4;
	
	/** The operator type */
	int type = STATE;
	
	/** where the latitiude lives */
	static List<String> lat = Arrays.asList("device", "geo", "lat");
	/** Where the longitude lives */
	static List<String> lon = Arrays.asList("device", "geo", "lon");
	/** The Geotag used by the GeoNode, this solves the GPS to location */
	public static GeoTag tag;
	
	/**
	 * Create a new GeoNode attribute.
	 * @param name String. The name of the node. For doc purposes only.
	 * @param type Object. This is CITY, STATE, COUNTY or ZIPCODE
	 * @param operator String. The operator to use, see Node.
	 * @param value Object. The value being tested.
	 * @throws Exception on file access errors.
	 */
	public GeoNode(String name, Object type , String operator,Object value) throws Exception {
		this.name = name;
		this.operator = OPS.get(operator);
		this.value = value;
		this.op = operator;
		if (type.equals("STATE")) this.type = STATE;
		if (type.equals("COUNTY")) this.type = COUNTY;
		if (type.equals("ZIPCODE")) this.type = ZIPCODE;
		if (type.equals("CITY")) this.type = CITY;
		setValues();
	}
	
	/**
	 * Queries the bid request for lat, lon, then computes, zip, city, state, county from that and tests the results/
	 * @param br BidRequest. The bidrequest being queried.
	 * @return boolean. Returns true ig the operation against the bid request succeeded.
	 * @throws Exception on javascript errors.
	 */
	@Override
	public boolean test(BidRequest br) throws Exception {
		brValue = br.interrogate(bidRequestValues);
		
		Object x = br.interrogate(lat);
		if (x == null)
			return false;
		DoubleNode latitude = (DoubleNode)x;
		x = br.interrogate(lon);
		if (x == null)
			return false;
		DoubleNode longitude = (DoubleNode)x;
		Solution solution = tag.getSolution(latitude.getDoubleValue(), longitude.getDoubleValue());
		if (solution == null)
			return false;
		
		switch(type) {
		case STATE:
			x = solution.state;
			break;
		case COUNTY:
			x = solution.county;
			break;
		case CITY:
			x = solution.city;
			break;
		case ZIPCODE:
			x = solution.code;
			break;
		}
		
		boolean test = testInternal(x);
		//System.out.println( " = " + test);
		if (code != null && shell != null) {
			try {
				Object t = shell.exec(code);
				if (t instanceof Boolean) 
					test = (Boolean)t;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return test;
	}
}
