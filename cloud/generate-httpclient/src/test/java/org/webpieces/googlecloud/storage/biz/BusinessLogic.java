package org.webpieces.googlecloud.storage.biz;

import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.googlecloud.storage.api.DeanRequest;
import org.webpieces.googlecloud.storage.api.DeanResponse;
import org.webpieces.googlecloud.storage.api.DeansCoolApi;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BusinessLogic {

    private DeansCoolApi deansApi;

    @Inject
    public BusinessLogic(DeansCoolApi deansApi) {
        this.deansApi = deansApi;
    }


    public void runTest() {

        DeanRequest req = new DeanRequest();
        req.setName("testing");
        try {
            DeanResponse deanResponse = deansApi.dean(req).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }
}
