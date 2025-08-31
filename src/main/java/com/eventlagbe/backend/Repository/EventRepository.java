package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    Page<Event> findAll(Pageable pageable);
    List<Event> findByOwnerId(String ownerId);
    List<Event> findByIsActiveTrue();
}


