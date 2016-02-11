/*
 * @(#)MemoryHandler.java	1.24 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.playorm.util.logging;

import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * <tt>Handler</tt> that buffers requests in a circular buffer in memory.
 * <p>
 * Normally this <tt>Handler</tt> simply stores incoming <tt>LogRecords</tt>
 * into its memory buffer and discards earlier records.  This buffering
 * is very cheap and avoids formatting costs.  On certain trigger
 * conditions, the <tt>MemoryHandler</tt> will push out its current buffer
 * contents to a target <tt>Handler</tt>, which will typically publish
 * them to the outside world.
 * <p>
 * There are three main models for triggering a push of the buffer:
 * <ul>
 * <li>
 * An incoming <tt>LogRecord</tt> has a type that is greater than
 * a pre-defined level, the <tt>pushLevel</tt>.
 * <li>
 * An external class calls the <tt>push</tt> method explicitly. 
 * <li>
 * A subclass overrides the <tt>log</tt> method and scans each incoming
 * <tt>LogRecord</tt> and calls <tt>push</tt> if a record matches some
 * desired criteria.
 * </ul>
 * <p>
 * <b>Configuration:</b>
 * By default each <tt>MemoryHandler</tt> is initialized using the following
 * LogManager configuration properties.  If properties are not defined
 * (or have invalid values) then the specified default values are used.
 * If no default value is defined then a RuntimeException is thrown.
 * <ul>
 * <li>   java.util.logging.MemoryHandler.level 
 *	  specifies the level for the <tt>Handler</tt>
 *        (defaults to <tt>Level.ALL</tt>).
 * <li>   java.util.logging.MemoryHandler.filter
 *	  specifies the name of a <tt>Filter</tt> class to use
 *	  (defaults to no <tt>Filter</tt>).
 * <li>   java.util.logging.MemoryHandler.size 
 *	  defines the buffer size (defaults to 1000).
 * <li>   java.util.logging.MemoryHandler.push
 *	  defines the <tt>pushLevel</tt> (defaults to <tt>level.SEVERE</tt>). 
 * <li>   java.util.logging.MemoryHandler.target
 *	  specifies the name of the target <tt>Handler </tt> class.
 *	  (no default).
 * </ul>
 *
 * @version 1.24, 12/19/03
 * @since 1.4
 */

public class MemoryHandler extends Handler {
    private static final int DEFAULT_SIZE = 1000;
    private Level pushLevel;
    private int size;
    private Handler target;
    private LogRecord[] buffer;
    private int start, count;

    // Private method to configure a ConsoleHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private void configure() {
        LogManager manager = LogManager.getLogManager();
	String cname = getClass().getName();

	pushLevel = getLevelProperty(manager, cname +".push", Level.SEVERE);
	size = getIntProperty(manager, cname + ".size", DEFAULT_SIZE);
	if (size <= 0) {
	    size = DEFAULT_SIZE;
	}
    
	setLevel(getLevelProperty(manager, cname +".level", Level.ALL));
	setFilter(getFilterProperty(manager, cname +".filter", null));
	setFormatter(getFormatterProperty(manager, cname +".formatter", new SimpleFormatter()));
    }

    static Level getLevelProperty(LogManager mgr, String name, Level defaultValue) {
        String val = mgr.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Level.parse(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
        }
    
    static Filter getFilterProperty(LogManager mgr, String name, Filter defaultValue) {
        String val = mgr.getProperty(name);
        try {
            if (val != null) {
            Class clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
        }
    
    static Formatter getFormatterProperty(LogManager mgr, String name, Formatter defaultValue) {
        String val = mgr.getProperty(name);
        try {
            if (val != null) {
            Class clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue;
        }
    
    static int getIntProperty(LogManager mgr, String name, int defaultValue) {
        String val = mgr.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
        }
    
    /**
     * Create a <tt>MemoryHandler</tt> and configure it based on
     * <tt>LogManager</tt> configuration properties.
     */
    public MemoryHandler() {
	//sealed = false;
	configure();
	//sealed = true;

	String name = "???";
	try {
            LogManager manager = LogManager.getLogManager();
            String cname = getClass().getName();
	    name = manager.getProperty(cname+".target");
	    Class clz = ClassLoader.getSystemClassLoader().loadClass(name);
	    target = (Handler) clz.newInstance();
	} catch (Exception ex) {
	    throw new RuntimeException("MemoryHandler can't load handler \"" + name + "\"" , ex);
	}
	init();
    }

    // Initialize.  Size is a count of LogRecords.
    private void init() {
	buffer = new LogRecord[size];
	start = 0;
	count = 0;
    }

    /**
     * Create a <tt>MemoryHandler</tt>.
     * <p>
     * The <tt>MemoryHandler</tt> is configured based on <tt>LogManager</tt>
     * properties (or their default values) except that the given <tt>pushLevel</tt>
     * argument and buffer size argument are used.
     *     
     * @param target  the Handler to which to publish output.
     * @param size    the number of log records to buffer (must be greater than zero)
     * @param pushLevel  message level to push on
     *
     * @throws IllegalArgumentException is size is <= 0
     */
    public MemoryHandler(Handler target, int size, Level pushLevel) {
	if (target == null || pushLevel == null) {
	    throw new NullPointerException();
	}
	if (size <= 0) {
	    throw new IllegalArgumentException();
	}
	//sealed = false;
	configure();
	//sealed = true;
	this.target = target;
	this.pushLevel = pushLevel;
	this.size = size;
	init();
    }

    /**
     * Store a <tt>LogRecord</tt> in an internal buffer.
     * <p>
     * If there is a <tt>Filter</tt>, its <tt>isLoggable</tt>
     * method is called to check if the given log record is loggable.
     * If not we return.  Otherwise the given record is copied into
     * an internal circular buffer.  Then the record's level property is
     * compared with the <tt>pushLevel</tt>. If the given level is
     * greater than or equal to the <tt>pushLevel</tt> then <tt>push</tt>
     * is called to write all buffered records to the target output
     * <tt>Handler</tt>.
     * 
     * @param  record  description of the log event. A null record is
     *                 silently ignored and is not published
     */
    @Override
    public synchronized void publish(LogRecord record) {
	if (!isLoggable(record)) {
	    return;
	}
    
    //make sure the source class and method name is loaded here.  Otherwise, the ConsoleHandler
    //or FileHandler will push the wrong one
    record.getSourceClassName();
    
	int ix = (start+count)%buffer.length;
	buffer[ix] = record;
	if (count < buffer.length) {
	    count++;
	} else {
	    start++;
	}
	if (record.getLevel().intValue() >= pushLevel.intValue()) {
	    push();
	}
    }

    /**
     * Push any buffered output to the target <tt>Handler</tt>.
     * <p>
     * The buffer is then cleared.
     */
    public synchronized void push() {
	for (int i = 0; i < count; i++) {
	    int ix = (start+i)%buffer.length;
	    LogRecord record = buffer[ix];
	    target.publish(record);
	}
	// Empty the buffer.
	start = 0;
	count = 0;
    }

    /**
     * Causes a flush on the target <tt>Handler</tt>.
     * <p>
     * Note that the current contents of the <tt>MemoryHandler</tt>
     * buffer are <b>not</b> written out.  That requires a "push".
     */
    public void flush() {
	target.flush();
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * This will also close the target <tt>Handler</tt>.
     *
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void close() {
	target.close();
	setLevel(Level.OFF);
    }

    /** 
     * Set the <tt>pushLevel</tt>.  After a <tt>LogRecord</tt> is copied 
     * into our internal buffer, if its level is greater than or equal to
     * the <tt>pushLevel</tt>, then <tt>push</tt> will be called.
     *
     * @param newLevel the new value of the <tt>pushLevel</tt>
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void setPushLevel(Level newLevel) {
	if (newLevel == null) {
	    throw new NullPointerException();
	}
	LogManager manager = LogManager.getLogManager();
        //checkAccess();
	pushLevel = newLevel;
    }

    /** 
     * Get the <tt>pushLevel</tt>.
     *
     * @return the value of the <tt>pushLevel</tt>
     */
    public synchronized Level getPushLevel() {
	return pushLevel;
    }

    /**
     * Check if this <tt>Handler</tt> would actually log a given 
     * <tt>LogRecord</tt> into its internal buffer.
     * <p>
     * This method checks if the <tt>LogRecord</tt> has an appropriate level and 
     * whether it satisfies any <tt>Filter</tt>.  However it does <b>not</b>
     * check whether the <tt>LogRecord</tt> would result in a "push" of the
     * buffer contents. It will return false if the <tt>LogRecord</tt> is Null.
     * <p>
     * @param record  a <tt>LogRecord</tt>
     * @return true if the <tt>LogRecord</tt> would be logged.
     *
     */
    public boolean isLoggable(LogRecord record) {
	return super.isLoggable(record);
    }
}
