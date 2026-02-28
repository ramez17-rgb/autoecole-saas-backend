package com.autoecole.saas.session.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class CreateSessionRequest {

    @NotNull(message = "Monitor ID is mandatory")
    private UUID monitorId;

    @NotNull(message = "Session date is mandatory")
    @FutureOrPresent(message = "Session date must be in the present or future")
    private LocalDate sessionDate;

    @NotNull(message = "Start time is mandatory")
    private LocalTime startTime;

    @NotNull(message = "End time is mandatory")
    private LocalTime endTime;

    @NotEmpty(message = "At least one student must be assigned")
    private List<UUID> studentIds;

    public CreateSessionRequest() {
    }

    public UUID getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(UUID monitorId) {
        this.monitorId = monitorId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public List<UUID> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<UUID> studentIds) {
        this.studentIds = studentIds;
    }
}
