package org.webpieces.templating.impl.source;

import java.util.ArrayList;
import java.util.List;

public class TokenizeHelper {

	private String pageSource;
	private ScriptToken state = ScriptToken.PLAIN;
	private int end = 0;
	private int begin = 0;
	private int beginLineNumber = 0;
	private List<Integer> newLineMarks = new ArrayList<>();
	private List<Token> tokens = new ArrayList<>();
	
	public TokenizeHelper(String source) {
		if(source.contains("\r"))
			throw new IllegalArgumentException("We rely on source input never containing \\r and only containing \\n for newlines");
		this.pageSource = source;
	}

	public List<Token> parseSource() {
		int lineNumber = 1;
		int left = pageSource.length() - end;
        while(left != 0) {
            char c = pageSource.charAt(end);
            char c1 = left > 1 ? pageSource.charAt(end+1) : 0;
            char c2 = left > 2 ? pageSource.charAt(end + 2) : 0;
            
            if(c == '\n') {
            	newLineMarks.add(end);
            	lineNumber++;
            }
            
            //advance one character for next time...
            end++;
            
            switch (state) {
                case PLAIN:
                    if (c == '%' && c1 == '{') {
                        found(ScriptToken.SCRIPT, 2, lineNumber);
                    } else if (c == '$' && c1 == '{') {
                        found(ScriptToken.EXPR, 2, lineNumber);
                    } else if (c == '#' && c1 == '{' && c2 == '/') {
                        found(ScriptToken.END_TAG, 3, lineNumber);
                    } else if (c == '#' && c1 == '{') {
                        found(ScriptToken.START_TAG, 2, lineNumber);
                    } else if (c == '&' && c1 == '{') {
                        found(ScriptToken.MESSAGE, 2, lineNumber);
                    } else if (c == '@' && c1 == '@' && c2 == '{') {
                        found(ScriptToken.ABSOLUTE_ACTION, 3, lineNumber);
                    } else if (c == '@' && c1 == '{') {
                        found(ScriptToken.ACTION, 2, lineNumber);
                    } else if (c == '*' && c1 == '{') {
                        found(ScriptToken.COMMENT, 2, lineNumber);
                    }
                    break;
                case SCRIPT:
                    if (c == '}' && c1 == '%') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    }
                    break;
                case COMMENT:
                    if (c == '}' && c1 == '*') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                        cleanupBeforeCommentWhitespace();
                    }
                    break;
                case START_TAG:
                    if (c == '}' && c == '#') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    } else if (c == '/' && c1 == '}' && c2 == '#') {
                        found(ScriptToken.END_TAG, 3, lineNumber);
                    }
                    break;
                case END_TAG:
                    if (c == '}' && c1 == '#') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    }
                    break;
                case EXPR:
                    if (c == '}' && c1 == '$') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    }
                    break;
                case ACTION:
                    if (c == '}' && c1 == '@') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    }
                    break;
                case ABSOLUTE_ACTION:
                    if (c == '}' && c1 == '@' && c2 == '@') {
                        found(ScriptToken.PLAIN, 3, lineNumber);
                    }
                    break;
                case MESSAGE:
                    if (c == '}' && c1 == '&') {
                        found(ScriptToken.PLAIN, 2, lineNumber);
                    }
                    break;
                case EOF:
                	throw new RuntimeException("Should not reach here");
            }
            
            left = pageSource.length() - end;
        }
        
        if(state != ScriptToken.PLAIN) {
        	Token token = tokens.get(tokens.size()-1);
        	int lastLine = token.endLineNumber;
        	throw new IllegalArgumentException("Line number="+lastLine+" has an unmatched token='"
        			+state.getStart()+"' where after the source is missing the end tag='"+state.getEnd()+"'");
        }
        
        end++;
        found(ScriptToken.EOF, 0, lineNumber);
        return tokens;
	}
	
	/** 
	 * Extra newline caused by simple comments are annoying when rendered so this strips them out.  This is not
	 * run in production anyways as we compile the resulting template file for production use.
	 * 
	 * We only strip whitespace before comments not after, so always ensure \n follows the end of the comment
	 * or implement something in this class to rework those two Tokens as well.
	 */
    private void cleanupBeforeCommentWhitespace() {
    	Token comment = tokens.get(tokens.size()-1);
    	Token plain = tokens.get(tokens.size()-2);
    	
    	int beginMark = comment.begin;
    	int newLineMark = 0;
    	for(int i = newLineMarks.size()-1; i > 0; i--) {
    		newLineMark = newLineMarks.get(i);
    		if(newLineMark < beginMark) {
    			break;
    		}
    	}
    	String candidateWhitespace = pageSource.substring(newLineMark, beginMark-2);
    	if(!candidateWhitespace.trim().equals("")) {
    		return;
    	}
    	
    	//otherwise, let's change some things
    	comment.begin = newLineMark;
    	plain.end = newLineMark;
    	plain.endLineNumber--;
    	comment.beginLineNumber++;
	}

	private void found(ScriptToken newState, int skip, int endLineNumber) {
        Token lastToken = new Token(begin, --end, state, beginLineNumber, endLineNumber, pageSource);
        begin = end += skip;
        beginLineNumber = endLineNumber;
        state = newState;
        tokens.add(lastToken);
    }

}
