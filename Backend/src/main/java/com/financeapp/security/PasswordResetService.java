package com.financeapp.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final long TTL_MS = 15 * 60_000; // 15 minutes
    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String issueToken(String email) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new Entry(email, Instant.now().toEpochMilli() + TTL_MS));
        return token;
    }

    public String consume(String token) {
        Entry entry = tokens.remove(token);
        if (entry == null) return null;
        if (entry.expiresAt < Instant.now().toEpochMilli()) return null;
        return entry.email;
    }

    private static class Entry {
        String email;
        long expiresAt;
        Entry(String email, long expiresAt) { this.email = email; this.expiresAt = expiresAt; }
    }
}


