package com.taskscheduler.manager;

import com.taskscheduler.model.Task;
import com.taskscheduler.scheduler.TaskScheduler;
import com.taskscheduler.storage.StorageManager;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskManager handles business logic for task management:
 * CRUD operations, scheduling, and coordination between storage and scheduler.
 */
public class TaskManager {
    
    private List<Task> tasks;
    private final TaskScheduler scheduler;
    private final StorageManager storage;
    
    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.scheduler = new TaskScheduler();
        this.storage = new StorageManager();
        loadTasks();
    }

    public TaskManager(StorageManager storage) {
        this.tasks = new ArrayList<>();
        this.scheduler = new TaskScheduler();
        this.storage = storage;
        loadTasks();
    }

    /**
     * Load tasks from storage
     */
    public void loadTasks() {
        this.tasks = storage.loadTasks();
    }

    /**
     * Save tasks to storage
     * @return true if save successful
     */
    public boolean saveTasks() {
        return storage.saveTasks(tasks);
    }

    /**
     * Add a new task
     * @param title Task title
     * @param priority Priority (1-5)
     * @param deadline Deadline date-time
     * @param duration Duration in minutes
     * @return Created task
     * @throws IllegalArgumentException if validation fails
     */
    public Task addTask(String title, int priority, LocalDateTime deadline, int duration) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        Task task = new Task(title.trim(), priority, deadline, duration);
        tasks.add(task);
        saveTasks();
        return task;
    }

    /**
     * Get task by ID
     * @param id Task ID
     * @return Task if found, null otherwise
     */
    public Task getTaskById(int id) {
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all tasks
     * @return List of all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Get pending (incomplete) tasks
     * @return List of pending tasks
     */
    public List<Task> getPendingTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    /**
     * Get completed tasks
     * @return List of completed tasks
     */
    public List<Task> getCompletedTasks() {
        return tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    /**
     * Update task
     * @param id Task ID
     * @param title New title (null to keep existing)
     * @param priority New priority (null to keep existing)
     * @param deadline New deadline (null to keep existing)
     * @param duration New duration (null to keep existing)
     * @return Updated task if found, null otherwise
     */
    public Task updateTask(int id, String title, Integer priority, LocalDateTime deadline, Integer duration) {
        Task task = getTaskById(id);
        if (task == null) {
            return null;
        }

        if (title != null && !title.trim().isEmpty()) {
            task.setTitle(title.trim());
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (deadline != null) {
            task.setDeadline(deadline);
        }
        if (duration != null) {
            task.setDuration(duration);
        }

        saveTasks();
        return task;
    }

    /**
     * Mark task as completed
     * @param id Task ID
     * @return true if task found and marked
     */
    public boolean markTaskCompleted(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setCompleted(true);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Mark task as pending (uncomplete)
     * @param id Task ID
     * @return true if task found and marked
     */
    public boolean markTaskPending(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setCompleted(false);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Delete task
     * @param id Task ID
     * @return true if task found and deleted
     */
    public boolean deleteTask(int id) {
        Task task = getTaskById(id);
        if (task != null) {
            tasks.remove(task);
            saveTasks();
            return true;
        }
        return false;
    }

    /**
     * Generate optimal schedule for pending tasks
     * @return List of tasks in optimal order
     */
    public List<Task> generateOptimalSchedule() {
        return scheduler.generateOptimalSchedule(tasks);
    }

    /**
     * Generate optimal schedule with time constraint
     * @param workingMinutes Maximum minutes available
     * @return List of tasks that fit within time limit
     */
    public List<Task> generateOptimalSchedule(int workingMinutes) {
        return scheduler.generateOptimalSchedule(tasks, workingMinutes);
    }

    /**
     * Get statistics about tasks
     * @return Map with statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", tasks.size());
        stats.put("completedTasks", getCompletedTasks().size());
        stats.put("pendingTasks", getPendingTasks().size());
        stats.put("overdueTasks", tasks.stream().filter(Task::isOverdue).count());
        
        double completionRate = tasks.isEmpty() ? 0.0 : 
            (double) getCompletedTasks().size() / tasks.size() * 100;
        stats.put("completionRate", completionRate);
        
        return stats;
    }

    /**
     * Get overdue tasks
     * @return List of overdue tasks
     */
    public List<Task> getOverdueTasks() {
        return tasks.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }
}

