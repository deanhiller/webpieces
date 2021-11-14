package org.webpieces.router.api.extensions;

import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import org.webpieces.router.impl.params.ParamTreeNode;

public interface EntityLookup {

	<T> boolean isManaged(Class<T> paramTypeToCreate);

	<T> XFuture<T> find(Meta paramTypeToCreate, ParamTreeNode tree, Function<Class<T>, T> beanCreate);

}
