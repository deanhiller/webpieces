package org.webpieces.templating.impl.tags;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.webpieces.ctx.api.Constants;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Flash;
import org.webpieces.ctx.api.Validation;
import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import groovy.lang.Closure;

/**
 * challenging to get right so heavily tested
 * 
 * 1. on first GET request must render bean from an enum, array, collection, or just prmitive field as string
 * 2. on second GET request must render flash IF set(even if null!!!) or the bean BUT the twist is flash is all Strings
 *
 * 
 * @author dhiller
 *
 */
public class FieldTag extends TemplateLoaderTag implements HtmlTag {

	private static final Logger log = LoggerFactory.getLogger(FieldTag.class);
	private Pattern pattern = Pattern.compile("\\[(.*?)\\]"); //for arrays only
	private String fieldHtmlPath;
	private ConverterLookup converter;

	public FieldTag(ConverterLookup converter, String fieldHtmlPath) {
		this.converter = converter;
		this.fieldHtmlPath = fieldHtmlPath;
	}

	@Override
	public String getName() {
		return "field";
	}

	protected String getErrorClass() {
		return "error";
	}
	
	@Override
	protected String getFilePath(GroovyTemplateSuperclass callingTemplate, Map<Object, Object> args, String srcLocation) {
		return fieldHtmlPath;
	}
	
	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs, Closure<?> body, String srcLocation) {
		if(tagArgs.get("_body") != null)
			throw new IllegalArgumentException("tag "+getName()+" must not define an argument of '_body' as that is reserved and will be overwritten"+srcLocation);
		else if(tagArgs.get("field") != null)
			throw new IllegalArgumentException("tag "+getName()+" must not define an argument of 'field' as that is reserved and will be overwritten "+srcLocation);

        String fieldName = tagArgs.get("defaultArgument").toString();
		Map<String, Object> field = createFieldData(fieldName, pageArgs);
        
		Map<String, Object> copyOfTagArgs = new HashMap<>();
		Map<String, Object> closureProps = new HashMap<>();		
		for(Map.Entry<Object, Object> entry : tagArgs.entrySet()) {
			String key = entry.getKey().toString();
			copyOfTagArgs.put(key, entry.getValue());
			if(body != null)
				body.setProperty(key, entry.getValue());
			closureProps.put(key, entry.getValue());
		}
		
		copyOfTagArgs.put("field", field);
		
		String bodyStr = "";
		if(body != null) {
			body.setProperty("field", field);
			closureProps.put("field", field);
			bodyStr = ClosureUtil.toString(getName(), body, closureProps);
		}
		//variables starting with _ will not be html escaped so the body html won't be converted like other variables
		copyOfTagArgs.put("_body", bodyStr);
		
		return copyOfTagArgs;
	}

	/**
	 *
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, Object> createFieldData(String fieldName2, Map<String, Object> pageArgs) {

		Result result = reworkNameForArrayOnly(fieldName2, pageArgs);
		String fieldName = result.fieldName;

        Flash flash = Current.flash();
        Validation validation = Current.validation();
        
        Map<String, Object> field = new HashMap<String, Object>();
        field.put("name", fieldName);
        String id = makeValidHtml4Id(fieldName);
        field.put("id", id);
        String flashValue = flash.get(fieldName);
        field.put("i18nKey", result.i18nName); //different from fieldName only for Arrays
        field.put("flash", flashValue);
        field.put("error", validation.getError(fieldName));
        field.put("errorClass", field.get("error") != null ? getErrorClass() : "");
        String[] pieces = fieldName.split("\\.");
        Object pageArgValue = null;
        Object obj = pageArgs.get(pieces[0]);
        
        //This breaks AJAX stuff...
        @SuppressWarnings("unchecked")
		Set<String> keys = (Set<String>) pageArgs.get(Constants.KEYS);
        if(!keys.contains(pieces[0]))
        	throw new IllegalArgumentException("Controller did not pass a value(null is fine) for key='"+pieces[0]+"'");
        
        if (pieces.length > 1) {
            try {
                String path = fieldName.substring(fieldName.indexOf(".") + 1);
                pageArgValue = PropertyUtils.getProperty(obj, path);
            } catch (Exception e) {
                // if there is a problem reading the field we dont set any
                // value
            	log.trace(() -> "exception", e);
            }
        } else {
        	//for method parameters not fields in the bean like above
        	pageArgValue = obj;
        }

        field.put("value", pageArgValue);

        String valAsStr = null;
        if(pageArgValue instanceof Collection) {
        	//For multiple select html OptionTag to work, this was needed
        	valAsStr = convert((Collection)pageArgValue);
        } else {
        	valAsStr = converter.convert(pageArgValue);
        }
        field.put("flashOrValue", preferFirst(flashValue, valAsStr));
        field.put("valueOrFlash", preferFirst(valAsStr, flashValue));
        
		return field;
	}

	@SuppressWarnings("rawtypes")
	private String convert(Collection pageArgValue) {
		Iterator iterator = pageArgValue.iterator();
		String result = "";
		String seperator = "";
		while(iterator.hasNext()) {
			Object bean = iterator.next();
        	String valAsStr = converter.convert(bean);
        	result += seperator + valAsStr;
        	if(seperator.equals(""))
        		seperator = ",";
		}
		return result;
	}

	protected Result reworkNameForArrayOnly(String fieldName, Map<String, Object> pageArgs) {
		if(!fieldName.contains("["))
			return new Result(fieldName, fieldName);
		
		//modify field name to be array format with real index
		//go from user.accounts[account_index].addresses[address_index].street to
		//        user.accounts[0].addresses[1].street such that PropertyUtils.getProperty(bean, fieldName) works 
		//i18n name will be 'user.accounts.addresses.street';
		
		String i18nName = fieldName;
		Matcher m = pattern.matcher(fieldName);
		while(m.find()) {
		    String indexName = m.group(1);
		    Object index = pageArgs.get(indexName);
		    fieldName = fieldName.replace(indexName, index+"");
		    i18nName = i18nName.replace("["+indexName+"]", "");
		}
		
		return new Result(fieldName, i18nName);
	}

	private String makeValidHtml4Id(String fieldName) {
		return fieldName.replace('.', '_').replace("[", ":").replace("]", ":");
	}

	private String preferFirst(String first, String last) {
		if(first != null)
			return first;
		return last;
	}

	private static class Result {
		public String fieldName;
		public String i18nName;

		public Result(String fieldName, String i18nName) {
			this.fieldName = fieldName;
			this.i18nName = i18nName;
		}
	}
}
