package org.webpieces.webserver.impl;

import java.io.*;
import java.util.Properties;

public class TestClass {
    public static String readVersion() {
        Properties properties = new Properties();
        try (InputStream stream = ResponseCreator.class.getResourceAsStream("/version.properties")) {
            properties.load(stream);
            return properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        System.out.println("webpieces/"+readVersion());
    }
}
