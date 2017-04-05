package org.webpieces.templatingdev.impl.source;

import java.util.List;

import javax.inject.Inject;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class GroovyScriptGenerator {

	private static final Logger log = LoggerFactory.getLogger(GroovyScriptGenerator.class);
	private TemplateTokenizer tokenizer;
	private ScriptWriter creator;

	@Inject
	public GroovyScriptGenerator(TemplateTokenizer tokenizer, ScriptWriter creator) {
		this.tokenizer = tokenizer;
		this.creator = creator;
	}
	
	public ScriptOutputImpl generate(String filePath, String source, String fullClassName) {
		creator.init();
		
		long start = System.currentTimeMillis();
		source = source.replace("\r", "");
		
		List<TokenImpl> tokens = tokenizer.tokenize(filePath, source);

		String className = fullClassName;
		String packageStr = null;
		//split class name if it has package
		int index = fullClassName.lastIndexOf(".");
		if(index > 0) {
			className = fullClassName.substring(index+1);
			packageStr = fullClassName.substring(0, index);
		}

		ScriptOutputImpl sourceCode = new ScriptOutputImpl(packageStr, className);

		try {
			// Class header
			creator.printHead(sourceCode, packageStr, className);
	
			generateBody(sourceCode, tokens);
	
			// Class end
			creator.printEnd(sourceCode);
		} finally {
			creator.cleanup();
		}
		
		TokenImpl token = tokens.get(tokens.size()-1);
		int lastLine = token.endLineNumber;
		long total = System.currentTimeMillis() - start;
		log.info(total+"ms (dev mode only) source generation. class="+className+" from "+lastLine+" html lines of code to "+sourceCode.getLineNumber()+" lines of groovy code");
		
		return sourceCode;
	}

	private void generateBody(ScriptOutputImpl sourceCode, List<TokenImpl> tokens) {
		for(int i = 0; i < tokens.size(); i++) {
			TokenImpl token = tokens.get(i);
			TokenImpl previousToken = null;
			if(i > 0)
				previousToken = tokens.get(i-1);
			
			TemplateToken state = token.state;
			
			switch (state) {
			case EOF:
				return;
			case PLAIN:
				creator.printPlain(token, sourceCode);
				break;
			case SCRIPT:
				creator.printScript(token, sourceCode);
				break;
			case FILE_VERIFY:
				creator.printFilePath(token, sourceCode);				
				break;
			case EXPR:
				creator.printExpression(token, sourceCode);
				break;
			case MESSAGE:
				creator.printMessage(token, sourceCode);
				break;
			case ACTION:
				creator.printAction(token, sourceCode, false);
				break;
			case ABSOLUTE_ACTION:
				creator.printAction(token, sourceCode, true);
				break;
			case COMMENT:
				creator.unprintUpToLastNewLine();
				break;
			case ESCAPE:
				creator.printEscaped(token, sourceCode);
				break;
			case START_END_TAG:
				creator.printStartEndTag(token, sourceCode);
				break;
			case START_TAG:
				creator.printStartTag(token, previousToken, sourceCode);
				break;
			case END_TAG:
				creator.printEndTag(token, sourceCode);
				break;
			}
		}
	}
}
