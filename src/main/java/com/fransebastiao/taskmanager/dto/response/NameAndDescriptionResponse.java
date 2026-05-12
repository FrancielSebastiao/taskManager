package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record NameAndDescriptionResponse(
    UUID id,
    String name, 
    String description
) {}
