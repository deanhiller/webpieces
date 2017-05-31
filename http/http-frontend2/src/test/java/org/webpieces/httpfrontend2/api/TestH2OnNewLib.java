package org.webpieces.httpfrontend2.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

@RunWith(Parameterized.class)
public class TestH2OnNewLib {
	private static final Logger log = LoggerFactory.getLogger(TestH2OnNewLib.class);

    String h2SpecPath;
    int port;
	private String testName;

	@SuppressWarnings("rawtypes")
	@Parameterized.Parameters
	public static Collection bothServers() {
        // Skipping 5.4.1 and 4.3 for now, because they fail.
        String[] tests = {
                "3.5", //0
                "4.2", //1
                //"4.3", //1 of 2 working.  Would need to change the hpack library(no thanks)
                "5.1", //2
                //"5.1.1", //their tests are in conflict and spec is in conflict UNLESS we keep state of a closed connection which would be dumb
                "5.1.2", //3
                "5.3", //4
                "5.3.1", //5
                "5.4", //6
                "5.4.1",  //7
                "5.5", //8
                "6.1", //9
                "6.2", //10
                "6.3", //11
                "6.4", //12
                //"6.5", //Only 1 fails as the hpack library does not support their table header size of 4 294 967 295 only MaxInt!! grrrrrr
                "6.5.2", //13
                "6.7", //14
                "6.8", //15
                "6.9",  //16
                "6.9.1", //17
                "6.9.2", //18
                "6.10",  //19
                "8.1",   //20
                "8.1.2",  //21
                //"8.1.2.1", // TODO.  easy to implement
                //"8.1.2.2", // TODO   easy to implement
                //"8.1.2.3", // TODO.
                //"8.1.2.6", //TODO
                "8.2"
        };
        
        Object[][] arrayOfArgs = new Object[tests.length][];
        for(int i = 0; i < tests.length; i++) {
        	arrayOfArgs[i] = new Object[] { i, tests[i] };
        }
        
		return Arrays.asList(arrayOfArgs);
	}
	
	public TestH2OnNewLib(int index, String testName) {
		this.testName = testName;
		log.info("constructing test suite for server prod="+testName+" index="+index);
	}
	
    @Before
    public void setup() {
        String goPath = System.getenv("GOPATH");
        if(goPath == null) {
        	File f = new File("/Users/dhiller/workspace/gowork");
        	if(f.exists())
        		goPath = f.getAbsolutePath();
        }
        
        if(goPath == null) {
            throw new RuntimeException("Must install h2spec and set GOPATH. Requires GO 1.5+. See https://github.com/summerwind/h2spec and https://golang.org/dl/.");
        }

        h2SpecPath = goPath + "/bin/h2spec";
        File h2SpecFile = new File(h2SpecPath);
        if(h2SpecFile.isDirectory() || !h2SpecFile.canExecute()) {
            throw new RuntimeException("Must install h2spec. Requires GO 1.5+. See https://github.com/summerwind/h2spec and https://golang.org/dl/");
        }

        port = ServerFactory.createTestServer(true, 100L);
    }

    private boolean passH2SpecTest(String testNumber) {
        try {
            ProcessBuilder pb = new ProcessBuilder(h2SpecPath, "-o", "2", "-p", Integer.toString(port), "-S", "-s", testNumber);
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
    	Assert.assertTrue(passH2SpecTest(testName));
    }
}
