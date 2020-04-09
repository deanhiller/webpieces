package org.webpieces.router.impl.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.router.impl.routers.EStaticRouter;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.security.Security;

public class ProdCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(ProdCompressionCacheSetup.class);
	
	private CompressionLookup lookup;
	private RouterConfig config;
	private MimeTypes mimeTypes;
	private List<String> encodings = new ArrayList<>();
	private FileUtil fileUtil;
	private Map<String, FileMeta> pathToFileMeta = new HashMap<>();

	@Inject
	public ProdCompressionCacheSetup(CompressionLookup lookup, RouterConfig config, MimeTypes mimeTypes, FileUtil fileUtil) {
		this.lookup = lookup;
		this.config = config;
		this.mimeTypes = mimeTypes;
		encodings.add(config.getStartupCompression());
		this.fileUtil = fileUtil;
	}
	
	public void setupCache(List<EStaticRouter> staticRoutes) {
		if(config.getCachedCompressedDirectory() == null) {
			log.info("NOT setting up compressed cached directory so performance will not be as good");
			return;
		}
		
		log.info("setting up compressed cache directories");
		for(EStaticRouter route : staticRoutes) {
			if(!route.isOnClassPath())
				createCache(route);
		}
		log.info("all cached directories setup");
	}

	private void createCache(EStaticRouter route) {
		File routeCache = route.getTargetCacheLocation();
		createDirectory(routeCache);
		
		File metaFile = FileFactory.newFile(routeCache, "webpiecesMeta.properties");
		Properties properties = load(metaFile);
		
		boolean modified;
		if(route.getFileSystemPath().isFile()) {
			VirtualFile file = route.getFileSystemPath();
			log.info("setting up cache for file="+file);
			File destination = FileFactory.newFile(routeCache, file.getName()+".gz");
			modified = maybeAddFileToCache(properties, file, destination, route.getFullPath());
		} else {
			VirtualFile directory = route.getFileSystemPath();
			log.info("setting up cache for directory="+directory);
			String urlPrefix = route.getFullPath();
			modified = transferAndCompress(properties, directory, routeCache, urlPrefix);
		}

		route.setHashMeta(properties);
		if(modified)
			store(metaFile, properties);
	}

	private void store(File metaFile1, Properties p) {
		try(FileOutputStream out = new FileOutputStream(metaFile1)) {
			p.store(out, "file hashes for next time.  Single file format(key:urlPathOnly, value:hash), directory format(key:urlPath+relativeFilePath, value:hash)");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Properties load(File metaFile) {
		Properties p = new Properties();
		if(!metaFile.exists())
			return p;
		
		try(FileInputStream in = new FileInputStream(metaFile)) {
			p.load(in);
			return p;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean transferAndCompress(Properties p, VirtualFile directory, File destination, String urlPath) {
		List<VirtualFile> files = directory.list();
		boolean modified = false;
		for(VirtualFile f : files) {
			if(f.isDirectory()) {
				File newTarget = FileFactory.newFile(destination, f.getName());
				createDirectory(newTarget);
				boolean changed = transferAndCompress(p, f, newTarget, urlPath+f.getName()+"/");
				if(changed)
					modified = true;
			} else {
				File newTarget = FileFactory.newFile(destination, f.getName()+".gz");
				String path = urlPath+f.getName();
				boolean changed = maybeAddFileToCache(p, f, newTarget, path);
				if(changed)
					modified = true;
			}
		}
		return modified;
	}

	private boolean maybeAddFileToCache(Properties properties, VirtualFile src, File destination, String urlPath) {
		String name = src.getName();
		int indexOf = name.lastIndexOf(".");
		if(indexOf < 0) {
			pathToFileMeta.put(urlPath, new FileMeta());
			return false; //do nothing
		}
		String extension = name.substring(indexOf+1);
		
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, "application/octet-stream");
		Compression compression = lookup.createCompressionStream(encodings, extension, mimeType);
		if(compression == null) {
			pathToFileMeta.put(urlPath, new FileMeta());
			return false;
		}

		//before we do the below, do a quick timestamp check to avoid reading in the files when not necessary
		long lastModifiedSrc = src.lastModified();
		long lastModified = destination.lastModified();
		//if hash is not there, the user may have changed the url so need to recalculate new hashes for new keys
		//There is a test for this...
		String previousHash = properties.getProperty(urlPath); 
		if(lastModified > lastModifiedSrc && previousHash != null) {
			if(log.isDebugEnabled())
				log.debug("timestamp later than src so skipping writing to="+destination);
			
			pathToFileMeta.put(urlPath, new FileMeta(previousHash));
			return false; //no need to check anything as destination was written after this source file
		}
		
		try {
				byte[] allData = fileUtil.readFileContents(urlPath, src);
				String hash = Security.hash(allData);
				
				if(previousHash != null) {
					if(hash.equals(previousHash)) {
						if(!destination.exists())
							throw new IllegalStateException("Previously existing file is missing="+destination+" Your file cache was "
									+ "corrupted.  You will need to delete the whole cache directory");

						log.info("Previous file is the same, no need to compress to="+destination+" hash="+hash);
						pathToFileMeta.put(urlPath, new FileMeta(previousHash));
						return false;
					}
				}

				//open, write, and close file with new data
				writeFile(destination, compression, allData, urlPath, src);
				//if file writing succeeded, set the hash
				properties.setProperty(urlPath, hash);
				
				FileMeta existing = pathToFileMeta.get(urlPath);
				if(existing != null)
					throw new IllegalStateException("this urlpath="+urlPath+" is referencing two files.  hash1="+existing.getHash()+" hash2="+hash
							+"  You should search your logs for this hash");
					
				pathToFileMeta.put(urlPath, new FileMeta(hash));
				
				log.info("compressed "+src.length()+" bytes to="+destination.length()+" to file="+destination+" hash="+hash);
				return true;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeFile(File destination, Compression compression, byte[] allData, String urlPath, VirtualFile src) throws FileNotFoundException, IOException {
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

	@Override
	public FileMeta relativeUrlToHash(String path) {
		return pathToFileMeta.get(path);
	}
}
