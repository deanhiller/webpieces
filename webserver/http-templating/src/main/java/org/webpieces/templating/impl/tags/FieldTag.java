package org.webpieces.templating.impl.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Flash;
import org.webpieces.ctx.api.Validation;
import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class FieldTag extends TemplateLoaderTag implements HtmlTag {

	private Pattern pattern = Pattern.compile("\\[(.*?)\\]"); //for arrays only
	private String fieldHtmlPath;

	public FieldTag(String fieldHtmlPath) {
		this.fieldHtmlPath = fieldHtmlPath;
	}

	@Override
	public String getName() {
		return "field";
	}

	@Override
	protected String getFilePath(GroovyTemplateSuperclass callingTemplate, Map<Object, Object> args, String srcLocation) {
		return fieldHtmlPath;
	}
	
	@Override
	protected Map<String, Object> convertTagArgs(Map<Object, Object> tagArgs, Map<String, Object> pageArgs, Closure<?> body, String srcLocation) {
		if(tagArgs.get("_body") != null)
			throw new IllegalArgumentException("tag "+getName()+" must not define an argument of '_body' as that is reserved and will be overwritten");
		else if(tagArgs.get("field") != null)
			throw new IllegalArgumentException("tag "+getName()+" must not define an argument of 'field' as that is reserved and will be overwritten ");

        String fieldName = tagArgs.get("defaultArgument").toString();
		Map<String, Object> field = createFieldData(fieldName, pageArgs);
        
		Map<String, Object> copyOfTagArgs = new HashMap<>();
		for(Map.Entry<Object, Object> entry : tagArgs.entrySet()) {
			String key = entry.getKey().toString();
			copyOfTagArgs.put(key, entry.getValue());
			body.setProperty(key, entry.getValue());
		}
		
		copyOfTagArgs.put("field", field);
		
		String bodyStr = "";
		if(body != null) {
			body.setProperty("field", field);
			bodyStr = ClosureUtil.toString(body);
		}
		//variables starting with _ will not be html escaped so the body html won't be converted like other variables
		copyOfTagArgs.put("_body", bodyStr); 
		
		return copyOfTagArgs;
	}

	/**
	 * 
	 * @param fieldName Is the argument like 'user.account.name' 
	 * @param pageArgs
	 * @return
	 */
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
        field.put("errorClass", field.get("error") != null ? "hasError" : "");
        String[] pieces = fieldName.split("\\.");
        Object pageArgValue = null;
        Object obj = pageArgs.get(pieces[0]);
        if (pieces.length > 1) {
            try {
                String path = fieldName.substring(fieldName.indexOf(".") + 1);
                pageArgValue = PropertyUtils.getProperty(obj, path);
            } catch (Exception e) {
                // if there is a problem reading the field we dont set any
                // value
            }
        } else {
        	pageArgValue = obj;
        }
        field.put("value", pageArgValue);
        
        field.put("flashOrValue", preferFirst(flashValue, pageArgValue));
        field.put("valueOrFlash", preferFirst(pageArgValue, flashValue));
        
		return field;
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

	private Object preferFirst(Object first, Object last) {
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
