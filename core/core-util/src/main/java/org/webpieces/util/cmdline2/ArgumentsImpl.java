package org.webpieces.util.cmdline2;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class ArgumentsImpl implements Arguments {

	private boolean calledAlready = false;
	private Map<String, ValueHolder> arguments;
	private Set<String> notConsumed = new HashSet<String>(); 
	private List<Throwable> errors = new ArrayList<>();
	//key to list of consumers of that key
	private Map<String, List<UsageHelp>> keyToAskedFor = new HashMap<>();
	private AtomicBoolean isConsumedAllArguments = new AtomicBoolean(false);
	private Map<String, ValueHolder> mustMatchDefaults = new HashMap<>();

	public ArgumentsImpl(Map<String, ValueHolder> args, List<Throwable> errors) {
		this.errors = errors;
		this.arguments = args;
		notConsumed.addAll(args.keySet());
	}
	
	@Override
	public void checkConsumedCorrectly() {
		if(calledAlready)
			throw new IllegalStateException("You only need to call this once");
		calledAlready = true;
			
		isConsumedAllArguments.set(true);
	
		//now check for 'extra' args that were not defined by the program to force cleanup
		for(String key : notConsumed) {
			errors.add(new IllegalArgumentException("Key="+key+" was not defined anywhere in the program"));
		}
		
		if(errors.size() > 0) {
			String msg = "Errors converting command line arguments:\n";
			msg += "(Call CommandLineException.getErrors to get the stack trace of each failure)";
			for(Throwable error : errors) {
				msg += "\n"+error;
			}
			
			msg += "\n\n"+commandLineHelpMessage();
			
			throw new CommandLineException(msg, errors, keyToAskedFor);
		}
	}

	@Override
	public <T> Supplier<T> createOptionalArg(String argumentKey, String defaultValueString, String help,
			Function<String, T> converter) {
		notConsumed.remove(argumentKey);

		ValueHolder valueHolder = arguments.get(argumentKey);
		List<UsageHelp> list = keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		keyToAskedFor.put(argumentKey, list);

		ValueHolder holder = mustMatchDefaults.putIfAbsent(argumentKey, new ValueHolder(defaultValueString));
		if(holder != null && !holder.getValue().equals(defaultValueString)) {
			String default1 = holder.getValue();
			String default2 = defaultValueString;
			UsageHelp usage = createUsage(valueHolder, defaultValueString, help);
			list.add(usage);
			errors.add(new IllegalStateException("Bug, two people consuming key -"+argumentKey+" but both provide different defaults.  default1="+default1+" default2="+default2));
			return new SupplierImpl<T>(isConsumedAllArguments);
		}
		
		//test defaultValue conversion so we throw so whoever adds this new argument knows right away if
		//the default value is valid.
		T defaultVal;
		try {
			defaultVal = converter.apply(defaultValueString);
		} catch(Throwable e) {
			//make this lazy as well so we display lots of errors all at once so they can fix a few issues
			UsageHelp usage = createUsage(valueHolder, defaultValueString, help);
			list.add(usage);
			errors.add(new RuntimeException("Bug, The defaultValue conversion test failed.  key="+argumentKey+" value="+defaultValueString, e));
			return new SupplierImpl<T>(isConsumedAllArguments);
		}

		if(valueHolder == null) {
			//key does not exist at all on command line
			list.add(new UsageHelp(defaultValueString, help, false, false, null));
			return new SupplierImpl<T>(defaultVal, true, isConsumedAllArguments);
		} else if(valueHolder.getValue() == null) {
			//key exists but there is no value
			list.add(new UsageHelp(defaultValueString, help, false, true, null));
			errors.add(new IllegalArgumentException("key="+argumentKey+" was supplied with no value.  A value is required OR remove the key(it's optional)"));
			return new SupplierImpl<T>(isConsumedAllArguments);
		}

		list.add(new UsageHelp(defaultValueString, help, true, true, valueHolder.getValue()));
		T param = convert(argumentKey, converter, valueHolder.getValue());
		return new SupplierImpl<T>(param, true, isConsumedAllArguments);
	}

	private UsageHelp createUsage(ValueHolder valueHolder, String defaultValueString, String help) {
		if(valueHolder == null) {
			return new UsageHelp(defaultValueString, help, false, false, null);
		} else if(valueHolder.getValue() == null) {
			//key exists but there is no value
			return new UsageHelp(defaultValueString, help, false, true, null);
		}
		return new UsageHelp(defaultValueString, help, true, true, valueHolder.getValue());
	}

	private <T> T convert(String key, Function<String, T> converter, String value) {
		try { 
			return converter.apply(value);
		} catch(IllegalArgumentException e) {
			errors.add(new IllegalArgumentException("Invalid value for key="+key+": "+e.getMessage(), e));
			return null;
		} catch(Throwable e) {
			errors.add(new RuntimeException("Bug, Converter for key="+key+". Whoever created the converter really screwed up and did not throw IllegalArgumentException and instead ran into this bug.  They should fix it", e));
			return null;
		}
	}

	@Override
	public <T> Supplier<T> createRequiredArg(String argumentKey, String help, Function<String, T> converter) {
		notConsumed.remove(argumentKey);
		
		ValueHolder valueHolder = arguments.get(argumentKey);
		List<UsageHelp> list = keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		keyToAskedFor.put(argumentKey, list);

		
		if(valueHolder == null) {
			//key does not exist at all on command line
			list.add(new UsageHelp(help, false, false, null));
			errors.add(new IllegalArgumentException("Argument -"+argumentKey+" is required but was not supplied.  help='"+help+"'"));
			return new SupplierImpl<T>(isConsumedAllArguments);
		} else if(valueHolder.getValue() == null) {
			//key exists but there is no value
			list.add(new UsageHelp(help, true, false, null));
			errors.add(new IllegalArgumentException("key="+argumentKey+" was supplied with no value.  A value is required"));
			return new SupplierImpl<T>(isConsumedAllArguments);
		}
		
		list.add(new UsageHelp(help, true, true, valueHolder.getValue()));
		T param = convert(argumentKey, converter, valueHolder.getValue());
		return new SupplierImpl<T>(param, false, isConsumedAllArguments);
	}

	@Override
	public Supplier<Boolean> createDoesExistArg(String argumentKey, String help) {
		notConsumed.remove(argumentKey);
		
		ValueHolder valueHolder = arguments.get(argumentKey);
		List<UsageHelp> list = keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		keyToAskedFor.put(argumentKey, list);

		UsageHelp usage;
		if(valueHolder == null) {
			usage = new UsageHelp(help, false, false, null);
		} else if(valueHolder.getValue() == null) {
			usage = new UsageHelp(help, true, false, null);
		} else {
			usage = new UsageHelp(help, true, true, valueHolder.getValue());
		}

		list.add(usage);
		
		//if checking on existence, it is optional...
		return new SupplierImpl<Boolean>(usage.isCmdLineContainsKey(), true, isConsumedAllArguments);
	}

	@Override
	public String commandLineHelpMessage() {
		if(keyToAskedFor.size() == 0)
			throw new IllegalStateException("Either you have no clients calling Argument.consumeXXX or you have no arguments to your program(stop using this library maybe?)");

		String fullHelp = "Dynamically generated help(depends on which plugins you pull in):\n";
		for(Entry<String, List<UsageHelp>> entry : keyToAskedFor.entrySet()) {
			fullHelp += "\t-"+entry.getKey()+" following usages:\n";
			for(UsageHelp usage : entry.getValue()) {
				fullHelp += "\t\t";
				if(usage.isHasDefaultValue()) {
					fullHelp += "(optional, default: "+usage.getDefaultValue()+")";
				}
				fullHelp += usage.getHelp()+"\n";
				fullHelp += "\t\t\t\tValue Parsed:"+usage.getValueParsed()+" foundKey:"+usage.isCmdLineContainsKey()+" foundValue:"+usage.isCmdLineContainsValue()+"\n";
			}
		}
		return fullHelp;
	}

	@Override
	public Supplier<InetSocketAddress> createOptionalInetArg(String argumentKey, String defaultValue, String help) {
		return createOptionalArg(argumentKey, defaultValue, help, (s) -> convertInet(s));
	}

	private InetSocketAddress convertInet(String value) {
		if(value == null)
			return null;
		else if("".equals(value)) //if command line passes "http.port=", the value will be "" to turn off the port
			return null;
		
		int index = value.indexOf(":");
		if(index < 0)
			throw new IllegalArgumentException("Invalid format.  Format must be '{host}:{port}' or ':port'");
		String host = value.substring(0, index);
		String portStr = value.substring(index+1);
		try {
			int port = Integer.parseInt(portStr);
			
			if("".equals(host.trim()))
				return new InetSocketAddress(port);
			
			return new InetSocketAddress(host, port);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid format.  The port piece of '{host}:{port}' or ':port' must be an integer");
		}
	}

}
