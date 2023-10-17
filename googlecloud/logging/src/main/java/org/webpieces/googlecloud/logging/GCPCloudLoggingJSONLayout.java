package org.webpieces.googlecloud.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.context.PlatformHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * GKE fluentd ingestion detective work:
 * https://cloud.google.com/error-reporting/docs/formatting-error-messages#json_representation
 * http://google-cloud-python.readthedocs.io/en/latest/logging-handlers-container-engine.html
 * http://google-cloud-python.readthedocs.io/en/latest/_modules/google/cloud/logging/handlers/container_engine.html#ContainerEngineHandler.format
 * https://github.com/GoogleCloudPlatform/google-cloud-python/blob/master/logging/google/cloud/logging/handlers/_helpers.py
 * https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry
 */
public class GCPCloudLoggingJSONLayout extends PatternLayout {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern REGEX_SSN = Pattern.compile("\\b[0-9]{9}\\b");
    private static final Pattern REGEX_SSN2 = Pattern.compile("\\b([0-9]{3})-([0-9]{2})-([0-9]{4})\\b");

    private static String version;
    private static String instanceId;
    private final SingletonHeaderList headerList;

    //A hack since logback creates this class, we HAVE to create a global to set the version so that
    //version shows up in every log.....
    public static void setVersion(String ver) {
        version = ver;
    }

    public static void setInstanceId(String instId) {
        instanceId = instId;
    }

    public GCPCloudLoggingJSONLayout() {
        headerList = SingletonHeaderList.getSingleton();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        try {
            String formattedMessage = super.doLayout(event);
            return doLayoutImpl(formattedMessage, event);
        } catch (Throwable e) {
            //must return something to put in logs
            return "Failed to write value as string. " + e.getMessage();
        }
    }

    /**
     * For testing without having to deal wth the complexity of super.doLayout()
     * Uses formattedMessage instead of event.getMessage()
     */
    private String doLayoutImpl(String formattedMessage, ILoggingEvent logEvent) throws JsonProcessingException {
        boolean hasException = logEvent.getThrowableProxy() != null;
        long timestampMillis = logEvent.getTimeStamp();
        String severity = mapLevelToGCPLevel(logEvent.getLevel());
        String threadName = logEvent.getThreadName();
        String loggerName = logEvent.getLoggerName();

        LogEvent event = buildLoggingEvent(formattedMessage, hasException, timestampMillis, severity, threadName, loggerName);

        // Add a newline so that each JSON log entry is on its own line.
        // Note that it is also important that the JSON log entry does not span multiple lines.
        return objectMapper.writeValueAsString(event) + "\n";
    }

    public LogEvent buildLoggingEvent(String message,
                                             boolean hasException,
                                             long timestampMillis,
                                             String severity,
                                             String threadName,
                                             String loggerName) {
        LogEvent event = new LogEvent();

        String socket = MDC.get("svrSocket");
        String clntSocket = MDC.get("clntSocket");
        String transactionId = MDC.get("txId");

        event.setMessage(message.trim());
        event.setHasException(hasException);
        event.setTimestampMillis(timestampMillis);
        event.setTimestamp(convertTimestampToGCPLogTimestamp(timestampMillis));
        event.setSeverity(severity);
        event.setThread(threadName);
        event.setLogger(loggerName);
        event.setSocket(socket);
        event.setClientSocket(clntSocket);
        event.setTransactionId(transactionId);
        event.setVersion(version);
        event.setInstanceId(instanceId);

        List<PlatformHeaders> platformHeaders = headerList.listHeaderCtxPairs();

        for(PlatformHeaders header : platformHeaders) {
            //we should only be setting headers that want to be logged to not waste cycles
            String key = header.getLoggerMDCKey();
            String value = MDC.get(key);
            if (value != null) {
                event.getHeaders().put(key, value);
            }
        }

        return event;

    }

    private static GCPCloudLoggingTimestamp convertTimestampToGCPLogTimestamp(long millisSinceEpoch) {
        int nanos = ((int)(millisSinceEpoch % 1000)) * 1_000_000; // strip out just the milliseconds and convert to nanoseconds
        long seconds = millisSinceEpoch / 1000L; // remove the milliseconds
        return new GCPCloudLoggingTimestamp(seconds, nanos);
    }

    private static String mapLevelToGCPLevel(Level level) {
        switch (level.toInt()) {
            case Level.TRACE_INT:
                return "TRACE";
            case Level.DEBUG_INT:
                return "DEBUG";
            case Level.INFO_INT:
                return "INFO";
            case Level.WARN_INT:
                return "WARN";
            case Level.ERROR_INT:
                return "ERROR";
            default:
                return null; /* This should map to no level in GCP Cloud Logging */
        }
    }

    @Override
    public Map<String, String> getDefaultConverterMap() {
        return PatternLayout.defaultConverterMap;
    }


}
