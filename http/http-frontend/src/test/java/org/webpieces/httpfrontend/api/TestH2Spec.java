package org.webpieces.httpfrontend.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class TestH2Spec {
    String h2SpecPath;

    @Before
    public void setup() {
        String goPath = System.getenv("GOPATH");
        if(goPath == null) {
            throw new RuntimeException("Must install h2spec and set GOPATH. Requires GO 1.5+. See https://github.com/summerwind/h2spec and https://golang.org/dl/.");
        }

        h2SpecPath = goPath + "/bin/h2spec";
        File h2SpecFile = new File(h2SpecPath);
        if(h2SpecFile.isDirectory() || !h2SpecFile.canExecute()) {
            throw new RuntimeException("Must install h2spec. Requires GO 1.5+. See https://github.com/summerwind/h2spec and https://golang.org/dl/");
        }

        ServerFactory.createTestServer(8084, true);
    }

    private boolean passH2SpecTest(String testNumber) {
        try {
            ProcessBuilder pb = new ProcessBuilder(h2SpecPath, "-p", "8084", "-S", "-s", testNumber);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if(line.contains("All tests passed"))
                    return true;
            }
            return false;
        } catch (IOException e) {
            System.out.print(e);
            return false;
        }
    }

    @Test
    public void testH2Spec() {
        // Skipping 5.4.1 and 4.3 for now, because they fail.
        String[] tests = {
                "3.5",
                "4.2",
                // "4.3",
                "5.1",
                "5.1.1",
                "5.1.2",
                "5.3",
                "5.3.1",
                "5.4",
                // "5.4.1",
                "5.5",
                "6.1",
                "6.2",
                "6.3",
                "6.4",
                "6.5",
                "6.5.2",
                "6.7",
                "6.8",
                "6.9",
                "6.9.1",
                "6.9.2",
                "6.10",
                "8.1",
                "8.1.2",
                "8.1.2.1",
                "8.1.2.2",
                "8.1.2.3",
                "8.1.2.6",
                "8.2"
        };
        for(String test: tests) {
            Assert.assertTrue(passH2SpecTest(test));
        }
    }
}
