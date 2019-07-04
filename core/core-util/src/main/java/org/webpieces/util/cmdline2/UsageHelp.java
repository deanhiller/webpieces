package org.webpieces.util.cmdline2;

public class UsageHelp {

	private final String defaultValue;
	private final String help;
	private final boolean cmdLineContainsKey;
	private final boolean cmdLineContainsValue;
	private final boolean hasDefaultValue;
	private final String valueParsed;

	//optional
	public UsageHelp(String defaultValue, String help, boolean cmdLineContainsKey, boolean cmdLineContainsValue, String valueParsed) {
		this.valueParsed = valueParsed;
		this.hasDefaultValue = true;
		this.defaultValue = defaultValue;
		this.help = help;
		this.cmdLineContainsKey = cmdLineContainsKey;
		this.cmdLineContainsValue = cmdLineContainsValue;
	}

	//required
	public UsageHelp(String help, boolean cmdLineContainsKey, boolean cmdLineContainsValue, String valueParsed) {
		this.valueParsed = valueParsed;
		this.hasDefaultValue = false;
		this.defaultValue = null;
		this.help = help;
		this.cmdLineContainsKey = cmdLineContainsKey;
		this.cmdLineContainsValue = cmdLineContainsValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getHelp() {
		return help;
	}

	public boolean isCmdLineContainsKey() {
		return cmdLineContainsKey;
	}

	public boolean isCmdLineContainsValue() {
		return cmdLineContainsValue;
	}

	public boolean isHasDefaultValue() {
		return hasDefaultValue;
	}

	public String getValueParsed() {
		return valueParsed;
	}
	
}
