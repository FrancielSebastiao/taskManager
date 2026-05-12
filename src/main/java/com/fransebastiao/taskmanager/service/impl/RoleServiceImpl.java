package com.fransebastiao.taskmanager.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.Role;
import com.fransebastiao.taskmanager.dto.response.RoleDto;
import com.fransebastiao.taskmanager.exception.custom.DuplicateException;
import com.fransebastiao.taskmanager.mapper.RoleMapper;
import com.fransebastiao.taskmanager.repository.RoleRepository;
import com.fransebastiao.taskmanager.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Transactional 
    @Override
    public RoleDto createRole(String name) {
        if (roleExists(name)) {
            throw new DuplicateException("Função: " + name + " já exsite.");
        }

        Role role = new Role(name);

        Role saved = roleRepository.save(role);
        return RoleMapper.map(saved);
    }

    private boolean roleExists(String name) {
        return roleRepository.findByName(name).isPresent();
    }
}
