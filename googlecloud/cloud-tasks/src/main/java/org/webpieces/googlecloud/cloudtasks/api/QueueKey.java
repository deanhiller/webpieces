package org.webpieces.googlecloud.cloudtasks.api;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueKey {
    String value();
}
