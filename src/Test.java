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
	static Set<Number> numbers1 = new TreeSet();
	static Set<Number> numbers2 = new TreeSet();
	public static void main(String[] args) throws Exception {
		numbers1.add(1);
		numbers1.add(2);
		numbers1.add(3);
		
		numbers2.add(2);
		
		 numbers1.retainAll(numbers2);
		 System.out.println(numbers1); // "[2, 3]"
	}

}
