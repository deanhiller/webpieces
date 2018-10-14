package org.webpieces.plugins.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.plugins.properties.beans.BeanMeta;
import org.webpieces.plugins.properties.beans.BeanMetaData;
import org.webpieces.plugins.properties.beans.PropertyInfo;
import org.webpieces.router.api.ObjectStringConverter;
import org.webpieces.router.api.SimpleStorage;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;
import org.webpieces.router.impl.params.ObjectTranslator;

import com.google.common.collect.Lists;

@Singleton
public class PropertiesController {

	private MenuCreator menuCreator;
	private BeanMetaData beanMetaData;
	private SimpleStorage storage;
	private PropertiesConfig config;
	private ObjectTranslator objectTranslator;

	@Inject
	public PropertiesController(
			MenuCreator menuCreator, 
			BeanMetaData beanMetaData, 
			SimpleStorage storage, 
			PropertiesConfig config,
			ObjectTranslator objectTranslator
			
	) {
		this.menuCreator = menuCreator;
		this.beanMetaData = beanMetaData;
		this.storage = storage;
		this.config = config;
		this.objectTranslator = objectTranslator;
	}

	public Render main() {
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"categories", beanMetaData.getCategories(),
				"suffix", config.getInterfaceSuffixMatch());
	}

	public Render bean(String category, String name) {
		BeanMeta meta = beanMetaData.getBeanMeta(category, name);
		
		ArrayList<Object> params = Lists.newArrayList("menu", menuCreator.getMenu(),
				"beanMeta", meta,
				"category", category,
				"name", name);

		List<PropertyInfo> properties = meta.getProperties();
		for(PropertyInfo p : properties) {
			params.add(p.getName());
			Object value = getValue(p);
			String valueStr = objectTranslator.getConverterFor(value).objectToString(value);
			params.add(valueStr);
		}
		
		Object[] paramsArray = params.toArray(new Object[0]);
		return Actions.renderThis(paramsArray);
	}

	private Object getValue(PropertyInfo p) {
		Method method = p.getGetter();
		try {
			return method.invoke(p.getInjectee());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public Redirect postBean(String category, String name) {
		Map<String, List<String>> multiPartFields = Current.getContext().getRequest().multiPartFields;
		
		BeanMeta meta = beanMetaData.getBeanMeta(category, name);
		List<PropertyInfo> props = meta.getProperties();

		//first validate the strings are of the right type for each return type and gather error messages up
		//1. lookup converter.  if not exist, add validation error
		//2. convert and catch conversion exception if needed and add validation error
		//3. store all values while doing 1 and 2.  apply in next loop below
		List<ValueInfo> values = new ArrayList<>();
		for(PropertyInfo info : props) {
			if(info.isReadOnly())
				continue;
			
			Class<?> returnType = info.getGetter().getReturnType();
			ObjectStringConverter<?> converter = objectTranslator.getConverter(returnType);
			if(converter == null) {
				//since we checked before when loading, there should be a converter, or code was changed and we have new bug
				throw new RuntimeException("Odd, this shouldn't be possible, bug.  return type="+returnType.getName());
			}

			List<String> valueList = multiPartFields.get(info.getName());
			String value = valueList.get(0);
			try {
				Object objectValue = converter.stringToObject(value);
				values.add(new ValueInfo(info, objectValue));
			} catch(Exception e) {
				Current.validation().addError(info.getName(), "Converter="+converter.getClass().getName()+" cannot convert String to "+returnType.getName());
			}
			
		}
		
		if(Current.validation().hasErrors()) {
			RequestContext ctx = Current.getContext();
			ctx.moveFormParamsToFlash(new HashSet<>());
			ctx.getFlash().keep();
			ctx.getValidation().keep();
			return Actions.redirect(PropertiesRouteId.BEAN_ROUTE, 
					"category", category, 
					"name", name);			
		}
		
		//KISS: not doing rollback code so if one prop fails to set, it does not save to database
		//and is partially applied.  (ie. keep your setter code simple!!).
		
		//ALSO, we are taking an apply all properties type of approach to keep it simple for now.  in the future,
		//we could read all props and only call setXXX on changes (in case they wrote extra not idempotent type code so
		//that code does not run on accident)
		for(ValueInfo value : values) {
			updateProperty(value.getInfo(), value.getValue());
		}
		
		Current.flash().setMessage("Modified Bean '"+name+".class' and persisted to Database");
		Current.flash().keep();
		
		return Actions.redirect(PropertiesRouteId.MAIN_PROPERTIES);
	}

	private void updateProperty(PropertyInfo info, Object objectValue) {
		try {
			info.getSetter().invoke(info.getInjectee(), objectValue);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
