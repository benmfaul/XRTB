import com.xrtb.common.*;
import com.xrtb.commands.*;
import com.xrtb.pojo.*;

config = Configuration.getInstance();
config.instanceName = "config.java";

config.exchanges.add("mobclix");
config.seats.put("mobclix","mobclixSeat");

shell = new JJS();

Campaign camp = new Campaign("test");
camp.add(new Node("AT","at",Node.EQUALS,new Integer(2)));
camp.add(new Node("W","imp.0.w",Node.EQUALS,new Integer(320)));
camp.add(new Node("H","imp.w.h",Node.EQUALS,new Integer(50)));

config.campaigns.add(camp);


