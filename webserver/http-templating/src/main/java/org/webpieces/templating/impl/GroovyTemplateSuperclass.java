package org.webpieces.templating.impl;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.webpieces.ctx.api.Current;
import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.impl.html.EscapeHTMLFormatter;
import org.webpieces.templating.impl.html.NullFormatter;

import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

public abstract class GroovyTemplateSuperclass extends Script {

	public static String OUT_PROPERTY_NAME = "__out";
	public static final EscapeHTMLFormatter ESCAPE_HTML_FORMATTER = new EscapeHTMLFormatter();
	private static final NullFormatter NULL_FORMATTER = new NullFormatter();
	
	private EscapeCharactersFormatter formatter;
	private HtmlTagLookup tagLookup;
	private Map<Object, Object> setTagProperties;
	private String superTemplateFilePath;
	private RouterLookup urlLookup;
	private ThreadLocal<String> sourceLocal = new ThreadLocal<>();
	
    public void initialize(EscapeCharactersFormatter f, HtmlTagLookup tagLookup, 
    		Map<Object, Object> setTagProps, RouterLookup urlLookup) {
    	formatter = f;
    	this.tagLookup = tagLookup;
    	this.setTagProperties = setTagProps;
    	this.urlLookup = urlLookup;
    }
    
    //TODO: Make formatter a ThreadLocal<Stack> such that if you nest verbatims with tags
    //then uninstallNullFormatter only means pop the stack such that you are still in verbatim
    //ie. this won't work well with nested verbatim right now
    protected void installNullFormatter() {
    	formatter = NULL_FORMATTER;
    }
    
    protected void installHtmlFormatter() {
    	formatter = ESCAPE_HTML_FORMATTER;
    }

    public String useFormatter(Object val) {
    	return formatter.format(val);
    }

    protected void runTag(String tagName, Map<Object, Object> args, Closure<?> closure, String srcLocation) {
    	srcLocation = modifySourceLocation2(srcLocation);
    	HtmlTag tag = tagLookup.lookup(tagName);
    	PrintWriter writer = (PrintWriter) getProperty(OUT_PROPERTY_NAME);
    	try {
    		tag.runTag(args, closure, writer, this, srcLocation);
    	} catch(Exception e) {
			throw new RuntimeException("Error running tag #{"+tagName+"}#.  See exception cause below."+srcLocation, e);
    	}
    }

    protected String runClosure(String tagName, Closure<?> closure, String srcLocation) {
    	srcLocation = modifySourceLocation2(srcLocation);
    	try {
    		return ClosureUtil.toString(closure);
    	} catch(Exception e) {
			throw new RuntimeException("Error running tag #{"+tagName+"}#.  See exception cause below."+srcLocation, e);
    	}
    }    
    
	public void putSetTagProperty(Object key, Object val) {
		setTagProperties.put(key, val);
	}
	
	public Object getSetTagProperty(Object key) {
		return setTagProperties.get(key);
	}
	
	public void setSuperTemplateFilePath(String path) {
		this.superTemplateFilePath = path;
	}

	public String getSuperTemplateFilePath() {
		return superTemplateFilePath;
	}

	public Map<Object, Object> getSetTagProperties() {
		return setTagProperties;
	}
	
	public String fetchUrl(String routeId, Map<?, ?> args, String srcLocation) {
    	srcLocation = modifySourceLocation2(srcLocation);
		try {
			Map<String, String> urlParams = new HashMap<>();
			for(Map.Entry<?, ?> entry : args.entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				if(value == null)
					throw new IllegalArgumentException("Cannot use null param in a url for param name="+key);
				//urlencoding happens for each value in the router...
				urlParams.put(key, value.toString());
			}

			return urlLookup.fetchUrl(routeId, urlParams);
		} catch(Exception e) {
			throw new RuntimeException("Error fetching route='"+routeId+"' ("+e.getMessage()+").  "+srcLocation+"\nThe list of params fed from html file="+args, e);
		}
	}

	public Object optional(String property) {
		try {
			return super.getProperty(property);
		} catch(MissingPropertyException e) {
			return null;
		}
	}
	
    @Override
    public Object getProperty(String property) {
    	String srcLocation = modifySourceLocation2(sourceLocal.get());
        try {
            return super.getProperty(property);
        } catch (MissingPropertyException e) {
        	throw new IllegalArgumentException("No such property '"+property+"' but perhaps you forgot quotes "
        			+ "around it or you forgot to pass it in from the controller's return value(with the RouteId) OR "
        			+ "lastly, if this is inside a custom tag, perhaps the tag did not pass in the correct arguments."+srcLocation, e);
        }
    }
    
    public String getMessage(Object ... args) {
    	String srcLocation = modifySourceLocation2(sourceLocal.get());
    	if(args.length < 2)
    		throw new IllegalArgumentException("&{...}& must include two arguments separated by a comma and did not."+srcLocation);
    	else if(args[1] == null)
    		throw new IllegalArgumentException("The second argument in &{...}& evaluated to null...did you forget quotes or "
    				+ "you forgot to pass in a valid value to that variable?"+srcLocation);
    	
    	String defaultText = "";
    	if(args[0] != null)
    		defaultText = args[0].toString();
    	String key = args[1].toString();

    	Object[] newArgs = createArgsArray(args);
    	
    	String msg = getMessageImpl(defaultText, key);
    	String result = MessageFormat.format(msg, newArgs);
    	
    	return result;
    }

	private Object[] createArgsArray(Object... args) {
		List<Object> argsList = new ArrayList<>();
    	for(int i = 2; i < args.length; i++) {
    		argsList.add(args[i]);
    	}
    	Object[] newArgs = argsList.toArray();
		return newArgs;
	}
    
    private String getMessageImpl(String defaultText, String key) {
    	List<Locale> locales = Current.request().preferredLocales;
    	
    	for(Locale l : locales) {
    		String message = Current.messages().get(key, l);
    		if(message != null)
    			return message;
    	}
    	
    	return defaultText;
    }
    
    //We should probably turn this into a ThreadLocal<Stack> ...
    public void enterExpression(String srcLocation) {
    	sourceLocal.set(srcLocation);
    }
    
    public void exitExpression() {
    	sourceLocal.set(null);
    }
	
	public static String modifySourceLocation2(String srcLocation) {
		return "\n\n\t"+srcLocation+"\n";
	}
}
