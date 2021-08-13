package org.webpieces.templatingdev.impl.source;

import java.util.Stack;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templatingdev.api.AbstractGroovyGen;
import org.webpieces.templatingdev.api.GroovyGen;
import org.webpieces.templatingdev.api.TemplateCompileConfig;
import org.webpieces.templatingdev.impl.tags.RoutePathTranslator;
import org.webpieces.templatingdev.impl.tags.TagGen;

public class ScriptWriter {

	//Some compilers can't deal with long lines so let's max at 30k
    protected static final int MAX_LINE_LENGTH = 30000;
    
	private Pattern pattern = Pattern.compile("\"");

	private ThreadLocal<Stack<TagState>> tagStack = new ThreadLocal<>();
	private GenLookup generatorLookup;
	private HtmlTagLookup htmlTagLookup;
	private UniqueIdGenerator uniqueIdGen;
	private TemplateCompileConfig config;
	private RoutePathTranslator translator;
	private boolean initialized;

	@Inject
	public ScriptWriter(
			HtmlTagLookup htmlTagLookup, 
			GenLookup lookup, 
			UniqueIdGenerator generator,
			RoutePathTranslator translator,
			TemplateCompileConfig config) {
		this.htmlTagLookup = htmlTagLookup;
		generatorLookup = lookup;
		this.uniqueIdGen = generator;
		this.translator = translator;
		this.config = config;
	}
	
	public void init() {
		if(initialized)
			return;
		
		initialized = true;
		generatorLookup.init();
	}
	
	public void printHead(ScriptOutputImpl sourceCode, String packageStr, String className) {
		tagStack.set(new Stack<>());

		if(packageStr != null && !"".equals(packageStr.trim())) {
			sourceCode.println("package "+packageStr, null);
			sourceCode.println();
		}
		
		sourceCode.println("import org.webpieces.ctx.api.Current", null);
		sourceCode.println();
		
        sourceCode.print("class ");
        //This generated classname is parsed when creating cleanStackTrace.
        //The part after "Template_" is used as key when
        //looking up the file on disk this template-class is generated from.
        //cleanStackTrace is looking in TemplateLoader.templates

        sourceCode.print(className);
        sourceCode.println(" extends org.webpieces.templating.impl.GroovyTemplateSuperclass {", null);
        sourceCode.println("  public Object run() {", null);
        sourceCode.println("    use(org.webpieces.templating.impl.source.GroovyExtensions) {", null);
        
//        for (String n : extensionsClassnames) {
//            println("use(_('" + n + "')) {");
//        }
	}

	public void printEnd(ScriptOutputImpl sourceCode) {
		sourceCode.println("    }", null);
		sourceCode.println("  }", null);
		sourceCode.println("}", null);
		
		if(tagStack.get().size() > 0) {
			TagState state = tagStack.get().pop();
			TokenImpl token = state.getToken();
			throw new IllegalStateException("Found unmatched tag #{"+token.getCleanValue()+"}#. "+token.getSourceLocation(true)); 
		}
	}

	public void printPlain(TokenImpl token, ScriptOutputImpl sourceCode) {
		String srcText = token.getValue();
		if(srcText.length() < MAX_LINE_LENGTH) {
			String text = addEscapesForGroovy(srcText);
			sourceCode.println("      __out.print(\""+text+"\");", token);
			return;
		}

		//while our max line lenght is 40k, the addEscapes lengthens the text for each new line and each
		//'/' character BUT someone would have to double the size so just throw in that one case to notify
		//the user before groovy breaks(this should not happen, but who knows....fail fast)
		while(srcText.length() > 0) {
			int cutpoint = Math.min(srcText.length(), MAX_LINE_LENGTH);
			String prefix = srcText.substring(0, cutpoint);
			srcText = srcText.substring(cutpoint);
			String text = addEscapesForGroovy(prefix);
			sourceCode.println("       __out.print(\""+text+"\");", token);
		}
	}

	private String addEscapesForGroovy(String srcText) {
        String text = srcText.replace("\\", "\\\\");
        text = pattern.matcher(text).replaceAll("\\\\\"");
        text = text.replace("\n", "\\n");
        text = text.replace("$", "\\$");
        return text;
	}

	public void printScript(TokenImpl token, ScriptOutputImpl sourceCode) {
		sourceCode.println(token.getValue().trim(), token);
	}

	public void printExpression(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		sourceCode.println();
		sourceCode.println("       enterExpression('"+token.getSourceLocation(false)+"');", token); //purely so we can add info to missing properties

		if(expr.startsWith("^")) {
			//variables that get prefixed with ^ do not get html escaped so they can return html to put 
			//in the page like escOff#body
			String newExpr = expr.substring("^".length());
			sourceCode.println("       __out.print("+newExpr+");", token);
		} else if(expr.startsWith("escOff#")) {
			//variables that get prefixed with escOff# do not get html escaped so they can return html to put 
			//in the page like escOff#body
			String newExpr = expr.substring("escOff#".length());
			sourceCode.println("       __out.print("+newExpr+");", token);
		} else
			sourceCode.println("       __out.print(useFormatter("+expr+"));", token);
		sourceCode.println("       exitExpression();", token);
		sourceCode.println();
	}

	public void printMessage(TokenImpl token, ScriptOutputImpl sourceCode) {
		String value = token.getValue().replaceAll("\r", "");
		String withValidNewLines = replaceNewLinesBetweenQuotes(value, token); 
		
		//any newlines left, we can remove
		withValidNewLines = withValidNewLines.replace("\n", " ");
		
		sourceCode.println();
		//This is here so when groovy calls getProperty() to resolve variables, there is info on what line had the issue if not there
		sourceCode.println("       enterExpression('"+token.getSourceLocation(false)+"');", token); //purely so we can add info to missing properties
		sourceCode.println("       __out.print(getMessage("+withValidNewLines+"));", token);
		sourceCode.println("       exitExpression();", token);
		sourceCode.println();		
	}

	private String replaceNewLinesBetweenQuotes(String value, TokenImpl token) {
		int currentIndex = 0;
		
		while(true) {
			int indexOf = value.indexOf("'", currentIndex);
			if(indexOf < 0)
				return value;
			
			int secondQuote = value.indexOf("'", indexOf+1);
			if(secondQuote < 0)
				throw new IllegalArgumentException("unbalanced quote in &{...}&. "+token.getSourceLocation(true));
			
			String before = value.substring(0, indexOf);
			String middle = value.substring(indexOf, secondQuote+1);
			middle = middle.replaceAll("\n", "\\\\n");
			String after = value.substring(secondQuote+1);
			
			String firstPartWithQuotes = before+middle;
			currentIndex = firstPartWithQuotes.length(); //set current index to after the quote
			
			value = firstPartWithQuotes + after;
		}
	}

	public void printAction(TokenImpl token, ScriptOutputImpl sourceCode, boolean isAbsolute) {
		if(isAbsolute)
			throw new UnsupportedOperationException("not supported yet.  Need to modify to use the Host header as input as the domain");

		String expr = token.getCleanValue();

		String groovySnippet = translator.translateRouteId(expr, token);
		//fetchUrl('VERBATIM_ROUTE_ID', [:], 'at org.webpieces.webserver.tags.app.aHrefTag.html(aHrefTag.html:5)')
		sourceCode.println("       __out.print("+groovySnippet+");", token);
	}

	/**
	 * This is for tags with no body(or ones where the body is optional and #{../}# was used.
	 * 
	 * @param token
	 * @param sourceCode
	 * @param callbacks 
	 */
	public void printStartEndTag(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagName = expr;
		if(indexOfSpace > 0) {
			tagName = expr.substring(0, indexOfSpace);
		}

		int id = uniqueIdGen.generateId();
		GroovyGen generator = generatorLookup.lookup(tagName, token);
		//we only lookup htmltag to verify it's existence and fail fast
		HtmlTag htmltag = htmlTagLookup.lookup(tagName);
		if(generator != null) {
			generator.generateStartAndEnd(sourceCode, token, id);
		} else {
			if(htmltag == null && !config.getCustomTagsFromPlugin().contains(tagName))
					throw new IllegalArgumentException("Unknown tag=#{"+tagName+"}# OR you didn't add '"
								+tagName+"' to list of customTags in build.gradle file. "+token.getSourceLocation(true));

			new TagGen(tagName, token, translator).generateStartAndEnd(sourceCode, token, id);
		}
	}

	public void printStartTag(TokenImpl token, TokenImpl previousToken, ScriptOutputImpl sourceCode) {
		String tagName = token.getTagName();

		GroovyGen generator = generatorLookup.lookup(tagName, token);
		HtmlTag htmltag = htmlTagLookup.lookup(tagName);
		if(generator != null) {
			if(generator instanceof AbstractGroovyGen) {
				AbstractGroovyGen abstractTag = (AbstractGroovyGen) generator;
				//Things like #{else}# tag are given chance to validate that it is only after an #{if}# tag
				abstractTag.validatePreviousSibling(token, previousToken);
			}
		} else {
			if(htmltag == null && !config.getCustomTagsFromPlugin().contains(tagName))
				throw new IllegalArgumentException("Unknown tag=#{"+tagName+"}# OR you didn't add '"
							+tagName+"' to list of customTags in build.gradle file. "+token.getSourceLocation(true));

			generator = new TagGen(tagName, token, translator);
		}

		int id = uniqueIdGen.generateId();
		generator.generateStart(sourceCode, token, id);
		tagStack.get().push(new TagState(token, generator, id));
	}

	public void printEndTag(TokenImpl token, ScriptOutputImpl sourceCode) {
		String expr = token.getCleanValue();
		if(tagStack.get().size() == 0)
			throw new IllegalArgumentException("Unmatched end tag #{/"+expr+"}# as the begin tag was not found..only the end tag. location="+token.getSourceLocation(true));
		TagState currentState = tagStack.get().pop();
		TokenImpl currentToken = currentState.getToken();
		if(!expr.equals(currentToken.getTagName()))
			throw new IllegalArgumentException("Unmatched end tag #{/"+expr+"}# as the begin tag appears to be #{"+currentToken.getCleanValue()
			+"}# which does not match.  end tag location="+token.getSourceLocation(false)+" begin tag location="+currentToken.getSourceLocation(false));

		GroovyGen generator = currentState.getGenerator();
		int uniqueId = currentState.getUniqueId();
		generator.generateEnd(sourceCode, token, uniqueId);
	}

	public void unprintUpToLastNewLine() {
		
	}

	public void cleanup() {
		tagStack.set(null);
	}

	public void printFilePath(TokenImpl token, ScriptOutputImpl sourceCode) {
		//TODO: record the script to pre-emptively send by calling into the groovy
		//superclass recording all these scripts to send
		//THEN, after script is run, client can call getScriptsToPreemptivelySend and send
		//all those scripts before the browser client asks for them(in http2 at least)
		//BUT we must also RECORD all these on that connection and not send them a 
		//second time which would be a waste of our CPU
		String value = token.getCleanValue();
		String path = translator.recordPath(value, token.getSourceLocation(false));
		sourceCode.println("       __out.print(\""+path+"\");", token);
	}

	public void printEscaped(TokenImpl token, ScriptOutputImpl sourceCode) {
		String value = token.getValue();
		value = StringEscapeUtils.escapeHtml4(value);
		value = addEscapesForGroovy(value);
		sourceCode.println("       __out.print(\""+value+"\");", token);
	}

}
