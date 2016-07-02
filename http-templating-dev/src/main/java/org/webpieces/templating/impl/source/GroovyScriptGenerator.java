package org.webpieces.templating.impl.source;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyScriptGenerator {

	private static final Logger log = LoggerFactory.getLogger(GroovyScriptGenerator.class);
	private TemplateTokenizer tokenizer;
	private GroovySrcWriter creator;

	@Inject
	public GroovyScriptGenerator(TemplateTokenizer tokenizer, GroovySrcWriter creator) {
		this.tokenizer = tokenizer;
		this.creator = creator;
	}
	
	public ScriptCode generate(String source, String className) {
		long start = System.currentTimeMillis();
		source = source.replace("\r", "");
		
		List<Token> tokens = tokenizer.tokenize(source);

		ScriptCode sourceCode = new ScriptCode();
		// Class header
		creator.printHead(sourceCode, className);

		generateBody(sourceCode, tokens);

		// Class end
		creator.printEnd(sourceCode);
		
		Token token = tokens.get(tokens.size()-1);
		int lastLine = token.endLineNumber;
		long total = System.currentTimeMillis() - start;
		log.info(total+"ms source generation. class="+className+" from "+lastLine+" html lines of code to "+sourceCode.getLineNumber()+" lines of groovy code");
		
		return sourceCode;
	}

	private void generateBody(ScriptCode sourceCode, List<Token> tokens) {

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
