package com.eventlagbe.backend.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "event_timeslots")
public class EventTimeslot {
    @Id
    private String id;

    @Indexed
    private String eventId;

    private String title; 
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant start;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant end;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Instant getStart() { return start; }
    public void setStart(Instant start) { this.start = start; }
    
    public Instant getEnd() { return this.end; }
    public void setEnd(Instant end) { this.end = end; }

    @Override
    public String toString() {
        return "EventTimeslot{" +
                "id='" + id + '\'' +
                ", eventId='" + eventId + '\'' +
                ", title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}


