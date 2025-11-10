package com.taskscheduler.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Task model class representing a single task with properties:
 * id, title, priority, deadline, duration, and completion status.
 */
public class Task {
    private static int nextId = 1;
    
    private int id;
    private String title;
    private int priority; // 1-5 scale (1 = lowest, 5 = highest)
    private LocalDateTime deadline;
    private int duration; // in minutes
    private boolean isCompleted;

    // Default constructor for JSON deserialization
    public Task() {
        this.id = nextId++;
    }

    // Constructor with all fields
    public Task(String title, int priority, LocalDateTime deadline, int duration) {
        this.id = nextId++;
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.duration = duration;
        this.isCompleted = false;
    }

    // Constructor with id (for loading from storage)
    public Task(int id, String title, int priority, LocalDateTime deadline, int duration, boolean isCompleted) {
        this.id = id;
        if (id >= nextId) {
            nextId = id + 1;
        }
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.duration = duration;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        this.priority = priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    /**
     * Calculate the hours remaining until deadline
     * @return hours remaining (can be negative if deadline has passed)
     */
    public double getHoursUntilDeadline() {
        if (deadline == null) {
            return Double.MAX_VALUE;
        }
        return java.time.Duration.between(LocalDateTime.now(), deadline).toHours();
    }

    /**
     * Check if task deadline has passed
     * @return true if deadline is in the past
     */
    public boolean isOverdue() {
        if (deadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(deadline) && !isCompleted;
    }

    /**
     * Get priority level as string
     * @return priority level description
     */
    public String getPriorityLevel() {
        switch (priority) {
            case 5: return "Critical";
            case 4: return "High";
            case 3: return "Medium";
            case 2: return "Low";
            case 1: return "Very Low";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String deadlineStr = deadline != null ? deadline.format(formatter) : "No deadline";
        String status = isCompleted ? "[COMPLETED]" : "[PENDING]";
        return String.format("%s Task #%d: %s | Priority: %s (%d) | Duration: %d min | Deadline: %s",
                status, id, title, getPriorityLevel(), priority, duration, deadlineStr);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

