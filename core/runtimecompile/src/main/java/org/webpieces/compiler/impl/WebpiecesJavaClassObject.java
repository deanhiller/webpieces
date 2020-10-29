package org.webpieces.compiler.impl;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WebpiecesJavaClassObject extends SimpleJavaFileObject {
    protected final JavaFileObject javaFileObject;
    protected final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    protected WebpiecesJavaClassObject(JavaFileObject javaFileObject, Kind kind) {
        super(javaFileObject.toUri(), kind);
        this.javaFileObject = javaFileObject;
    }

    public byte[] getBytes() {
        return stream.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new TreeOutputStream(javaFileObject.openOutputStream(), stream);
    }
}