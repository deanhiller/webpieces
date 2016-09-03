package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(CompressionCacheSetup.class);
	private CompressionLookup lookup;
	private RouterConfig config;
	private MimeTypes mimeTypes;
	private List<String> encodings = new ArrayList<>();

	@Inject
	public CompressionCacheSetup(CompressionLookup lookup, RouterConfig config, MimeTypes mimeTypes) {
		this.lookup = lookup;
		this.config = config;
		this.mimeTypes = mimeTypes;
		encodings.add(config.getStartupCompression());
	}
	
	public void setupCache(List<StaticRoute> staticRoutes) {
		if(config.getCachedCompressedDirectory() == null) {
			log.info("NOT setting up compressed cached directory so performance will not be as good");
			return;
		}
		
		log.info("setting up compressed cache directories");
		for(StaticRoute route : staticRoutes) {
			createCache(route);
		}
		log.info("all cached directories setup");
	}

	private void createCache(StaticRoute route) {
		int id = route.getStaticRouteId();
		File dir = config.getCachedCompressedDirectory();
		File routeCache = new File(dir, id+"");
		createDirectory(routeCache);

		if(route.isFile()) {
			File file = new File(route.getFileSystemPath());
			log.info("setting up cache for file="+file);
			maybeAddFileToCache(file, new File(routeCache, file.getName()));
			return;
		}

		File directory = new File(route.getFileSystemPath());
		log.info("setting up cache for directory="+directory);
		transferAndCompress(directory, routeCache);
	}

	private void transferAndCompress(File directory, File destination) {
		File[] files = directory.listFiles();
		for(File f : files) {
			File newTarget = new File(destination, f.getName());
			if(f.isDirectory()) {
				createDirectory(newTarget);
				transferAndCompress(f, newTarget);
			} else {
				maybeAddFileToCache(f, newTarget);
			}
		}
	}

	private void maybeAddFileToCache(File src, File destination) {
		String name = src.getName();
		int indexOf = name.indexOf(".");
		if(indexOf < 0)
			return; //do nothing
		String extension = name.substring(indexOf+1);
		
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, "application/octet-stream");
		Compression compression = lookup.createCompressionStream(encodings, extension, mimeType);
		if(compression == null)
			return;

		try (FileOutputStream out = new FileOutputStream(destination);
			 OutputStream compressionOut = compression.createCompressionStream(out);
			 FileInputStream in = new FileInputStream(src)) {
			
				byte[] data = new byte[20000];
				int read;
				while((read = in.read(data)) > 0) {
					compressionOut.write(data, 0, read);
				}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("compressed "+src.length()+" bytes to="+destination.length()+" to file="+destination);
	}

	private void createDirectory(File directoryToCreate) {
		if(directoryToCreate.exists()) {
			if(!directoryToCreate.isDirectory())
				throw new RuntimeException("File="+directoryToCreate+" is NOT a directory and we need a directory there...perhaps"
						+ " delete the cache and restart the server as something is corrupt");
			return;
		}
		
		boolean success = directoryToCreate.mkdirs();
		if(!success)
			throw new RuntimeException("Could not create cache directory="+directoryToCreate);
	}
}
