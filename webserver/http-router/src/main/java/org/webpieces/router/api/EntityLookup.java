package org.webpieces.router.api;

import java.util.function.Function;

import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.params.ParamTreeNode;

public interface EntityLookup {

	<T> boolean isManaged(Class<T> paramTypeToCreate);

	<T> T find(Class<T> paramTypeToCreate, ParamTreeNode tree, ObjectTranslator translator, Function<Class<T>, T> beanCreate);

}
