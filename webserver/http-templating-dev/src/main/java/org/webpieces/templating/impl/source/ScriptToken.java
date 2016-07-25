package org.webpieces.templating.impl.source;

public enum ScriptToken {

    EOF(null, null),            //end of file
    PLAIN(null, null),          //normal text
    SCRIPT("%{", "}%"),         // %{...}%
    EXPR("${", "}$"),           // ${...}$
    START_TAG("#{", "}#"),      // #{...}#
    END_TAG("#{/", "}#"),       // #{/...}#
    START_END_TAG("#{", "/}#"), // #{.../}#
    MESSAGE("&{", "}&"),        // &{...}&
    ACTION("@{", "}@"),         // @{...}@
    ABSOLUTE_ACTION("@@{", "}@@"), // @@{...}@@
    COMMENT("*{", "}*");        // *{...}*
    
    private String start;
	private String end;

	private ScriptToken(String start, String end) {
    	this.start = start;
    	this.end = end;
    }

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}
	
}