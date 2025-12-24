package com.financeapp.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    private final Map<String, Long> blacklistedTokenExp = new ConcurrentHashMap<>();

    public void blacklist(String token, long expiresAtMillis) {
        blacklistedTokenExp.put(token, expiresAtMillis);
    }

    public boolean isBlacklisted(String token) {
        Long exp = blacklistedTokenExp.get(token);
        if (exp == null) return false;
        if (exp < Instant.now().toEpochMilli()) {
            blacklistedTokenExp.remove(token);
            return false;
        }
        return true;
    }
}


