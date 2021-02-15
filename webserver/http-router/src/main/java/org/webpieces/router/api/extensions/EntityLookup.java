package org.webpieces.router.api.extensions;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.router.impl.params.ParamTreeNode;

public interface EntityLookup {

	<T> boolean isManaged(Class<T> paramTypeToCreate);

	<T> CompletableFuture<T> find(Meta paramTypeToCreate, ParamTreeNode tree, Function<Class<T>, T> beanCreate);

}
