package com.eventlagbe.backend.Controller;

import com.eventlagbe.backend.Models.Skill;
import com.eventlagbe.backend.Repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:5173")
public class SkillController {
    @Autowired
    private SkillRepository skillRepository;

    @GetMapping
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Skill>> searchSkills(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Skill> skills = skillRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable);
        return ResponseEntity.ok(skills);
    }

    @PostMapping
    public ResponseEntity<?> addSkill(@RequestBody Skill skill) {
        if (skillRepository.existsByName(skill.getName())) {
            return ResponseEntity.badRequest().body("Skill with this name already exists");
        }
        Skill saved = skillRepository.save(skill);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable String id, @RequestBody Skill skill) {
        Optional<Skill> existing = skillRepository.findById(id);
        if (existing.isPresent()) {
            Skill s = existing.get();
            s.setName(skill.getName());
            s.setDescription(skill.getDescription());
            s.setIsActive(skill.getIsActive());
            Skill updated = skillRepository.save(s);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable String id) {
        if (skillRepository.existsById(id)) {
            skillRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
} 