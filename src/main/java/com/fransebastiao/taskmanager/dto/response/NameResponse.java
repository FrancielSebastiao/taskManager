package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record NameResponse(
    UUID id,
    String name
) {}
