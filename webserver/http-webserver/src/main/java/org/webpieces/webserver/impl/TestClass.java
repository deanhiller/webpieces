package org.webpieces.webserver.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestClass {
    public static String readVersion() {
        try {
            String filePath = new File("").getAbsolutePath();
            BufferedReader reader = new BufferedReader(new FileReader(filePath+"/webserver/http-webserver/output/resources/version.properties"));
            String line = reader.readLine();
            String seg[] = line.split("=");
            String version = seg[seg.length-1];
            return version;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null";
    }

    public static void main(String args[]) {
        System.out.println("webpieces/"+readVersion());
    }
}
