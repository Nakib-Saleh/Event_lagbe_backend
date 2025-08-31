package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SkillRepository extends MongoRepository<Skill, String> {
    boolean existsByName(String name);
    Page<Skill> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
} 