package org.webpieces.util.cmdline2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
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

public class ArgumentsImpl implements ArgumentsCheck {

	private static final Logger log = LoggerFactory.getLogger(ArgumentsImpl.class);

	private InetConverter inetConverter = new InetConverter(); //hmm, I hate not doing DI here!!
	private boolean calledAlready = false;
	private Map<String, ValueHolder> arguments;
	private FetchValue valueFetcher;
	private JvmEnv environment;
	private Set<String> notConsumed = new HashSet<String>(); 
	private List<Throwable> errors = new ArrayList<>();

	private AtomicBoolean isConsumedAllArguments = new AtomicBoolean(false);

	private Variables cmdLineArgs;
	private Variables envArgs;

	private Map<String, String> mustMatchEnvVars = new HashMap<>();

	public ArgumentsImpl(
			Map<String, ValueHolder> args,
			List<Throwable> errors,
			JvmEnv environment,
			FetchValue valueFetcher
	) {
		this.errors = errors;
		this.arguments = args;
		this.valueFetcher = valueFetcher;
		cmdLineArgs = new Variables((name) -> arguments.get(name));
		envArgs = new Variables((name) -> {
			String val = environment.readEnvVar(name);
			if(val == null)
				return null;
			return new ValueHolder(val);
		});

		this.environment = environment;
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

			throw new CommandLineException(msg, errors, cmdLineArgs.keyToAskedFor, this.envArgs.keyToAskedFor);
		} else {
			log.info("Full list of variables for this server\n\n"+commandLineHelpMessage());
		}
	}

	@Override
	public Supplier<String> createOptionalArg(String argumentKey, String defaultValueString, String help) {
		return createOptionalArg(argumentKey, defaultValueString, help, (s) -> s);
	}

	@Override
	public <T> Supplier<T> createOptionalArg(String argumentKey, String defaultValueString, String help,
			Function<String, T> converter) {
		notConsumed.remove(argumentKey);

		return createOptionalVarImpl(argumentKey, defaultValueString, help, converter, cmdLineArgs);
	}

	public <T> Supplier<T> createOptionalVarImpl(
			String argumentKey, String defaultValueString, String help,
			Function<String, T> converter,
			Variables vars
	) {
		Map<String, List<UsageHelp>> keyToAskedFor = vars.keyToAskedFor;
		Map<String, ValueHolder<String>> mustMatchDefaults = vars.mustMatchDefaults;

		ValueHolder<String> valueHolder = vars.fetch(argumentKey);

		List<UsageHelp> list = keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		keyToAskedFor.put(argumentKey, list);

		ValueHolder<String> holder = mustMatchDefaults.putIfAbsent(argumentKey, new ValueHolder(defaultValueString));
		if(holder != null && !valuesEqual(holder, defaultValueString)) {
			String default1 = holder.getValue();
			String default2 = defaultValueString;
			UsageHelp usage = createUsage(valueHolder, defaultValueString, help);
			list.add(usage);
			//fail fast here as TestBasicStart.java will catch the issue
			throw new IllegalStateException("You have a bug, two people consuming key -"+argumentKey+" but both provide different defaults.  default1="+default1+" default2="+default2);
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
			//SPECIAL CASE, blow up now so programmer fixes his stuff as TestBasicDevStart will catch this bug.
			throw new IllegalArgumentException("Bug in your code.  You are trying to convert value='"
					+defaultValueString+"' found in key="
					+argumentKey+".  Fix your converter or your defaultValue that you passed in", e);
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

	private <T> boolean valuesEqual(ValueHolder<T> holder, T defaultValueString) {
		if(holder.getValue() == null) {
			if(defaultValueString == null)
				return true;
			return false;
		}

		return holder.getValue().equals(defaultValueString);
	}

	private UsageHelp createUsage(ValueHolder<String> valueHolder, String defaultValueString, String help) {
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
			errors.add(new IllegalArgumentException("Invalid value for key="+key+ " (value="+value+"): "+e.getMessage(), e));
			return null;
		} catch(Throwable e) {
			errors.add(new RuntimeException("Bug, Converter for key="+key+". Whoever created the converter really screwed up and did not throw IllegalArgumentException and instead ran into this bug.  They should fix it", e));
			return null;
		}
	}

	public Supplier<String> createOptionalEnvVar(String envVarName, String defaultValue, String help) {
		return createOptionalEnvVar(envVarName, defaultValue, help, (s) -> s);
	}

	public <T> Supplier<T> createOptionalEnvVar(String envVarName, String defaultValue, String help, Function<String, T> converter) {
		return createOptionalVarImpl(envVarName, defaultValue, help, converter, this.envArgs);
	}


	@Override
	public Supplier<String> createRequiredArg(String argumentKey, String testDefault, String help) {
		return createRequiredArg(argumentKey, testDefault, help, (s) -> s);
	}

	@Override
	public <T> Supplier<T> createRequiredArg(String argumentKey, T testDefault, String help, Function<String, T> converter) {
		notConsumed.remove(argumentKey);
		return createRequiredVarImpl(argumentKey, testDefault, help, converter, this.cmdLineArgs);
	}

	@Override
	public Supplier<String> createRequiredEnvVar(String envVarName, String testDefault, String help) {
		return createRequiredEnvVar(envVarName, testDefault, help, (s) -> s);
	}

	@Override
	public <T> Supplier<T> createRequiredEnvVar(String envVarName, T testDefault, String help, Function<String, T> converter) {
		return createRequiredVarImpl(envVarName, testDefault, help, converter, this.envArgs);
	}

	private <T> Supplier<T> createRequiredVarImpl(String argumentKey, T testDefault, String help, Function<String, T> converter, Variables vars) {

		Map<String, List<UsageHelp>> keyToAskedFor = vars.keyToAskedFor;
		Map<String, ValueHolder<Object>> mustMatchDefaults = vars.mustMatchTestDefaults;

		ValueHolder<String> valueHolder = vars.fetch(argumentKey);

		List<UsageHelp> list = keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		keyToAskedFor.put(argumentKey, list);

		ValueHolder<T> holder = mustMatchDefaults.putIfAbsent(argumentKey, new ValueHolder(testDefault));
		if(holder != null && !valuesEqual(holder, testDefault)) {
			T default1 = holder.getValue();
			T default2 = testDefault;
			UsageHelp usage = createUsage(valueHolder, testDefault+"", help);
			list.add(usage);
			//fail fast here as TestBasicStart.java will catch the issue
			throw new IllegalStateException("Bug, two people consuming key -"+argumentKey+" but both provide different defaults.  default1="+default1+" default2="+default2);
		}

		if(valueHolder == null) {
			//key does not exist at all on (cmd line or environment)
			return createSupplier(argumentKey, testDefault, help, list);
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

	private <T> Supplier<T> createSupplier(String argumentKey, T testDefault, String help, List<UsageHelp> list) {
		return valueFetcher.fetchFinalValue(testDefault,
				() -> reqArgMissing(argumentKey, help, list),
				() -> fillDefaultIn(testDefault, help, list)
		);
	}

	private <T> SupplierImpl<T> fillDefaultIn(T testDefault, String help, List<UsageHelp> list) {
		list.add(new UsageHelp(help, true, true, testDefault.toString()));
		return new SupplierImpl<>(testDefault, false, isConsumedAllArguments);
	}

	private <T> SupplierImpl<T> reqArgMissing(String argumentKey, String help, List<UsageHelp> list) {
		list.add(new UsageHelp(help, false, false, null));
		errors.add(new IllegalArgumentException("Argument -" + argumentKey + " is required but was not supplied.  help='" + help + "'"));
		return new SupplierImpl<T>(isConsumedAllArguments);
	}

	@Deprecated
	@Override
	public <T> Supplier<T> createRequiredArgOrEnvVar(String argumentKey, String envVarName, String help, Function<String, T> converter) {
		notConsumed.remove(argumentKey);

		ValueHolder<String> valueHolder = arguments.get(argumentKey);
		List<UsageHelp> list = cmdLineArgs.keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		cmdLineArgs.keyToAskedFor.put(argumentKey, list);

		if (valueHolder == null && environment.readEnvVar(envVarName) != null) {
			valueHolder = new ValueHolder(environment.readEnvVar(envVarName));

			String previousValue = mustMatchEnvVars.putIfAbsent(argumentKey, envVarName);
			if(previousValue != null && !previousValue.equals(envVarName)) {
				UsageHelp usage = createUsage(valueHolder, envVarName, help);
				list.add(usage);
				errors.add(new IllegalStateException("Bug, two people consuming key -"+argumentKey+" but both provide different env vars.  envVar1="+previousValue+" envVar2="+envVarName));
				return new SupplierImpl<T>(isConsumedAllArguments);
			}
		}

		if(valueHolder == null) {
			//key does not exist at all on command line
			list.add(new UsageHelp(help, false, false, null));
			errors.add(new IllegalArgumentException("Argument -"+argumentKey+" or env var "+envVarName+" is required but was not supplied.  help='"+help+"'"));
			return new SupplierImpl<T>(isConsumedAllArguments);
		} else if(valueHolder.getValue() == null) {
			//key exists but there is no value
			list.add(new UsageHelp(help, true, false, null));
			errors.add(new IllegalArgumentException("key="+argumentKey+" and env var "+envVarName+" was supplied with no value.  A value is required"));
			return new SupplierImpl<T>(isConsumedAllArguments);
		}

		list.add(new UsageHelp(help, true, true, valueHolder.getValue()));
		T param = convert(argumentKey, converter, valueHolder.getValue());
		return new SupplierImpl<T>(param, false, isConsumedAllArguments);
	}

	@Override
	public Supplier<Boolean> createDoesExistArg(String argumentKey, String help) {
		notConsumed.remove(argumentKey);
		
		ValueHolder<String> valueHolder = arguments.get(argumentKey);
		List<UsageHelp> list = cmdLineArgs.keyToAskedFor.getOrDefault(argumentKey, new ArrayList<UsageHelp>());
		cmdLineArgs.keyToAskedFor.put(argumentKey, list);

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
		if(cmdLineArgs.keyToAskedFor.size() == 0 && envArgs.keyToAskedFor.size() == 0)
			throw new IllegalStateException("Either you have no clients calling Arguments.createXXX or you have no arguments to your program(stop using this library maybe?)");

		String fullHelp = "Dynamically generated help(depends on which plugins you pull in):\n";
		fullHelp += "CMD LINE ARG HELP FIRST------------------------------------------\n";
		fullHelp += printEachVar(this.cmdLineArgs.keyToAskedFor, true);
		fullHelp += "ENV VARS HELP------------------------------------------\n";
		fullHelp += printEachVar(this.envArgs.keyToAskedFor, false);
		fullHelp += "END---------------------------------------------------\n";
		return fullHelp;
	}

	private String printEachVar(Map<String, List<UsageHelp>> vars, boolean isCmdLine) {
		String fullHelp = "";
		for(Entry<String, List<UsageHelp>> entry : vars.entrySet()) {
			fullHelp += "\t";
			if(isCmdLine) {
				fullHelp += "-";
			}

			fullHelp += entry.getKey() + " ";
			UsageHelp usageHelp = entry.getValue().get(0);
			if(usageHelp.isHasDefaultValue()) {
				fullHelp += "(optional, default: "+usageHelp.getDefaultValue()+")";
			}
			fullHelp+=":\n";

			int counter = 1;
			for(UsageHelp usage : entry.getValue()) {
				fullHelp += "\t\tUsage #"+counter+":"+usage.getHelp()+"\n";
				//We can't print security tokens in the logs
				//Value Parsed:"+usage.getValueParsed()+
				fullHelp += "\t\t\t\tFoundKey:"+usage.isCmdLineContainsKey()+" foundValue:"+usage.isCmdLineContainsValue()+"\n";
				counter++;
			}
		}
		return fullHelp;
	}

	@Override
	public Supplier<InetSocketAddress> createOptionalInetArg(String argumentKey, String defaultValue, String help) {
		return createOptionalArg(argumentKey, defaultValue, help, (s) -> inetConverter.convertInet(s));
	}



}
