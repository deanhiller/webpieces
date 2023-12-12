package org.webpieces.googlecloud.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.logback.LoggingEventEnhancer;
import org.slf4j.MDC;
import org.webpieces.util.context.PlatformHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GKE fluentd ingestion detective work:
 * https://cloud.google.com/error-reporting/docs/formatting-error-messages#json_representation
 * http://google-cloud-python.readthedocs.io/en/latest/logging-handlers-container-engine.html
 * http://google-cloud-python.readthedocs.io/en/latest/_modules/google/cloud/logging/handlers/container_engine.html#ContainerEngineHandler.format
 * https://github.com/GoogleCloudPlatform/google-cloud-python/blob/master/logging/google/cloud/logging/handlers/_helpers.py
 * https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry
 */
public class GCPCloudLoggingEnhancer implements LoggingEventEnhancer {

    private static String version;
    private static String instanceId;

    //A hack since logback creates this class, we HAVE to create a global to set the version so that
    //version shows up in every log.....
    public static void setVersion(String ver) {
        version = ver;
    }

    public static void setInstanceId(String instId) {
        instanceId = instId;
    }

    public GCPCloudLoggingEnhancer() {

    }

    @Override
    public void enhanceLogEntry(LogEntry.Builder builder, ILoggingEvent logEvent) {
        boolean hasException = logEvent.getThrowableProxy() != null;
        //String severity = mapLevelToGCPLevel(logEvent.getLevel());
        String threadName = logEvent.getThreadName();
        //String loggerName = logEvent.getLoggerName();



        Map<String, String> labels = new HashMap<>();
        labels.put("hasException", hasException + "");
        labels.put("thread", threadName);
        labels.put("version", version);
        labels.put("instanceId", instanceId);

//        String socket = MDC.get("svrSocket");
//        String clntSocket = MDC.get("clntSocket");
//        String transactionId = MDC.get("txId");
//        labels.put("svrSocket", socket);
//        labels.put("clntSocket", clntSocket);
//        labels.put("txId", transactionId);

        for(Map.Entry<String, String> entry : MDC.getCopyOfContextMap().entrySet()) {
            labels.put(entry.getKey(), entry.getValue());
        }

        builder.setLabels(labels);
    }
}
