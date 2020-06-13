package org.webpieces.router.impl.hooks;

import org.webpieces.router.impl.loader.ProdClassForName;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdClassForName.class)
public interface ClassForName {

	Class<?> clazzForName(String moduleName);
}
