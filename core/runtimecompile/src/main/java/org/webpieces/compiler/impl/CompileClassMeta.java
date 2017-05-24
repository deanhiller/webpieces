package org.webpieces.compiler.impl;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * Represent a application class
 */
public class CompileClassMeta {

	private static final Logger log = LoggerFactory.getLogger(CompileClassMeta.class);

	/**
	 * The fully qualified class name
	 */
	public String name;
	/**
	 * A reference to the java source file
	 */
	public VirtualFile javaFile;
	/**
	 * The Java source
	 */
	public String javaSource;
	/**
	 * The compiled byteCode
	 */
	public byte[] javaByteCode;
	/**
	 * The in JVM loaded class
	 */
	public Class<?> javaClass;
	/**
	 * Last time than this class was compiled
	 */
	public Long timestamp = 0L;
	/**
	 * Is this class compiled
	 */
	boolean compiled;

	private CompileConfig config;

	public CompileClassMeta(CompileConfig config) {
		this.config = config;
	}

	public CompileClassMeta(String name, VirtualFile javaFile, CompileConfig config) {
		this.name = name;
		this.javaFile = javaFile;
		this.config = config;
		this.refresh();
	}

	/**
	 * Need to refresh this class !
	 */
	public void refresh() {
		//does this ever happen where javaFile is null...?  class is deleted?
		//if (this.javaFile != null) {
		this.javaSource = this.javaFile.contentAsString(config.getFileEncoding());
		//}
		this.javaByteCode = null;
		this.compiled = false;
		this.timestamp = javaFile.lastModified();
	}

	/**
	 * Is this class already compiled but not defined ?
	 * 
	 * @return if the class is compiled but not defined
	 */
	public boolean isDefinable() {
		return compiled && javaClass != null;
	}

	public String getPackage() {
		int dot = name.lastIndexOf('.');
		return dot > -1 ? name.substring(0, dot) : "";
	}

	/**
	 * Compile the class from Java source
	 * 
	 * @return the bytes that comprise the class file
	 */
	public byte[] compile(CompilerWrapper compiler, ClassDefinitionLoader loader) {
		long start = System.currentTimeMillis();
		compiler.compile(new String[] { this.name }, loader);

		if(log.isTraceEnabled()) {
			long time = System.currentTimeMillis()-start;
			log.trace(()->time+"ms to compile class "+name);
		}

		return this.javaByteCode;
	}

	/**
	 * Unload the class
	 */
	public void uncompile() {
		this.javaClass = null;
	}

	/**
	 * Call back when a class is compiled.
	 * 
	 * @param code
	 *            The bytecode.
	 */
	public void compiled(byte[] code) {
		//if(log.isTraceEnabled())
		
		log.info("class now compiled="+name);
		javaByteCode = code;
		compiled = true;
		this.timestamp = this.javaFile.lastModified();
	}

	@Override
	public String toString() {
		return name + " (compiled:" + compiled + ")";
	}
}