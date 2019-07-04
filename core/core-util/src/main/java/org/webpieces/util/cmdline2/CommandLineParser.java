package org.webpieces.util.cmdline2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple converter from -key=value -key2='value2 something' to a Map<String, String>
 * 
 * @author dhiller
 *
 */
public class CommandLineParser {

	public Arguments parse(String[] args) {
		Map<String, ValueHolder> arguments = new HashMap<>();
		List<Throwable> errors = new ArrayList<Throwable>();
		for(String arg: args) {
			String[] split = arg.split("=");
			if(!split[0].startsWith("-")) {
				errors.add(new IllegalArgumentException("Argument "+arg+" has a key that does not start with - which is required"));
				continue; //do next key so we can aggregate ALL errors first
			} else if(split.length == 1) {
				String key = split[0].substring(1);
				arguments.put(key, new ValueHolder(null));
				continue;
			} else if(split.length != 2) {
				errors.add(new IllegalArgumentException("Argument "+arg+" has bad syntax.  It either has no = in it or has too many"));
				continue; //do next key so we can aggregate ALL errors first
			}
			
			String key = split[0].substring(1);
			arguments.put(key, new ValueHolder(split[1]));
		}
		return new ArgumentsImpl(arguments, errors);
	}
	
}
