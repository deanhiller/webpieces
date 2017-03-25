package org.webpieces.templating.impl.source;

import static org.webpieces.templating.impl.source.TemplateToken.*;
import java.util.ArrayList;
import java.util.List;

/*
* for discarding extra lines, it is a bit tricky
* case 1: Hi there, my name is ${user.name}$ and my favorite color is ${color}$ (preserve all)
* case 2: Hi there, my name is ${user.name}$\n and this is my story (preserve all)
* case 3: #{if}#\nShow this\n#{/if}\n  should rewrite as "Show this\n" so two \n are removed
* case 4: #{if}#Show this#{/if}# should preserve all as "Show this\n"
* case 5: (really breaks stuff) #{form ..}#\n.....#{/form}# In this case, we really don't want to strip anything since
*         the <form> will replace the first line perfectly unlike #{if}# AND if they tab/space of #{form}# element, we
*         want to maintain those spaces, tabs so it looks like the user wrote it when rendered
* 
* Anotherwords, to be perfect, we really need methods on the tags to tell if we should remove lines and whitespace or not
* 
* To get this correct, we will really need to write a few test cases for this
*/
public class TemplateTokenizerTask {

	private String pageSource;
	private TemplateToken state = TemplateToken.PLAIN;
	private int end = 0;
	private int begin = 0;
	private int beginLineNumber = 0;
	private List<Integer> newLineMarks = new ArrayList<>();
	private List<TokenImpl> tokens = new ArrayList<>();
	private String filePath;
	private int startTokenCount;

	public TemplateTokenizerTask(String filePath, String source) {
		this.filePath = filePath;
		if (source.contains("\r"))
			throw new IllegalArgumentException(
					"We rely on source input never containing \\r and only containing \\n for newlines");
		this.pageSource = source;
	}

	public List<TokenImpl> parseSource() {
		int lineNumber = 1;
		int left = pageSource.length() - end;
		while (left != 0) {
			lineNumber = processNext3Chars(lineNumber, left);

			left = pageSource.length() - end;
		}

		if (state != PLAIN) {
			TokenImpl token = tokens.get(tokens.size() - 1);
			int lastLine = token.endLineNumber;
			throw new IllegalArgumentException("File=" + filePath + " has an issue.  It is missing an end token of='"
					+ state.getEnd() + "'" + " where the start token was on line number=" + lastLine
					+ " and start token of the tag looks like='" + state.getStart() + "'"
					+ token.getSourceLocation(true));
		}

		end++;
		found(EOF, 0, lineNumber);
		return tokens;
	}

	private int processNext3Chars(int lineNumber, int left) {
		char c = pageSource.charAt(end);
		char c1 = left > 1 ? pageSource.charAt(end + 1) : 0;
		char c2 = left > 2 ? pageSource.charAt(end + 2) : 0;

		if (c == '\n') {
			newLineMarks.add(end);
			lineNumber++;
		}

		// advance one character for next time...
		end++;

		switch (state) {
		case PLAIN:
			processStartTagMatches(lineNumber, c, c1, c2);
			break;
		default:
			processEnd(lineNumber, c, c1, c2);
			break;
		}
		return lineNumber;
	}

	private void processEnd(int lineNumber, char c, char c1, char c2) {
		switch (state) {
		case PLAIN:
			throw new IllegalStateException("Should not be in state=PLAIN at this point ever");
		case SCRIPT:
			if (SCRIPT.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			}
			break;
		case COMMENT:
			if (COMMENT.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			}
			break;
		case START_TAG:
			if (START_TAG.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			} else if (START_END_TAG.matchesEnd(c, c1, c2)) {
				found(PLAIN, 3, lineNumber, true, false);
			}
			break;
		case END_TAG:
			if (END_TAG.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			}
			break;
		case EXPR:
			if (EXPR.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			}
			break;
		case ACTION:
			if (ACTION.matchesEnd(c, c1, c2)) {
				found(PLAIN, 2, lineNumber);
			}
			break;
		case ABSOLUTE_ACTION:
			if (ABSOLUTE_ACTION.matchesEnd(c, c1, c2)) {
				found(PLAIN, 3, lineNumber);
			}
			break;
		case MESSAGE:
			if (c == '&' && c1 == '{') {
				startTokenCount++; // For nested i18n tags
			} else if (MESSAGE.matchesEnd(c, c1, c2)) {
				if (startTokenCount == 0) {
					found(PLAIN, 2, lineNumber);
				} else
					startTokenCount--;
			}
			break;
		case EOF:
		case START_END_TAG:
			throw new RuntimeException("Should not reach here");
		}
	}

	private void processStartTagMatches(int lineNumber, char c, char c1, char c2) {
		if (SCRIPT.matchesStart(c, c1, c2)) {
			found(SCRIPT, 2, lineNumber);
		} else if (EXPR.matchesStart(c, c1, c2)) {
			found(EXPR, 2, lineNumber);
		} else if (END_TAG.matchesStart(c, c1, c2)) {
			found(END_TAG, 3, lineNumber);
		} else if (START_TAG.matchesStart(c, c1, c2)) {
			found(START_TAG, 2, lineNumber);
		} else if (MESSAGE.matchesStart(c, c1, c2)) {
			found(MESSAGE, 2, lineNumber);
		} else if (ABSOLUTE_ACTION.matchesStart(c, c1, c2)) {
			found(ABSOLUTE_ACTION, 3, lineNumber);
		} else if (ACTION.matchesStart(c, c1, c2)) {
			found(ACTION, 2, lineNumber);
		} else if (COMMENT.matchesStart(c, c1, c2)) {
			found(COMMENT, 2, lineNumber);
		} else if (c == '\n') {
			// We do this so any plain tokens that are all whitespace can be
			// discarded...
			found(PLAIN, 1, lineNumber, false, true);
		}
	}

	// /**
	// * Extra newline caused by simple comments are annoying when rendered so
	// this strips them out. This is not
	// * run in production anyways as we compile the resulting template file for
	// production use.
	// *
	// * We only strip whitespace before comments not after, so always ensure \n
	// follows the end of the comment
	// * or implement something in this class to rework those two Tokens as
	// well.
	// */
	// private void cleanupBeforeCommentWhitespace() {
	// TokenImpl comment = tokens.get(tokens.size()-1);
	// TokenImpl plain = tokens.get(tokens.size()-2);
	//
	// int beginMark = comment.begin;
	// int newLineMark = 0;
	// for(int i = newLineMarks.size()-1; i > 0; i--) {
	// newLineMark = newLineMarks.get(i);
	// if(newLineMark < beginMark) {
	// break;
	// }
	// }
	// String candidateWhitespace = pageSource.substring(newLineMark,
	// beginMark-2);
	// if(!candidateWhitespace.trim().equals("")) {
	// return;
	// }
	//
	// //otherwise, let's change some things
	// comment.begin = newLineMark;
	// plain.end = newLineMark;
	// plain.endLineNumber--;
	// comment.beginLineNumber++;
	// }

	private void found(TemplateToken newState, int skip, int endLineNumber) {
		found(newState, skip, endLineNumber, false, false);
	}

	private void found(TemplateToken newState, int skip, int endLineNumber, boolean isOpenCloseTag,
			boolean hasNewLine) {
		TemplateToken finalState = state;
		if (isOpenCloseTag)
			finalState = START_END_TAG;

		--end;
		int endValue = end;
		if (hasNewLine) // special case for PLAIN
			endValue++;

		TokenImpl lastToken = new TokenImpl(filePath, begin, endValue, finalState, beginLineNumber, endLineNumber,
				pageSource);
		if (finalState != PLAIN) {
			lastToken.verifyContentsHaveNoStartTag();
		}

		begin = end += skip;
		beginLineNumber = endLineNumber;
		state = newState;
		tokens.add(lastToken);
	}

}
