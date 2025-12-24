package com.financeapp.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private static final int DEFAULT_LIMIT = 10; // requests
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        long now = Instant.now().toEpochMilli();
        Counter c = counters.computeIfAbsent(key, k -> new Counter(0, now + WINDOW_MS));
        synchronized (c) {
            if (now > c.resetAt) {
                c.count = 0;
                c.resetAt = now + WINDOW_MS;
            }
            if (c.count >= DEFAULT_LIMIT) return false;
            c.count++;
            return true;
        }
    }

    private static class Counter {
        int count;
        long resetAt;
        Counter(int count, long resetAt) { this.count = count; this.resetAt = resetAt; }
    }
}


