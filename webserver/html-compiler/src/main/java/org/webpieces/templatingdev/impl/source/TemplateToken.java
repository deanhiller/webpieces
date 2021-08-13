package org.webpieces.templatingdev.impl.source;

public enum TemplateToken {

	//TODO: http2 record the script to pre-emptively send by calling into the groovy
	//superclass recording all these scripts to send
	//THEN, after script is run, client can call getScriptsToPreemptivelySend and send
	//all those scripts before the browser client asks for them(in http2 at least)
	//BUT we must also RECORD all these on that connection and not send them a 
	//second time which would be a waste of our CPU
	
    EOF(null, null),            //end of file
    PLAIN(null, null),          //normal text
    SCRIPT("%{", "}%"),         // %{...}%
    FILE_VERIFY("%[", "]%"),    // %[...]% verify the file exists so we don't deploy with missing files
    EXPR("${", "}$"),           // ${...}$
    START_TAG("#{", "}#"),      // #{...}#
    END_TAG("#{/", "}#"),       // #{/...}#
    START_END_TAG("#{", "/}#"), // #{.../}#
    MESSAGE("&{", "}&"),        // &{...}&
    ACTION("@[", "]@"),         // @[...]@
    ABSOLUTE_ACTION("@@[", "]@@"), // @@[...]@@
    COMMENT("*{", "}*"),        // *{...}*
	ESCAPE("*[", "]*");         // *[...]* escapes all platform tokens except ]* which does not need escaping AND escapes all html as well;
	
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