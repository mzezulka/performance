package com.hp.mwtests.ts.arjuna.performance;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.internal.JaegerTracer.Builder;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class TracingHelper {

    private static final String TRACER_CONFIG_LOCATION = "tracer_config.properties";
    private static final Properties CONFIG = loadConfig();
    
    static Tracer getJaegerTracer() {
        SamplerConfiguration samplerConfig = new SamplerConfiguration().withType("const").withParam(1);
        SenderConfiguration senderConfig = new SenderConfiguration()
                .withAgentHost(CONFIG.getProperty("jaeger.reporter_host"))
                .withAgentPort(Integer.decode(CONFIG.getProperty("jaeger.reporter_port")));
        ReporterConfiguration reporterConfig = new ReporterConfiguration()
                .withLogSpans(true)
                .withFlushInterval(Integer.valueOf(CONFIG.getProperty("jaeger.flush_interval")))
                .withMaxQueueSize(Integer.valueOf(CONFIG.getProperty("jaeger.max_queue_size")))
                .withSender(senderConfig);
        Builder bldr = new Configuration("tx-demo-perf-tests")
                .withSampler(samplerConfig)
                .withReporter(reporterConfig)
                .getTracerBuilder();
        return bldr.build();
    }

    static Properties loadConfig() {
        try (InputStream fs = TracingHelper.class.getClassLoader().getResourceAsStream(TRACER_CONFIG_LOCATION)) {
            Properties config = new Properties();
            config.load(fs);
            return config;
        } catch (IOException ex) {
            // unrecoverable exception
            throw new RuntimeException(ex);
        }
    }
    
    private enum TracerType {
        JAEGER, NOOP;
    }
    
    static void registerTracer() {
        String tracer = System.getProperty("tracing", CONFIG.getProperty("tracer.default")).toUpperCase();
        switch (TracerType.valueOf(tracer)) {
        case JAEGER:
            GlobalTracer.registerIfAbsent(TracingHelper.getJaegerTracer());
            break;
        case NOOP:
            break;
        default:
            throw new RuntimeException("Unsupported tracer type. List of supported ones: "
                    + Arrays.asList(TracerType.values()));
        }
    }
}
