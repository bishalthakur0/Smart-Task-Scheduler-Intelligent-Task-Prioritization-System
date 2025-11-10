package com.taskscheduler.scheduler;

import com.taskscheduler.model.Task;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskScheduler implements a greedy algorithm with priority queue
 * to generate optimal task schedules based on deadline, priority, and duration.
 * 
 * Algorithm: Weighted scoring formula combining priority, urgency, and effort.
 * Time Complexity: O(n log n) due to sorting and priority queue operations.
 */
public class TaskScheduler {
    
    // Weights for priority score calculation
    private static final double WEIGHT_PRIORITY = 0.5;    // w1: importance
    private static final double WEIGHT_URGENCY = 0.3;     // w2: urgency (deadline)
    private static final double WEIGHT_EFFORT = 0.2;      // w3: effort/time
    
    // Default working hours per day (8 hours = 480 minutes)
    private static final int DEFAULT_WORKING_MINUTES = 480;
    
    /**
     * Generate optimal schedule for tasks using greedy algorithm
     * @param tasks List of tasks to schedule
     * @return List of tasks in optimal order
     */
    public List<Task> generateOptimalSchedule(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter out completed tasks
        List<Task> pendingTasks = tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());

        if (pendingTasks.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate priority scores for each task
        Map<Task, Double> taskScores = new HashMap<>();
        for (Task task : pendingTasks) {
            double score = calculateScore(task);
            taskScores.put(task, score);
        }

        // Sort tasks by score (highest score first) - greedy approach
        List<Task> sortedTasks = new ArrayList<>(pendingTasks);
        sortedTasks.sort((t1, t2) -> {
            double score1 = taskScores.get(t1);
            double score2 = taskScores.get(t2);
            // Higher score = higher priority
            return Double.compare(score2, score1);
        });

        // Apply deadline constraint: ensure tasks with imminent deadlines come first
        // Sort by deadline first, then by score
        sortedTasks.sort((t1, t2) -> {
            // If one task is overdue, it should come first
            boolean t1Overdue = t1.isOverdue();
            boolean t2Overdue = t2.isOverdue();
            
            if (t1Overdue && !t2Overdue) return -1;
            if (!t1Overdue && t2Overdue) return 1;
            
            // If both overdue or both not overdue, check deadline proximity
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilT1 = t1.getDeadline() != null ? 
                Duration.between(now, t1.getDeadline()).toHours() : Long.MAX_VALUE;
            long hoursUntilT2 = t2.getDeadline() != null ? 
                Duration.between(now, t2.getDeadline()).toHours() : Long.MAX_VALUE;
            
            // Tasks with closer deadlines come first
            int deadlineComparison = Long.compare(hoursUntilT1, hoursUntilT2);
            if (deadlineComparison != 0) {
                return deadlineComparison;
            }
            
            // If deadlines are same, use priority score
            return Double.compare(taskScores.get(t2), taskScores.get(t1));
        });

        return sortedTasks;
    }

    /**
     * Generate optimal schedule with time constraint (working hours limit)
     * @param tasks List of tasks to schedule
     * @param workingMinutes Maximum minutes available for scheduling
     * @return List of tasks that can be completed within time limit
     */
    public List<Task> generateOptimalSchedule(List<Task> tasks, int workingMinutes) {
        List<Task> optimalSchedule = generateOptimalSchedule(tasks);
        List<Task> feasibleSchedule = new ArrayList<>();
        int totalTime = 0;

        for (Task task : optimalSchedule) {
            if (totalTime + task.getDuration() <= workingMinutes) {
                feasibleSchedule.add(task);
                totalTime += task.getDuration();
            } else {
                // Cannot fit this task, but continue to see if smaller tasks fit
                continue;
            }
        }

        return feasibleSchedule;
    }

    /**
     * Calculate priority score for a task using weighted formula:
     * S = (w1 * Priority) + (w2 * 1/Deadline_Remaining) + (w3 * 1/Duration)
     * 
     * @param task Task to calculate score for
     * @return Priority score (higher = more important)
     */
    public double calculateScore(Task task) {
        // Normalize priority (1-5 scale, so divide by 5 to get 0.2-1.0)
        double priorityScore = task.getPriority() / 5.0;
        
        // Urgency score based on deadline
        double urgencyScore = calculateUrgencyScore(task);
        
        // Effort score (shorter tasks get higher score)
        double effortScore = calculateEffortScore(task);
        
        // Weighted combination
        double totalScore = (WEIGHT_PRIORITY * priorityScore) + 
                           (WEIGHT_URGENCY * urgencyScore) + 
                           (WEIGHT_EFFORT * effortScore);
        
        return totalScore;
    }

    /**
     * Calculate urgency score based on deadline
     * @param task Task to evaluate
     * @return Urgency score (0.0 to 1.0)
     */
    private double calculateUrgencyScore(Task task) {
        if (task.getDeadline() == null) {
            return 0.1; // No deadline = low urgency
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilDeadline = Duration.between(now, task.getDeadline()).toHours();
        
        if (hoursUntilDeadline < 0) {
            // Overdue tasks get maximum urgency
            return 1.0;
        }
        
        if (hoursUntilDeadline == 0) {
            return 0.99;
        }
        
        // Inverse relationship: closer deadline = higher urgency
        // Normalize: 1 hour = 1.0, 24 hours = 0.5, 168 hours (1 week) = 0.1
        // Formula: 1 / (1 + hours/24)
        double normalizedHours = hoursUntilDeadline / 24.0;
        return 1.0 / (1.0 + normalizedHours);
    }

    /**
     * Calculate effort score (shorter tasks preferred)
     * @param task Task to evaluate
     * @return Effort score (0.0 to 1.0)
     */
    private double calculateEffortScore(Task task) {
        int duration = task.getDuration();
        
        // Shorter tasks get higher score
        // Normalize: 15 min = 1.0, 60 min = 0.5, 240 min (4 hours) = 0.2
        // Formula: 1 / (1 + duration/60)
        double normalizedDuration = duration / 60.0;
        return 1.0 / (1.0 + normalizedDuration);
    }

    /**
     * Sort tasks by deadline (earliest first)
     * @param tasks List of tasks to sort
     * @return Sorted list by deadline
     */
    public List<Task> sortByDeadline(List<Task> tasks) {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort((t1, t2) -> {
            if (t1.getDeadline() == null && t2.getDeadline() == null) return 0;
            if (t1.getDeadline() == null) return 1;
            if (t2.getDeadline() == null) return -1;
            return t1.getDeadline().compareTo(t2.getDeadline());
        });
        return sorted;
    }

    /**
     * Get estimated completion time for a schedule
     * @param schedule List of tasks in schedule
     * @return Total duration in minutes
     */
    public int getTotalDuration(List<Task> schedule) {
        return schedule.stream()
                .mapToInt(Task::getDuration)
                .sum();
    }

    /**
     * Check if schedule fits within working hours
     * @param schedule List of tasks
     * @param workingMinutes Available minutes
     * @return true if schedule fits
     */
    public boolean isFeasible(List<Task> schedule, int workingMinutes) {
        return getTotalDuration(schedule) <= workingMinutes;
    }
}

