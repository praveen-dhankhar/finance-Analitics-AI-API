package com.financeapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(@Value("${spring.profiles.active:default}") String profile) {
        return registry -> registry.config().commonTags("profile", profile);
    }

    @Bean
    public ClassLoaderMetrics classLoaderMetrics() { return new ClassLoaderMetrics(); }

    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() { return new JvmMemoryMetrics(); }

    @Bean
    public JvmGcMetrics jvmGcMetrics() { return new JvmGcMetrics(); }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() { return new JvmThreadMetrics(); }

    @Bean
    public ProcessorMetrics processorMetrics() { return new ProcessorMetrics(); }
}


