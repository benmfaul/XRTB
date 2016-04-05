package com.xrtb.common;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * A class that encapsulates a Nashhorn script engine.
 * @author Ben M. Faul
 *
 */
public class JJS {
	/** The script engine */
	static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	
	
	/**
	 * Standard constructor.
	 */
	public JJS() {

	}
	
	/**
	 * Constructor providing exec and init functions.
	 * @param init. String - the code defining the init() function.
	 * @param exec. String - the code defining the exec() function.
	 * @throws Exception if the evaluation throws an exception in the script engine.
	 */
	public JJS(String init, String exec) throws Exception {
		engine.eval(init);
		engine.eval(exec);
	}
	
	/**
	 * Call the init() function with an argument.
	 * @param in. Object. The input parameter.
	 * @return Object. The value of the return from the function.
	 * @throws Exception if the evaluation throws an exception in the script engine.
	 */
	public Object callInit(Object in) throws Exception {
		Object rc = engine.eval("init("+in+");");
		return rc;
	}
	
	/**
	 * Call the exec() function with an argument.
	 * @param in. Object. The input parameter.
	 * @return Object. The value of the return from the function.
	 * @throws Exception if the evaluation throws an exception in the script engine.
	 */
	public Object callExec(Object in) throws Exception{
		String str = ("exec("+in+");");
		System.out.println(str);
		Object rc = engine.eval("exec("+in+");");
		return rc;
	}
	
	/**
	 * Execute arbitrary string containing code.
	 * @param str, String - the code to execute.
	 * @return Object. The value (if any) of the return statement in the code (else null).
	 * @throws Exception if the evaluation throws an exception in the script engine.
	 */
	public Object exec(String str) throws Exception {
		Object rc = engine.eval(str);
		return rc;
	}
	
}
