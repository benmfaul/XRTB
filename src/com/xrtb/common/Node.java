package com.xrtb.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import com.xrtb.pojo.BidRequest;

/**
 * A class that implements a parse-able node in the RTB object, and applies campaign logic. The idea of the node
 * is to define a constraint using the dotted form of the JSON specification of the bid request parameter. By default
 * if you specify a Node and the hierarchy does not exist in the bid request, then this means the campaign does not match. You can
 * override this behavior by setting the object's 'notPresentOk' flag. Then when the hierarchy doesn;t exist, the Node tests true
 * otherwise if it is present, then the Node returns the value of the comparison that was specified.
 * <p>
 * Examples Hierarchies:
 * <p>
 * Retrieve a value: 'user.geo.country' - this means the Node will extract this field from the bid request.
 * <p>
 * Retrieve a value from a list: 'imp.0.id' is equivalent to JS: value = imp[0].id;
 * <p>
 * You also specify what to compare the bid request's values to. In could be that the return values are an array, or maybe just a
 * scalar. The Node can handle both data types.
 * <p>
 * The comparison operators are equal, not equal, lt, le, gt, ge Member of set, not member of set, set intersects, not set intersects, geo in range lat/lon,
 * not in range of lat/lon, in the domain of a range of numbers, and not in the range of numbers.
 * <p>
 * TODO: Query
 * @author Ben M. Faul
 *
 */
public class Node {
	/** Query TBD */
	public static final int QUERY = 0;
	/* Test for equality */
	public static final int EQUALS = 1;
	/* Test for inequality */
	public static final int NOT_EQUALS = 2;
	/** Test for set membership */
	public static final int MEMBER = 3;
	/** Test for not membership */
	public static final int NOT_MEMBER = 4;
	/** Test set intersection */
	public static final int INTERSECTS = 5;
	/** Test not intersection */
	public static final int NOT_INTERSECTS = 6;
	/** Test lat/lon range with other lat/lon, in km */
	public static final int INRANGE = 7;
	/** Test not in range */
	public static final int NOT_INRANGE = 8;
	/** Test less than numeric */
	public static final int LESS_THAN = 9;
	/** Test less than equal numeric */
	public static final int LESS_THAN_EQUALS = 10;
	/** Test greater than numeric */
	public static final int GREATER_THAN = 11;
	/** Test greater than equal numeric */
	public static final int GREATER_THAN_EQUALS = 12;
	/** Test in domain x less than y greater than z */
	public static final int DOMAIN = 13;
	/** Test not in domain */
	public static final int NOT_DOMAIN = 14;
	/** A convenient map to turn string operator references to their int conterparts */
	public static Map<String,Integer> OPS = new HashMap();
	static {
		OPS.put("QUERY",QUERY);
		OPS.put("EQUALS",EQUALS);
		OPS.put("NOT_EQUALS",NOT_EQUALS);
		OPS.put("MEMBER",MEMBER);
		OPS.put("NOT_MEMBER",NOT_MEMBER);
		OPS.put("INTERSECTS",INTERSECTS);
		OPS.put("NOT_INTERSECTS",NOT_INTERSECTS);
		OPS.put("INRANGE",INRANGE);
		OPS.put("NOT_INRANGE",NOT_INRANGE);
		OPS.put("LESS_THAN",LESS_THAN);
		OPS.put("LESS_THAN_EQUALS",LESS_THAN_EQUALS);
		OPS.put("GREATER_THAN",GREATER_THAN);
		OPS.put("GREATER_THAN_EQUALS",GREATER_THAN_EQUALS);
		OPS.put("DOMAIN",DOMAIN);
		OPS.put("NOT_DOMAIN",NOT_DOMAIN);
	}

	/** campaign identifier */
	public String name;			
	/** dotted form of the item in the bid to pull (eg user.geo.lat) */
	transient public String hierarchy;       
	/** which operator to use */
	transient public int operator = -1;     
	/** Node's value as an object. */
	public Object value;
	/** Node's value as a map */
	transient Map mvalue;			   
	/** The retrieved object from the bid, as defined in the hierarchy */
	transient protected Object brValue;
	
	/** when the value is a number */
	transient Number ival = null; 	
	/** when the value is a string */
	transient String sval = null;
	/** when the value is a set */
	transient Set qval = null;	
	/** when the value is a map */
	transient Map mval = null;
	/** When the value is a list */
	transient List lval = null;
	
	/** if present will execute this JavaScript code */
	protected String code = null;	
	/** context to execute in */
	public JJS shell =   null;
	/** text name of the operator */
	public String op;		
	
	/** set to false if required field not present */
	public boolean notPresentOk = true;    
	/** decomposed hierarchy */
	public List<String> bidRequestValues = new ArrayList();	  
	
	
	/**
	 * Simple constructor useful for testing.
	 */
	public Node() {
		
	}
	
	/**
	 * Constructor for the campaign Node
	 * @param name. String - the name of this node.
	 * @param hierarchy. String - the hierarchy in the request associated with this node.
	 * @param operator. int - the operator to apply to this operation.
	 * @param value. Object - the constant to test the value of the hierarchy against.
	 * @throws Exception if the values obejct is not recognized.
	 */
	public Node(String name, String hierarchy ,String operator,Object value) throws Exception {
		this.name = name;
		this.hierarchy = hierarchy;
		op = operator;
		this.value = value;

		setBRvalues();
		setValues();

	}
	
	/**
	 * Sets the values from the this.value object.
	 * @throws Exception if this.values is not a recognized object.
	 */
	public void setValues() throws Exception {
		if (value instanceof Integer || value instanceof Double) {
			ival = (Number)value;
		}
		if (value instanceof TreeSet) 
			qval = (TreeSet)value;
		if (value instanceof String)
			sval = (String)value;
		if (value instanceof Map)
			mval = (Map)value;
		if (value instanceof List) {       // convert ints to doubles
			lval = (List)value;
		}
		
		if (op != null) {
			Integer x = OPS.get(op);
			if (x == null)
				throw new Exception("Unknown operator: " + op);
			operator = x.intValue();
		}
		
		hierarchy = "";
		for (int i = 0; i < bidRequestValues.size();i++) {
			hierarchy += bidRequestValues.get(i);
			if (i + 1 >= bidRequestValues.size() == false) {
				hierarchy += ".";
			}
		}
	}
	
	/**
	 * Does this atrribute have this hierarchy
	 * @param str String. The string to test.
	 * @return true if the hierarchy matches the string
	 */
	public boolean equals(String str) {
		if (hierarchy == null) {
			hierarchy = "";
			for (int i=0;i<bidRequestValues.size();i++) {
				hierarchy += bidRequestValues.get(i);
				if (i+1 !=bidRequestValues.size()) {
					hierarchy += ".";
				}
			}
		}
		return str.equals(hierarchy);
	}
	
	/**
	 * Constructor for campaign node without attached JavaScript code
	 * @param name String. The name of the node.
	 * @param heirarchy The dotted notation hierarchy associated with this node.
	 * @param operator int. The operation to apply to the node.
	 * @param value Object. The value that the bid request specified by hierarchy will be tested against.
	 * @throws Exception if the value object is not recognized.
	 */
	public Node(String name, String heirarchy ,int operator,Object value)  throws Exception {
		this(name,heirarchy,"EQUALS",value);              // fake this out so we don't call recursively
		this.operator = operator;
		setValues();
	}
	
	/**
	 * Constructor for the campaign Node with associated JavaScript
	 * @param name. String - the name of this node.
	 * @param heirarchy. String - the hierarchy in the request associated with this node.
	 * @param operator. int - the operator to apply to this operation.
	 * @param value. Object - the constant to test the value of the hierarchy against.
	 * @param code. String - the Java code to execute if node evaluates true.
	 * @param shell. JJS - the encapsulated Nashhorn context to use for this operation.
	 * @throws Exception if the value object is not recognized.
	 */
	public Node(String name, String heirarchy ,String operator,Object value, String code, JJS shell) throws Exception {
		this(name,heirarchy,operator,value);
		this.code = code;
		this.shell = shell;
	}
	
	/**
	 * Set the bidRequest values array from the hierarchy
	 */
	void setBRvalues() {
		String[] splitted = hierarchy.split("\\.");
		for (String s : splitted) {
			bidRequestValues.add(s);
		}
	}
	
	/**
	 * Test the bidrequest against this node
	 * @param br. BidRequest - the bid request object to test.
	 * @return boolean - returns true if br-value op value evaluates true. Else false.
	 * @throws Exception if the request object and the values are not compatible.
	 */
	public boolean test(BidRequest br) throws Exception {
		brValue = br.interrogate(hierarchy);
		//System.out.print("TEST: " + this.heirarchy);
		boolean test = testInternal(brValue);
		return test;
	}
	
	/**
	 * Internal version of test() when recursion is required (NOT_* form)
	 * @param value. Object. Converts the value of the bid request field (Jackson) to the appropriate Java object.
	 * @return boolean. Returns true if the operation succeeded.
	 * @throws Exception if the value is not recognized or is not compatible with this.value.
	 */
	public boolean testInternal(Object value) throws Exception {
		
		if (value == null) { // the object requested is not in the bid request.
			if (notPresentOk)
				return true;
			return false;
		}
		Number nvalue = null;
		String svalue = null;
		Set qvalue = null;
		
		if (value instanceof String)
			svalue = (String)value;
		if (value instanceof IntNode) {
			IntNode n = (IntNode) value;
			nvalue = n.getNumberValue();	
		}
		else
		if (value instanceof TextNode) {	
			TextNode tn = (TextNode)value;
			svalue = tn.getTextValue();
		}
		else
		if (value instanceof ArrayNode) {
			List list = traverse((ArrayNode)value);
			qvalue = new TreeSet(list);
		}
		else
		if (value instanceof ObjectNode) {
			ObjectNode n = (ObjectNode) value;
			mvalue = iterateObject(n);
		}
		else 
		if (value instanceof Double) {
			DoubleNode n =  new DoubleNode((Double)value); //(Node) value;
			nvalue = n.getNumberValue();
		}
		else
		if (value instanceof Integer) {
			IntNode n =  new IntNode((Integer)value); //(Node) value;
			nvalue = n.getNumberValue();
		}

		switch(operator) {
		case QUERY:
			return true;
			
		case EQUALS:
			return processEquals(ival,nvalue,sval,svalue,qval,qvalue);		
		case NOT_EQUALS:
			return !processEquals(ival,nvalue,sval,svalue,qval,qvalue);
			
		case MEMBER:
			if (qvalue == null)
				qvalue = new TreeSet(lval);
			if (ival == null && svalue == null && this.value instanceof String) {
				svalue = (String)this.value;
			}
				
			return processMember(ival,svalue,qvalue);	
		case NOT_MEMBER:
			if (qvalue == null)
				qvalue = new TreeSet(lval);
			if (ival == null && svalue == null && this.value instanceof String) {
				svalue = (String)this.value;
			}
			return !processMember(ival,svalue,qvalue);
			
		case INTERSECTS:
		case NOT_INTERSECTS:
			if (qvalue == null)
				qvalue = new TreeSet(lval);
			if (qval == null) {
				qval = new TreeSet();
				if (svalue != null) {;
					qval.add(svalue);
				} else {
					if (nvalue != null) {
						qval.add(nvalue);
					}
				}
			} else {
				if (svalue != null) {;
					qval.add(svalue);
			} else {
				if (nvalue != null) {
					qval.add(nvalue);
				}
			}
			}
			if(operator == INTERSECTS)
				return processIntersects(qval,qvalue);
			return !processIntersects(qval,qvalue);
			
		case INRANGE:
			return computeInRange(mvalue,lval);
		case NOT_INRANGE:
			return !computeInRange(mvalue,lval);
			
		case DOMAIN:
			return computeInDomain(nvalue,lval);
		case NOT_DOMAIN:
			return !computeInDomain(nvalue,lval);
			
		case LESS_THAN:
		case LESS_THAN_EQUALS:
		case GREATER_THAN:
		case GREATER_THAN_EQUALS:
			return processLTE(operator,ival,nvalue,sval,svalue,qval,qvalue);		
					
		default:
			return false;
			//throw new Exception("Undefined operation attempted");
		}
	}
	
	/**
	 * Processes the relational operators.
	 * @param operator. int - less than, less than equal, etc...
	 * @param ival. Number - The constant's value if a number.
	 * @param nvalue. Number - The bid request's value if a number,
	 * @param sval. String - the constant's value if a String.
	 * @param svalue. String - the bid request value if a String.
	 * @param qval. Set - constant's value if it is a Set.
	 * @param qvalue. Set - the bid requests value if it is a Set.
	 * @return boolean. Returns the value of the operation (true or false).
	 */
	public boolean processLTE(int operator,Number ival, Number nvalue, String sval, String svalue, Set qval, Set qvalue) {
		if (ival == null || nvalue == null)
			return false;
		switch(operator) {
		case LESS_THAN:
			return ival.doubleValue() < nvalue.doubleValue();
		case LESS_THAN_EQUALS:
			return ival.doubleValue() <= nvalue.doubleValue();
		case GREATER_THAN:	
			return ival.doubleValue() > nvalue.doubleValue();
		case GREATER_THAN_EQUALS:
			return ival.doubleValue() >= nvalue.doubleValue();
		} 
		return false;
	}
	
	/**
	 * Determine if the value of this node object equals that of what is found in the
	 * bid request object.
	 * @param ival. Number - The constant's value if a number.
	 * @param nvalue. Number - The bid request's value if a number,
	 * @param sval. String - the constant's value if a String.
	 * @param svalue. String - the bid request value if a String.
	 * @param qval. Set - constant's value if it is a Set.
	 * @param qvalue. Set - the bid requests value if it is a Set.
	 * @return boolean. Returns true if operation is true.
	 */
	public boolean processEquals(Number ival, Number nvalue, String sval, String svalue, Set qval, Set qvalue) {
		if (ival != null) {
			double a = ival.doubleValue();
			double b = nvalue.doubleValue();
			return a==b;
		} else
		if (sval != null) {
			return sval.equals(svalue);
		} else 
		if (qval != null) {
			if (qval.size() != qvalue.size())
				return false;
			return qval.containsAll(qvalue);
		}
		return false;
	}
	
	/**
	 * Compute range in meters from qval (set, lat, lon, meters) against a set of
	 * @param pos. Map - A  map of the geo object in the bid request; containing keys "lat","lon","type".
	 * @param qvalue. List A list of maps defining "lat", "lon","range" for testing against multiple regions. This is the constant value.
	 * @return boolean. Returns true if any of the qvalue regions is in range of pos.
	 */
	public boolean computeInRange(Map<String,Double> pos, List<Map> qvalue) {		
		for (int i=0;i<qvalue.size();i++) {
			Map<String,Double> xy = (Map<String,Double>)qvalue.get(i);
			double xlat = xy.get("lat");
			double xlon = xy.get("lon");
			
			double limit = 0;
			Object obj = xy.get("range");
			if (obj instanceof Integer) {
				Integer x = (Integer)obj;
				limit = (double)x;
			} else {
				Double d = (Double)obj;
				limit = d;
			}
				
			double range = getRange(xlat,xlon,pos.get("lat"),pos.get("lon"));
			
			if (range < limit)
				return true;
		}
		return false;
	}
	
	/**
	 * Compute distance in meters between xlat,xlon and ylat,ylon
	 * @param xlat - First point's latitude
	 * @param xlon - First point's longitude
	 * @param ylat - Second point's latitude
	 * @param ylon - Second point's longitude
	 * @return double. Distance in meters between these 2 points.
	 */
	public static double  getRange(Number xlat, Number xlon, Number ylat, Number ylon) {
			double lat1 = xlat.doubleValue();
			double long1 = xlon.doubleValue();
			
			double lat2 = ylat.doubleValue();
			double long2 = ylon.doubleValue();
			
	        double dlat1=lat1*(Math.PI/180);

	        double dlong1=long1*(Math.PI/180);
	        double dlat2=lat2*(Math.PI/180);
	        double dlong2=long2*(Math.PI/180);

	        double dLong=dlong1-dlong2;
	        double dLat=dlat1-dlat2;

	        double aHarv= Math.pow(Math.sin(dLat/2.0),2.0)+Math.cos(dlat1)*Math.cos(dlat2)*Math.pow(Math.sin(dLong/2),2);
	        double cHarv=2*Math.atan2(Math.sqrt(aHarv),Math.sqrt(1.0-aHarv));
	        //earth's radius from wikipedia varies between 6,356.750 km � 6,378.135 km (�3,949.901 � 3,963.189 miles)
	        //The IUGG value for the equatorial radius of the Earth is 6378.137 km (3963.19 mile)
	        double earth=6378.137*1000; // meters
	        double distance=earth*cHarv;
			
	        return distance;
	}
	
	/**
	 * Determine of the value of this node is in the domain of the other node.
	 * @param ival Number. The value to be tested.
	 * @param qvalue List. The low and high values to test
	 * @return boolean. Returns true of value is in the domain of qvalue, else false;
	 * @throws Exception if the values being compared are not compatible or not a recognized type.
	 */
	public boolean computeInDomain(Number ival, List qvalue) throws Exception {
		if (qvalue.size() != 2) 
			throw new Exception("Domain computation requires a low and high range (2 value)");
		double value = ival.doubleValue();

		double low = (Double)qvalue.get(0);
		double high = (Double)qvalue.get(1);
		if (value >= low && value <= high)
			return true;
		return false;
	}
	
	
	/**
	 * Process membership of scalar value in the list provided in the bid request.
	 * @param ival. Number - the constant's value if a number.
	 * @param sval. String - the constan't value if a string.
	 * @param qvalue. Set - the bid request values.
	 * @return boolean. Returns true of ival/sval in qvalue.
	 */
	boolean processMember(Number ival, String sval,  Set qvalue) {
		try {
		boolean ok = false;
		if (ival != null) {
			ok = qvalue.contains(ival);
		}
		if (sval != null) {
			ok = qvalue.contains(sval);
		}
		return ok;
		} catch(Exception e) {
			
		}
		return false;
	}
	
	/**
	 * Process the intersection of the node value and that of the value in the
	 * bid request.
	 * @param qval. Set - the set of things from the constant object.
	 * @param qvalue. Set - the set of things from the bid request.
	 * @return boolean. Returns true if there is an intersection.
	 */
	boolean processIntersects(Set qval, Set qvalue) {
		qval.retainAll(qvalue);
		return !(qval.size() == 0);
	}
	
	/**
	 * Iterate over a Jackson object and create a Java Map.
	 * @param node. ObjectNode. The Jackson node to set up as a Map.
	 * @return Map. Returns the Map implementation of the Jackson node.
	 */
	Map iterateObject(ObjectNode node) {
		Map m = new HashMap();
		Iterator it = node.iterator();
		it = node.getFieldNames();
		while(it.hasNext()) {
			String key = (String)it.next();
			Object s = node.get(key);
			if (s instanceof TextNode) {
				TextNode t = (TextNode)s;
				m.put(key,t.getValueAsText());
			} else
			if (s instanceof DoubleNode) {
				DoubleNode t= (DoubleNode)s;
				m.put(key, t.getNumberValue());
			} else
			if (s instanceof IntNode) {
				IntNode t = (IntNode)s;
				m.put(key,t.getNumberValue());
			} else
				m.put(key,s);  // indeterminate, need to traverse       
		}
		return m;
	}
	/**
	 * Traverse an ArrayNode and convert to ArrayList
	 * @param n. A Jackson ArrayNode.
	 * @return List. The list that corresponds to the Jackson ArrayNode.
	 */
	List traverse (ArrayNode n) {
		List list = new ArrayList();
		
		for (int i=0;i<n.size();i++) {
			Object obj = n.get(i);
			if (obj instanceof IntNode) {
				IntNode d = (IntNode)obj;
				list.add(d.getNumberValue());
			} else
			if (obj instanceof DoubleNode) {
				DoubleNode d = (DoubleNode)obj;
				list.add(d.getNumberValue());
			}
			else {
				TextNode t = (TextNode)obj;
				list.add(t.getTextValue());
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the value of the interrogate of the bid request.
	 * @return Object. The value of the bid request derived from the query of the hierarchy.
	 */
	public Object getBRvalue() {
		return brValue;
	}
}
