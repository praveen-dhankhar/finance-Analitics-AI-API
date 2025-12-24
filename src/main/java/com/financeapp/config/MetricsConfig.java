package com.financeapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Aspect
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final String profile;

    public MetricsConfig(MeterRegistry meterRegistry, @Value("${spring.profiles.active:default}") String profile) {
        this.meterRegistry = meterRegistry;
        this.profile = profile;
    }

    @Around("execution(* com.financeapp.service..*(..))")
    public Object timeServiceCalls(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return pjp.proceed();
        } finally {
            sample.stop(Timer.builder("service.method.duration")
                    .tag("method", method)
                    .tag("profile", profile)
                    .register(meterRegistry));
        }
    }
}


