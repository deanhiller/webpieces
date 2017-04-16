package org.webpieces.router.api;

import java.util.function.Function;

import org.webpieces.router.impl.params.Meta;
import org.webpieces.router.impl.params.ParamTreeNode;

public interface EntityLookup {

	<T> boolean isManaged(Class<T> paramTypeToCreate);

	<T> T find(Meta paramTypeToCreate, ParamTreeNode tree, Function<Class<T>, T> beanCreate);

}
