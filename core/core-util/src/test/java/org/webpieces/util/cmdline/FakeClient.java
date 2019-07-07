package org.webpieces.util.cmdline;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.cmdline2.CommandLineParser;

public class FakeClient {

	//These fields are spread throughout the server in modules/routes/plugin
	//during the construction phase but are not invoked until the running phase
	private Supplier<InetSocketAddress> key1;
	private Supplier<String> key2;
	private Supplier<Boolean> key3;
	private Supplier<Integer> key4a;
	private Supplier<Integer> key4b;
	private Supplier<String> key5a;
	private Supplier<String> key5b;
	private Supplier<Integer> key6a;
	private Supplier<Boolean> key6b;
	private Arguments parse;
	private Supplier<InetSocketAddress> key7;

	public void fakeMain(String[] args) {
		
		parse = new CommandLineParser().parse(args);

		//These lines are spread throughout Plugins/Modules/Routes so that as you add plugins/modules/routes
		//your command line dynamically changes
		
		//consume once optional
		key1 = parse.consumeOptional("key1", ":0", "This is key1", (s) -> convertInet(s));
		//consume once required
		key2 = parse.consumeRequired("key2", "This is key2", (s) -> s);
		//consume once check exist
		key3 = parse.consumeDoesExist("key3", "This is key3");
		
		//consume twice optional
		key4a = parse.consumeOptional("key4", "456", "This is key4", (s) -> convertInt(s));
		key4b = parse.consumeOptional("key4", "456", "This is key4 second one", (s) -> convertInt(s));
		
		//consume twice required
		key5a = parse.consumeRequired("key5", "This is key5", (s) -> s);
		key5b = parse.consumeRequired("key5", "This is key5 second one", (s) -> s);

		//consume optional and check boolean
		key6a = parse.consumeOptional("key6", "789", "This is key6", (s) -> convertInt(s));
		key6b = parse.consumeDoesExist("key6", "This is key6 check");
		
		//test for default null...
		key7 = parse.consumeOptional("key7", null, "This is key7", (s) -> convertInet(s));

	}
	
	public void checkArgs() {
		parse.checkConsumedCorrectly();
	}
	
	private InetSocketAddress convertInet(String value) {
		if(value == null)
			return null;
		else if("".equals(value))
			return null;
		
		int index = value.indexOf(":");
		if(index < 0)
			throw new IllegalArgumentException("Invalid format.  Format must be '{host}:{port}' or ':port'");
		String host = value.substring(0, index);
		String portStr = value.substring(index+1);
		try {
			int port = Integer.parseInt(portStr);
			return new InetSocketAddress(host, port);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid format.  The port piece of '{host}:{port}' or ':port' must be an integer");
		}
	}
	
	private Integer convertInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Must be an Integer and was not", e);
		}
	}


	public InetSocketAddress readKey1() {
		return key1.get();
	}


	public Integer readKey4a() {
		return key4a.get();
	}

	public InetSocketAddress readKey7() {
		return key7.get();
	}

	public boolean isKey3() {
		return key3.get();
	}
}
