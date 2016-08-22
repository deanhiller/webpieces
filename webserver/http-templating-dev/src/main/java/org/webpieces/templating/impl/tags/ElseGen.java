package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.AbstractTag;
import org.webpieces.templating.api.CompileCallback;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public class ElseGen extends AbstractTag {

	@Override
	public String getName() {
		return "else";
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback) {
		sourceCode.println(" else {", token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.println("}", token);
		sourceCode.println();
	}

	/*
	 * A few use cases
	 * (pass)case 1: text\n   #{/if}#\n   #{else}#\n text (typical multi-line case)
	 * (pass)case 2: text#{/if}##{else}#text (typical one liner case)
	 * (pass)case 3: text\n   #{/if}##{else}#\n text (typical case for if/else on same line
	 * 
	 * This case naturally fails as there is text between the if and the else
	 * (fail)case 5: text #{/if}# text  #{else}#\ntext
	 * 
	 * This case fails due to whitespace being between if and the else because I am disciplined enough to want better errors than a
	 * groovy compiler errors for users but lazy to do what would be quite a bit of work for not much benefit at this time..
	 * (fail)case 4: text\n   #{/if}#  #{else}#\n text
	 * 
	 * 
	 * These next two are due to the parsing taking any tokens on it's own line and stripping it down to just the token with no 
	 * whitespace to the left and right so essentialy, all if..else becomes a one liner #{/if}#{else}# on the same line
	 * 
	 * This case fails because #{/if}# is not on it's own line..
	 * (fail)case 6: text #{/if}#\n   #{else}#\ntext
	 * 
	 * This case fails because #{else}# is not on it's own line...
	 * (fail)case 7: text\n  #{/if}#\n   #{else}# text
	 */
	@Override
	public void validatePreviousSibling(Token current, Token prevous) {
		String name = getName();
		if(!prevous.isEndTag())
			throw new IllegalArgumentException(name+" tag is misused.  Either there is no previous #{/if}# tag OR\n"
					+ " it must be used like #{/if}##{else}# or #{/elseif}##{else}# with no\n"
					+ " spaces between the end if tag and begin else tag OR both #{/if}# and #{else}# must be on it's\n"
					+ " own lines with no text(only whitespace) or both #{/elseif}# and #{else}# must be on it's\n"
					+ " own lines with no text(only whitespace)\n"
					+ " #{"+name+"} is in error. "+current.getSourceLocation(true));
		
		String previousTagName = prevous.getCleanValue();
		if(!"if".equals(previousTagName) && !"elseif".equals(previousTagName) && !"list".equals(previousTagName)) {
			throw new IllegalArgumentException(name+" tag is missing the previous #{/if}# or #{/elseif}# or #{/list}# tag.  Instead we "
					+ "found a #{/"+previousTagName+"}# tag before the else. "+current.getSourceLocation(true));
		}
	}
}
