package org.webpieces.templating.impl;

public class ParsingState {

	public ScriptToken state = ScriptToken.PLAIN;
	public int end = 0;
	public int begin;
	public int beginLineNumber;

}
