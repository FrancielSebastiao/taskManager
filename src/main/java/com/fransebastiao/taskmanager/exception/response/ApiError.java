package com.fransebastiao.taskmanager.exception.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
public class ApiError {
    private String message;
    private int status;
    private LocalDateTime timestamp;
}
