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
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompileTest {

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractCompileTest.class);
	private final File tmpDirectory = FileFactory.newCacheLocation("webpieces/AbstractCompileTest/"+getClass().getSimpleName());
	
	private File javaFileCacheDir = FileFactory.newFile(tmpDirectory, "cachedJavaFiles");
	protected static final File MY_CODE_PATH = FileFactory.newBaseFile("src/test/java");
	private static final File myResourcePath = FileFactory.newBaseFile("src/test/changedJavaFiles");
	protected CompileOnDemand compiler;
	private boolean filesMoved;
	protected File byteCodeCacheDir = FileFactory.newFile(tmpDirectory, "bytecode");

	@Before
	public void setUp() {		
		LOG.info("storing bytecode cache in="+byteCodeCacheDir.getAbsolutePath());
		LOG.info("running tests from user.dir="+FileFactory.getBaseWorkingDir());
		
		// clear out the bytecode cache (maybe not every time?)
		clearByteCodeCache(byteCodeCacheDir);

		CompileConfig config = createCompileConfig();

		compiler = new CompileOnDemandImpl(config, getPackageFilter());
	}

	protected CompileConfig createCompileConfig() {
		List<VirtualFile> arrayList = new ArrayList<>();
		arrayList.add(new VirtualFileImpl(MY_CODE_PATH));
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
			throw SneakyThrow.sneak(e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected int invokeMethodReturnInt(Class c, String method) {
		try {
			return (Integer) invokeMethodImpl(c, method);
		} catch (Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object invokeMethodImpl(Class c, String method)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Method[] methods = c.getMethods();
		for (Method m : methods) {
			if (method.equals(m.getName())) {
				Object obj = c.getDeclaredConstructor().newInstance();
				return m.invoke(obj);
			}
		}
		throw new IllegalStateException("method=" + method + " not found");
	}

	protected void resetFiles() {
		File testDir = getTestCacheDir();

		String packageFilter = getPackageFilter();
		String path = packageFilter.replace('.', File.separatorChar);

		File existingDir = FileFactory.newFile(MY_CODE_PATH, path);
		copyFiles(testDir, existingDir);
	}

	protected void cacheAndMoveFiles() {
		if (!javaFileCacheDir.exists())
			javaFileCacheDir.mkdir();

		File testDir = getTestCacheDir();
		if (!testDir.exists())
			testDir.mkdirs();

		String packageFilter = getPackageFilter();
		String path = packageFilter.replace('.', File.separatorChar);

		File existingDir = FileFactory.newFile(MY_CODE_PATH, path);
		copyFiles(existingDir, testDir);

		File resourceDir = FileFactory.newFile(myResourcePath, path);
		copyFiles(resourceDir, existingDir);
		
		filesMoved = true;
	}

	private void copyFiles(File existingDir, File testDir) {
		try {
			for (File from : existingDir.listFiles()) {
				File toFile = FileFactory.newFile(testDir, from.getName());
				if(toFile.exists())
					toFile.delete();
				Files.copy(from.toPath(), toFile.toPath());
				
				//Windows sucks and we have to update the file lastModified ourselves
				toFile.setLastModified(System.currentTimeMillis());
			}
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private File getTestCacheDir() {
		File testDir = FileFactory.newFile(javaFileCacheDir, getPackageFilter());
		return testDir;
	}

}
