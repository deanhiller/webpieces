package org.webpieces.util.cmdline;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple converter from -key=value -key2='value2 something' to a Map<String, String>
 * 
 * @author dhiller
 *
 */
public class CommandLineParser {

	public Map<String, String> parse(String[] args) {
		Map<String, String> arguments = new HashMap<>();
		for(String arg: args) {
			String[] split = arg.split("=");
			if(split.length != 2)
				continue; //old parser can't cope with this
			else if(!split[0].startsWith("-"))
				continue; //let the new parser handle this
			
			String key = split[0].substring(1);
			arguments.put(key, split[1]);
		}
		return arguments;
	}
	
    public int parseInt(String name, String value) {
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Could not convert command line param="+name+" from value="+value+" to an integer");
        }
    }
}
