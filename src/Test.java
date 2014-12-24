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

		Test t = new Test();
		String ss = t.test();
		System.out.println("Final @"+ss.hashCode());

	}
	
	public String test() {
		String a = "1 + 2";
		System.out.println("Start: @"+a.hashCode());
		return a;
	}

}
