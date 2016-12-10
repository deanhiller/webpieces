package org.webpieces.templating.impl.source;

public enum TemplateToken {

    EOF(null, null),            //end of file
    PLAIN(null, null),          //normal text
    SCRIPT("%{", "}%"),         // %{...}%
    EXPR("${", "}$"),           // ${...}$
    START_TAG("#{", "}#"),      // #{...}#
    END_TAG("#{/", "}#"),       // #{/...}#
    START_END_TAG("#{", "/}#"), // #{.../}#
    MESSAGE("&{", "}&"),        // &{...}&
    ACTION("@[", "]@"),         // @[...]@
    ABSOLUTE_ACTION("@@[", "]@@"), // @@[...]@@
    COMMENT("*{", "}*");          // *{...}*
    
    private String start;
	private String end;

	TemplateToken(String start, String end) {
    	this.start = start;
    	this.end = end;
    }

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}

	public boolean matchesStart(char c, char c1, char c2) {
		if(start.length() == 2 && c == start.charAt(0) && c1 == start.charAt(1))
			return true;
		else if(start.length() == 3 && c == start.charAt(0) && c1 == start.charAt(1) && c2 == start.charAt(2))
			return true;
		return false;
	}

	public boolean matchesEnd(char c, char c1, char c2) {
		if(end.length() == 2 && c == end.charAt(0) && c1 == end.charAt(1))
			return true;
		else if(end.length() == 3 && c == end.charAt(0) && c1 == end.charAt(1) && c2 == end.charAt(2))
			return true;		
		return false;
	}
	
}