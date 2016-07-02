package org.webpieces.templating.impl.source;

import java.util.List;
import java.util.regex.Pattern;

public class GroovySrcWriter {

	//Some compilers can't deal with long lines so let's max at 40k
    protected static final int maxLineLength = 30000;
    
	private Pattern pattern = Pattern.compile("\"");

	public void printHead(ScriptCode sourceCode, String packageStr, String className) {

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

	public void printEnd(ScriptCode sourceCode) {
		sourceCode.println("    }");
		sourceCode.println("  }");
		sourceCode.println("}");
	}

	public void printPlain(Token token, ScriptCode sourceCode) {
		String srcText = token.getValue();
		if(srcText.length() < maxLineLength) {
			String text = addEscapesToSrc(srcText);
			sourceCode.println("      out.print(\""+text+"\");");
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
			sourceCode.println("      out.print(\""+text+"\");");
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

	public void printExpression(Token token, ScriptCode sourceCode) {
		String expr = token.getValue().trim();
        sourceCode.print("      out.print(__escapeTextCharacters("+expr+"));");
        sourceCode.appendTokenComment(token);
        sourceCode.println();
	}

	public void printMessage() {
		
	}

	public void printAction(boolean b) {
		
	}

	public void printStartTag() {
	}

	public void printEndTag() {
		
	}

	public void unprintUpToLastNewLine() {
		
	}

	public void verifyTagIntegrity(List<Token> tokens) {
		
	}

}
