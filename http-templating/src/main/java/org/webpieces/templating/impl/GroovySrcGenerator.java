package org.webpieces.templating.impl;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovySrcGenerator {

	private static final Logger log = LoggerFactory.getLogger(GroovySrcGenerator.class);
	private TemplateTokenizer tokenizer;
	private SourceCreator creator;

	@Inject
	public GroovySrcGenerator(TemplateTokenizer tokenizer, SourceCreator creator) {
		this.tokenizer = tokenizer;
		this.creator = creator;
	}
	
	public SourceState generate(String source, String className) {
		source = source.replace("\r", "");
		
		List<Token> tokens = tokenizer.tokenize(source);

		SourceState sourceCode = new SourceState();
		// Class header
		creator.printHead(sourceCode, className);

		generateBody(sourceCode, tokens);

		// Class end
		creator.printEnd(sourceCode);
		
		return sourceCode;
	}

	private void generateBody(SourceState sourceCode, List<Token> tokens) {

		for(Token token : tokens) {
			ScriptToken state = token.state;
			
			switch (state) {
			case EOF:
				return;
			case PLAIN:
				creator.printPlain(token, sourceCode);
				break;
			case SCRIPT:
				creator.printScript();
				break;
			case EXPR:
				creator.printExpression(token, sourceCode);
				break;
			case MESSAGE:
				creator.printMessage();
				break;
			case ACTION:
				creator.printAction(false);
				break;
			case ABSOLUTE_ACTION:
				creator.printAction(true);
				break;
			case COMMENT:
				creator.unprintUpToLastNewLine();
				break;
			case START_TAG:
				creator.printStartTag();
				break;
			case END_TAG:
				creator.printEndTag();
				break;
			}
		}
		
		creator.verifyTagIntegrity(tokens);
	}
}
