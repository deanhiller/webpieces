package org.webpieces.plugin.secure.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugin.backend.menu.MenuCreator;
import org.webpieces.plugin.secure.properties.beans.BeanMeta;
import org.webpieces.plugin.secure.properties.beans.BeanMetaData;
import org.webpieces.plugin.secure.properties.beans.KeyUtil;
import org.webpieces.plugin.secure.properties.beans.PropertyInfo;
import org.webpieces.plugin.secure.properties.beans.PropertyInvoker;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.util.exceptions.SneakyThrow;

import com.google.common.collect.Lists;

@Singleton
public class PropertiesController {

	private MenuCreator menuCreator;
	private BeanMetaData beanMetaData;
	private SimpleStorage storage;
	private PropertiesConfig config;
	private PropertyInvoker invoker;

	@Inject
	public PropertiesController(
			MenuCreator menuCreator, 
			BeanMetaData beanMetaData, 
			SimpleStorage storage, 
			PropertiesConfig config,
			PropertyInvoker invoker
	) {
		this.menuCreator = menuCreator;
		this.beanMetaData = beanMetaData;
		this.storage = storage;
		this.config = config;
		this.invoker = invoker;
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
				"name", name,
				"thisServerOnly", false);

		List<PropertyInfo> properties = meta.getProperties();
		for(PropertyInfo p : properties) {
			params.add(p.getName());
			String valueStr = invoker.readPropertyAsString(p);
			params.add(valueStr);
		}
		
		Object[] paramsArray = params.toArray(new Object[0]);
		return Actions.renderThis(paramsArray);
	}

	public Redirect postBean(String category, String name, boolean thisServerOnly) throws InterruptedException, ExecutionException {
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
			
			List<String> valueList = multiPartFields.get(info.getName());
			String valueAsString = valueList.get(0);

			Class<?> returnType = info.getGetter().getReturnType();
			ObjectStringConverter<?> converter = invoker.fetchConverter(info);

			try {
				Object objectValue = converter.stringToObject(valueAsString);
				values.add(new ValueInfo(info, objectValue, valueAsString));
			} catch(Exception e) {
				Current.validation().addError(info.getName(), "Converter="+converter.getClass().getName()+" cannot convert String to "+returnType.getName());
			}
			
		}

		RequestContext ctx = Current.getContext();
		if(Current.validation().hasErrors()) {
			ctx.moveFormParamsToFlash(new HashSet<>());
			ctx.getFlash().keep(true);
			ctx.getValidation().keep(true);
			return Actions.redirect(PropertiesRouteId.BEAN_ROUTE,
					"category", category, 
					"name", name);			
		}

		ctx.getValidation().keep(false);

		//KISS: not doing rollback code so if one prop fails to set, it does not save to database
		//and is partially applied.  (ie. keep your setter code simple!!).
		
		//ALSO, we are taking an apply all properties type of approach to keep it simple for now.  in the future,
		//we could read all props and only call setXXX on changes (in case they wrote extra not idempotent type code so
		//that code does not run on accident)
		Map<String, String> propertiesToSaveToDatabase = new HashMap<>();
		for(ValueInfo value : values) {
			updateProperty(value.getInfo(), value.getValue());

			String key = KeyUtil.formKey(category, name, value.getInfo().getName());
			propertiesToSaveToDatabase.put(key, value.getValueAsString());
		}

		if(thisServerOnly) {
			Current.flash().setMessage("Modified Bean '"+name+".class' ONLY on this server in-memory.  (Changes not applied to database for restarts)");
			Current.flash().keep();
		} else {
			XFuture<Void> future = storage.save(KeyUtil.PLUGIN_PROPERTIES_KEY, propertiesToSaveToDatabase);
			future.get(); //synchronously wait in case it fails so user is told it failed to save to database
			
			Current.flash().setMessage("Modified Bean '"+name+".class' and persisted to Database");
			Current.flash().keep();
		}
		
		return Actions.redirect(PropertiesRouteId.MAIN_PROPERTIES);
	}

	private void updateProperty(PropertyInfo info, Object objectValue) {
		try {
			info.getSetter().invoke(info.getInjectee(), objectValue);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw SneakyThrow.sneak(e);
		}
	}
}
