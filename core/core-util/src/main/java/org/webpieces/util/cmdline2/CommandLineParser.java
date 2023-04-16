package org.webpieces.util.cmdline2;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced converter allowing startup to define params so when you add GuiceModules, params are defined and THEN allowing 
 * consumption of those params in a second stage
 * 
 * @author dhiller
 *
 */
public class CommandLineParser {

	private FetchValue fetchValue;
	private JvmEnv environment;

	@Inject
	public CommandLineParser(JvmEnv environment, FetchValue fetchValue) {
		this.environment = environment;
		this.fetchValue = fetchValue;
	}

	@Deprecated
	public CommandLineParser() {
		this.environment = new JvmEnv();
		this.fetchValue = new FetchValue();
	}

	/**
	 * @param args - cmdline arguments
	 * @return
	 */
	public ArgumentsCheck parse(String ... args) {
		Map<String, ValueHolder> arguments = new HashMap<>();
		List<Throwable> errors = new ArrayList<Throwable>();
		for(String arg: args) {
			if(!arg.startsWith("-")) {
				errors.add(new IllegalArgumentException("Argument '"+arg+"' has a key that does not start with - which is required"));
				continue; //do next key so we can aggregate ALL errors first
			}

			//strip the - from the argument ( '-key=asd' becomes 'key=asd' or '-single' become 'single' or '-emp=' becomes 'emp=')
			arg = arg.substring(1);
			int index = arg.indexOf("=");
			if(index < 0) {
				arguments.put(arg, new ValueHolder(null));
				continue;
			}

			String key = arg.substring(0, index);
			//works for 'key=asd'(resulting in "asd") and for 'key='(resulting in "")
			String value = arg.substring(index+1);
			arguments.put(key, new ValueHolder(value));
		}
		return new ArgumentsImpl(arguments, errors, environment);
	}
	
}
