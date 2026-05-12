package com.fransebastiao.taskmanager.service;

import java.util.Date;

public interface BlacklistService {
    void blacklist(String token, Date expiration);
    boolean isBlacklisted(String jti);
}
