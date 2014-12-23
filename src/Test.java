import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class Test {

	public static void main(String[] args) throws Exception {

		String test = "/rtb/win/nexage/5.0/42.378/-71.227/id123/35c22289-06e2-48e9-a0cd-94aeb79fab43/http://rtb4.tapinsystems.net/?{siteid}/http://localhost:8080/images/320x50.jpg?adid={adid}&#38;bidid={oid}";
		
		String [] c = test.split("http:");
		for (String s : c) {
			System.out.println(s);
		}
		String [] x = c[0].split("/");
		for (String s : x) {
			System.out.println(s);
		}
	}

}
