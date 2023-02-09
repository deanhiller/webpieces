package org.webpieces.googlecloud.cloudtasks;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.webpieces.googlecloud.cloudtasks.api.QueueClientCreator;
import org.webpieces.util.context.ClientAssertions;

import javax.inject.Singleton;
import java.net.InetSocketAddress;

public class FakeProdModule implements Module{
    @Override
    public void configure(Binder binder) {
        binder.bind(ClientAssertions.class).toInstance(new ClientAssertionsForTest());
    }

    @Provides
    @Singleton
    public DeansApi provideDeansApi(QueueClientCreator creator) {
        return creator.createClient(DeansApi.class, new InetSocketAddress(8080));
    }
}
