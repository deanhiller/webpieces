package org.webpieces.compiler.impl;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

public class WebpiecesFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private Map<String, WebpiecesJavaClassObject> objects = new HashMap<>();

    public WebpiecesFileManager(StandardJavaFileManager manager) {
        super(manager);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name) {
                byte[] b = objects.get(name).getBytes();
                return super.defineClass(name, b, 0, b.length);
            }
        };
    }

    public byte[] getBytes(String name) {
        return objects.get(name).getBytes();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaFileObject javaFileForOutput = fileManager.getJavaFileForOutput(location, name, kind, sibling);
        objects.put(name, new WebpiecesJavaClassObject(javaFileForOutput, kind));
        return objects.get(name);
    }

}
