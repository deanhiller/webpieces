package org.webpieces.googlecloud.cloudtasks;

import org.webpieces.util.context.Context;

public class ClientAssertionsForTest implements org.webpieces.util.context.ClientAssertions {
    @Override
    public void throwIfCannotGoRemote() {
        Object tests = Context.get("tests");
        if(tests != null){
            throw new IllegalStateException("For testing.");
        }
    }
}
