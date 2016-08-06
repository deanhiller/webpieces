package org.webpieces.templating.impl.source;

import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.webpieces.templating.api.AbstractTag;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.impl.tags.TagGen;
import org.webpieces.templating.api.GroovyGen;

public class ScriptWriter {

	//Some compilers can't deal with long lines so let's max at 30k
    protected static final int maxLineLength = 30000;
    
	private Pattern pattern = Pattern.compile("\"");

	private ThreadLocal<Stack<GroovyGen>> tagStack = new ThreadLocal<>();
	private GenLookup generatorLookup;
	private HtmlTagLookup htmlTagLookup;
	private UniqueIdGenerator uniqueIdGen;

	@Inject
	public ScriptWriter(HtmlTagLookup htmlTagLookup, GenLookup lookup, UniqueIdGenerator generator) {
		this.htmlTagLookup = htmlTagLookup;
		generatorLookup = lookup;
		this.uniqueIdGen = generator;
	}
	
	public void printHead(ScriptOutputImpl sourceCode, String packageStr, String className) {
		tagStack.set(new Stack<>());

		if(packageStr != null && !"".equals(packageStr.trim())) {
			sourceCode.println("package "+packageStr);
		}
		
        sourceCode.print("class ");
        //This generated classname is parsed when creating cleanStackTrace.
        //The part after "Template_" is used as key when
        //looking up the file on disk this template-class is generated from.
        //cleanStackTrace is looking in TemplateLoader.templates

        sourceCode.print(className);
        sourceCode.println(" extends org.webpieces.templating.impl.GroovyTemplateSuperclass {");
        sourceCode.println("  public Object run() {");
        sourceCode.println("    use(org.webpieces.templating.impl.source.GroovyExtensions) {");
//        for (String n : extensionsClassnames) {
//            println("use(_('" + n + "')) {");
//        }
	}

	public void printEnd(ScriptOutputImpl sourceCode) {
		sourceCode.println("    }");
		sourceCode.println("  }");
		sourceCode.println("}");
		
		tagStack.set(null);
	}

	public void printPlain(TokenImpl token, ScriptOutputImpl sourceCode) {
		String srcText = token.getValue();
		if(srcText.length() < maxLineLength) {
			String text = addEscapesToSrc(srcText);
			sourceCode.println("      __out.print(\""+text+"\");");
			return;
		}

		//while our max line lenght is 40k, the addEscapes lengthens the text for each new line and each
		//'/' character BUT someone would have to double the size so just throw in that one case to notify
		//the user before groovy breaks(this should not happen, but who knows....fail fast)
		while(srcText.length() > 0) {
			int cutpoint = Math.min(srcText.length(), maxLineLength);
			String prefix = srcText.substring(0, cutpoint);
			srcText = srcText.substring(cutpoint);
			String text = addEscapesToSrc(prefix);
			sourceCode.println("      __out.print(\""+text+"\");");
		}
	}

	private String addEscapesToSrc(String srcText) {
        String text = srcText.replace("\\", "\\\\");
        text = pattern.matcher(text).replaceAll("\\\\\"");
        text = text.replaceAll("\n", "\\\\n");
        return text;
	}

	public void printScript() {
		
	}

	public void printExpression(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getValue().trim();
        sourceCode.print("      __out.print(useFormatter("+expr+"));");
        sourceCode.appendTokenComment(token);
        sourceCode.println();
	}

	public void printMessage() {
		
	}

	public void printAction(boolean b) {
		
	}

	/**
	 * This is for tags with no body(or ones where the body is optional and #{../}# was used.
	 * 
	 * @param token
	 * @param sourceCode
	 */
	public void printStartEndTag(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagName = expr;
		if(indexOfSpace > 0) {
			tagName = expr.substring(0, indexOfSpace);
		}
		
		GroovyGen generator = generatorLookup.lookup(tagName, token);
		HtmlTag htmltag = htmlTagLookup.lookup(tagName);
		if(generator != null) {
			generator.generateStartAndEnd(sourceCode, token);
		} else if(htmltag == null) {
			throw new IllegalArgumentException("Unknown tag="+tagName+" location="+token.getSourceLocation());
		} else {
			int id = uniqueIdGen.generateId();
			new TagGen(tagName, token, id).generateStartAndEnd(sourceCode, token);
		}
	}

	public void printStartTag(TokenImpl token, TokenImpl previousToken, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagName = expr;
		if(indexOfSpace > 0) {
			tagName = expr.substring(0, indexOfSpace);
		}

		GroovyGen generator = generatorLookup.lookup(tagName, token);
		HtmlTag htmltag = htmlTagLookup.lookup(tagName);
		if(generator != null) {
			if(generator instanceof AbstractTag) {
				AbstractTag abstractTag = (AbstractTag) generator;
				//Things like #{else}# tag are given chance to validate that it is only after an #{if}# tag
				abstractTag.validatePreviousSibling(token, previousToken);
			}
		} else if(htmltag == null) {
			throw new IllegalArgumentException("Unknown tag="+tagName+" location="+token.getSourceLocation());
		} else {
			int id = uniqueIdGen.generateId();
			generator = new TagGen(tagName, token, id);
		}

		generator.generateStart(sourceCode, token);
		tagStack.get().push(generator);
	}

	public void printEndTag(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		if(tagStack.get().size() == 0)
			throw new IllegalArgumentException("Unmatched end tag #{/"+expr+"}# as the begin tag was not found..only the end tag. location="+token.getSourceLocation());
		GroovyGen currentGenerator = tagStack.get().pop();
		if(!expr.equals(currentGenerator.getName()))
			throw new IllegalArgumentException("Unmatched end tag #{/"+expr+"}# as the begin tag appears to be #{"+currentGenerator
			+"}# which does not match.  end tag location="+token.getSourceLocation());

		currentGenerator.generateEnd(sourceCode, token);
	}

	public void unprintUpToLastNewLine() {
		
	}

	public void verifyTagIntegrity(List<TokenImpl> tokens) {
		
	}

}
