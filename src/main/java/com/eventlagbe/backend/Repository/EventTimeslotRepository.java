package com.eventlagbe.backend.Repository;

import com.eventlagbe.backend.Models.EventTimeslot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventTimeslotRepository extends MongoRepository<EventTimeslot, String> {
    List<EventTimeslot> findByEventId(String eventId);
}


