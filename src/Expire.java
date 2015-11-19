import io.netty.util.concurrent.Future;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.redisson.core.RExpirable;


public class Expire {

	public static void main(String [] args) {
		
		String instance = "demo:ben-OptiPlex-780:8080";
		
		boolean where = instance.matches("xxx(.*)");
		System.out.println(where);
	}
}
