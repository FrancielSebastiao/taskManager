package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.resource.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {

    Optional<Material> findByName(String name);

    boolean existsByName(String name);

    List<Material> findByNameContainingIgnoreCase(String name);
}
