package org.webpieces.ctx.api;

/**
 * The interface applications bind a singleton to so it's globally available in the server's context
 *
 * @deprecated anything marked @Singleton is ApplicationContext so this is not useful except creates a mess
 * as people use this instead of guice object singletons which should be used instead
 */
@Deprecated
public interface ApplicationContext {

}
