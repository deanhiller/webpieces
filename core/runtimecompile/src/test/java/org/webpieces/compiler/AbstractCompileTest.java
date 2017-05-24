package org.webpieces.compiler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.compiler.impl.CompileOnDemandImpl;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.VirtualFileImpl;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public abstract class AbstractCompileTest {

	protected static final Logger log = LoggerFactory.getLogger(AbstractCompileTest.class);
	private static final String property = System.getProperty("java.io.tmpdir");
	private File javaFileCacheDir = new File(property + "/cachedJavaFiles");
	private static final String filePath = System.getProperty("user.dir");
	protected static final File myCodePath = new File(filePath + "/src/test/java");
	private static final File myResourcePath = new File(filePath + "/src/test/changedJavaFiles");
	protected CompileOnDemand compiler;
	private boolean filesMoved;
	protected File byteCodeCacheDir = new File(property+"/bytecode");

	@Before
	public void setUp() {		
		log.info("storing bytecode cache in="+byteCodeCacheDir.getAbsolutePath());
		log.info("running tests from user.dir="+filePath);
		
		// clear out the bytecode cache (maybe not every time?)
		clearByteCodeCache(byteCodeCacheDir);

		CompileConfig config = createCompileConfig();

		compiler = new CompileOnDemandImpl(config, getPackageFilter());
	}

	protected CompileConfig createCompileConfig() {
		List<VirtualFile> arrayList = new ArrayList<>();
		arrayList.add(new VirtualFileImpl(myCodePath));
		CompileConfig config = new CompileConfig(arrayList, new VirtualFileImpl(byteCodeCacheDir));
		return config;
	}

	@After
	public void tearDown() {
		if(filesMoved)
			resetFiles();
	}
	private void clearByteCodeCache(File path) {
		if (!path.exists())
			return;
		for (File file : path.listFiles()) {
			file.delete();
		}
	}

	protected abstract String getPackageFilter();

	@SuppressWarnings("rawtypes")
	protected Object invokeMethod(Class c, String method) {
		try {
			return invokeMethodImpl(c, method);
		} catch (Exception e) {
			throw new RuntimeException("exception", e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected int invokeMethodReturnInt(Class c, String method) {
		try {
			return (Integer) invokeMethodImpl(c, method);
		} catch (Exception e) {
			throw new RuntimeException("exception", e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object invokeMethodImpl(Class c, String method)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			if (method.equals(m.getName())) {
				Object obj = c.newInstance();
				return m.invoke(obj);
			}
		}
		throw new IllegalStateException("method=" + method + " not found");
	}

	protected void resetFiles() {
		File testDir = getTestCacheDir();

		String packageFilter = getPackageFilter();
		String path = packageFilter.replace('.', '/');

		File existingDir = new File(myCodePath, path);
		copyFiles(testDir, existingDir);
	}

	protected void cacheAndMoveFiles() {
		if (!javaFileCacheDir.exists())
			javaFileCacheDir.mkdir();

		File testDir = getTestCacheDir();
		if (!testDir.exists())
			testDir.mkdirs();

		String packageFilter = getPackageFilter();
		String path = packageFilter.replace('.', '/');

		File existingDir = new File(myCodePath, path);
		copyFiles(existingDir, testDir);

		File resourceDir = new File(myResourcePath, path);
		copyFiles(resourceDir, existingDir);
		
		filesMoved = true;
	}

	private void copyFiles(File existingDir, File testDir) {
		try {
			for (File from : existingDir.listFiles()) {
				File toFile = new File(testDir, from.getName());
				if(toFile.exists())
					toFile.delete();
				Files.copy(from.toPath(), toFile.toPath());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File getTestCacheDir() {
		File testDir = new File(javaFileCacheDir, getPackageFilter());
		return testDir;
	}

}
