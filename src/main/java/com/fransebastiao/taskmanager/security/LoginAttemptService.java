package com.fransebastiao.taskmanager.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    public static final int MAX_ATTEMPT = 5;
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<>() {
                @Override
                public Integer load(@SuppressWarnings("null") String key) {
                    return 0;
                }
            });
    }

    @SuppressWarnings("null")
    public void loginFailed(String key) {
        int attempts = getAttempts(key);
        attemptsCache.put(key, Math.min(attempts + 1, MAX_ATTEMPT));
    }

    @SuppressWarnings("null")
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public boolean isBlocked(String key) {
        return getAttempts(key) >= MAX_ATTEMPT;
    }

    @SuppressWarnings("null")
    private int getAttempts(String key) {
        try {
            return attemptsCache.get(key);
        } catch (ExecutionException e) {
            return 0;
        }
    }
}
