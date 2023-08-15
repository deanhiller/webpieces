package org.webpieces.aws.storage;

import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;

public class TestClientAssertions implements ClientAssertions {

    @Override
    public void throwIfCannotGoRemote() {

        Object tests = Context.get("tests");
        if(tests != null){
            throw new IllegalStateException("For testing.");
        }

    }
    
}
