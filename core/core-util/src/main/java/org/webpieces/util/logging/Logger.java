package org.webpieces.util.logging;

import java.util.function.Supplier;

public class Logger {

	private org.slf4j.Logger logger;

	public Logger(org.slf4j.Logger logger) {
		this.logger = logger;
	}

    /**
     * Return the name of this <code>Logger</code> instance.
     * @return name of this logger instance 
     */
    public String getName() {
    	return logger.getName();
    }

    /**
     * Is the logger instance enabled for the TRACE level?
     *
     * @return True if this Logger is enabled for the TRACE level,
     *         false otherwise.
     */
    public boolean isTraceEnabled() {
    	return logger.isTraceEnabled();
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public void trace(Supplier<String> msg) {
    	if(isTraceEnabled())
    		logger.trace(msg.get());
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @since 1.4
     */
    public void trace(Supplier<String> msg, Throwable t) {
    	if(isTraceEnabled())
    		logger.trace(msg.get(), t);    	
    }

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    public boolean isDebugEnabled() {
    	return logger.isDebugEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public void debug(Supplier<String> msg) {
    	if(isDebugEnabled())
    		logger.debug(msg.get());
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void debug(Supplier<String> msg, Throwable t) {
    	if(isDebugEnabled())
    		logger.debug(msg.get(), t);    	
    }

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    public boolean isInfoEnabled() {
    	return logger.isInfoEnabled();
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public void info(String msg) {
    	logger.info(msg);
    }

    public void info(Supplier<String> msg) {
    	if(isInfoEnabled())
    		logger.info(msg.get());
    }

    public void info(String msg, Object... arguments) {
        logger.info(msg, arguments);
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void info(String msg, Throwable t) {
    	logger.info(msg, t);
    }
    
    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void info(Supplier<String> msg, Throwable t) {
    	if(isInfoEnabled())
    		logger.info(msg.get(), t);
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    public boolean isWarnEnabled() {
    	return logger.isWarnEnabled();
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg) {
    	logger.warn(msg);
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void warn(String msg, Throwable t) {
    	logger.warn(msg, t);
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    public boolean isErrorEnabled() {
    	return logger.isErrorEnabled();
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public void error(String msg) {
    	logger.error(msg);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void error(String msg, Throwable t) {
    	logger.error(msg, t);
    }

}
