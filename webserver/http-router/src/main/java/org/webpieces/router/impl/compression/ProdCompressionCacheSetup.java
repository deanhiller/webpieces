package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.security.Security;

public class ProdCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(ProdCompressionCacheSetup.class);
	
	private CompressionLookup lookup;
	private RouterConfig config;
	private MimeTypes mimeTypes;
	private List<String> encodings = new ArrayList<>();
	private FileUtil fileUtil;

	@Inject
	public ProdCompressionCacheSetup(CompressionLookup lookup, RouterConfig config, MimeTypes mimeTypes, FileUtil fileUtil) {
		this.lookup = lookup;
		this.config = config;
		this.mimeTypes = mimeTypes;
		encodings.add(config.getStartupCompression());
		this.fileUtil = fileUtil;
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
		String id = route.getStaticRouteId();
		File dir = config.getCachedCompressedDirectory();
		File routeCache = new File(dir, id);
		createDirectory(routeCache);
		
		File metaFile = new File(routeCache, "webpiecesMeta.properties");
		Properties p = load(metaFile);
		
		if(route.isFile()) {
			File file = new File(route.getFileSystemPath());
			log.info("setting up cache for file="+file);
			maybeAddFileToCache(p, file, new File(routeCache, file.getName()+".gz"), route.getPath());
			return;
		}

		File directory = new File(route.getFileSystemPath());
		log.info("setting up cache for directory="+directory);
		String urlPrefix = route.getPath();
		transferAndCompress(p, directory, routeCache, urlPrefix);
		
		store(metaFile, p);
	}

	private void store(File metaFile, Properties p) {
		try {
			FileOutputStream out = new FileOutputStream(metaFile);
			p.store(out, "file hashes for next time.  Single file format(key:urlPathOnly, value:hash), dir(key:urlPath+relativeFilePath, value:hash)");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Properties load(File metaFile) {
		try {
			Properties p = new Properties();
			if(!metaFile.exists())
				return p;
			p.load(new FileInputStream(metaFile));
			return p;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void transferAndCompress(Properties p, File directory, File destination, String urlPath) {
		File[] files = directory.listFiles();
		for(File f : files) {
			if(f.isDirectory()) {
				File newTarget = new File(destination, f.getName());
				createDirectory(newTarget);
				transferAndCompress(p, f, newTarget, urlPath+f.getName()+"/");
			} else {
				File newTarget = new File(destination, f.getName()+".gz");
				String path = urlPath+f.getName();
				maybeAddFileToCache(p, f, newTarget, path);
			}
		}
	}

	private void maybeAddFileToCache(Properties p, File src, File destination, String urlPath) {
		String name = src.getName();
		int indexOf = name.lastIndexOf(".");
		if(indexOf < 0)
			return; //do nothing
		String extension = name.substring(indexOf+1);
		
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, "application/octet-stream");
		Compression compression = lookup.createCompressionStream(encodings, extension, mimeType);
		if(compression == null)
			return;

		//before we do the below, do a quick timestamp check to avoid reading in the files when not necessary
		long lastModifiedSrc = src.lastModified();
		long lastModified = destination.lastModified();
		//if hash is not there, the user may have changed the url so need to recalculate new hashes for new keys
		//There is a test for this...
		String previousHash = p.getProperty(urlPath); 
		if(lastModified > lastModifiedSrc && previousHash != null)
			return; //no need to check anything as destination was written after this source file
		
		try {
				byte[] allData = fileUtil.readFileContents(urlPath, src);
				String hash = Security.hash(allData);
				
				if(previousHash != null) {
					if(!hash.equals(previousHash))
						throw new IllegalStateException("Your app modified the file="+src.getAbsolutePath()+" from the last release BUT"
								+ " you did not change the name of the file nor the url path meaning your customer will never get the new version"
								+ " until the cache expires which can be a month out.  (Modify the names of files/url path when changing them)\n"
								+ "existing compressed file="+destination+"\nprevious hash="+previousHash+" currentHash="+hash);
					log.info("Previous file is the same, no need to compress to="+destination);
					return;
				}

				//open, write, and close file with new data
				writeFile(destination, compression, allData, urlPath, src);
				//if file writing succeeded, set the hash
				p.setProperty(urlPath, hash);

				log.info("compressed "+src.length()+" bytes to="+destination.length()+" to file="+destination);
				
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeFile(File destination, Compression compression, byte[] allData, String urlPath, File src) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(destination);
		try(OutputStream compressionOut = compression.createCompressionStream(out)) 
		{
			fileUtil.writeFile(compressionOut, allData, urlPath, src);			
		}
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
