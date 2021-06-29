package org.webpieces.util.exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;

public abstract class WebpiecesException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public WebpiecesException(String message) {
		super(message);
	}

	public WebpiecesException() {
	}

	public WebpiecesException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebpiecesException(Throwable cause) {
		super(cause);
	}
	
	public WebpiecesException clone(String message) {
		try {
			//best effort clone..DAMN checked exceptions yet again making us do crazy crap
			
			Constructor<? extends WebpiecesException> constructor = getClass().getConstructor(String.class, Throwable.class);
			return constructor.newInstance(message, this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//if we can't create, return original one..
			this.addSuppressed(e);
			return this;
		}
	}

}
