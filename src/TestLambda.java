import java.util.HashMap;
import java.util.concurrent.Callable;


public class TestLambda {

	public static void main(String []args) throws Exception {
		HashMap<Integer, Callable<String>> opcode_only = new HashMap<Integer, Callable<String>>();
		opcode_only.put(0, () -> { return "none"; });
		opcode_only.put(1, () -> { return "ONE!...."; });
		System.out.println(opcode_only.get(0).call());
	}
}
