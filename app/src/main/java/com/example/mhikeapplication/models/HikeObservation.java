package com.example.mhikeapplication.models;

public class HikeObservation {

    private long observationId;
    private long hikeId;
    private String observationContent;
    private String timeOfObservation;
    private String observationNotes;
    private String observationLocation;

    // Constructors
    public HikeObservation() {
    }

    // Getters and Setters
    public long getObservationId() {
        return observationId;
    }

    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }

    public long getHikeId() {
        return hikeId;
    }

    public void setHikeId(long hikeId) {
        this.hikeId = hikeId;
    }

    public String getObservationContent() {
        return observationContent;
    }

    public void setObservationContent(String observationContent) {
        this.observationContent = observationContent;
    }

    public String getTimeOfObservation() {
        return timeOfObservation;
    }

    public void setTimeOfObservation(String timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    public String getObservationNotes() {
        return observationNotes;
    }

    public void setObservationNotes(String observationNotes) {
        this.observationNotes = observationNotes;
    }

    public String getObservationLocation() {
        return observationLocation;
    }

    public void setObservationLocation(String observationLocation) {
        this.observationLocation = observationLocation;
    }
}
