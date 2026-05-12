package com.fransebastiao.taskmanager.mapper;

import com.fransebastiao.taskmanager.domain.user.Role;
import com.fransebastiao.taskmanager.dto.response.RoleDto;

public class RoleMapper {

    public static RoleDto map(Role role) {
        RoleDto dto = new RoleDto(role.getName());
        return dto;
    }
}
