package org.webpieces.plugin.dto;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ElementType.TYPE})
public @interface Dto {

    /**
     * The class we create with the web application injector.  You most likely
     * want this class annotated with @Singleton but that's your choice
     */
    Class lookupClass();

    /**
     * The function we call on lookupClass to lookup the entity.  The function can
     * return CompletableFuture<Dto> OR Dto.  If it returns CompletableFuture<Dto>,
     * webpieces will make the call asynchronously and free up a thread while waiting
     * for the response
     */
    String function(); 

}
