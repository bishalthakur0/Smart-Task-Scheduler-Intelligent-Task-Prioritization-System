package com.taskscheduler;

import com.taskscheduler.manager.TaskManager;
import com.taskscheduler.model.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskManager taskManager;

    public TaskController() {
        this.taskManager = new TaskManager();
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskManager.getAllTasks();
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        taskManager.addTask(task.getTitle(), task.getPriority(), task.getDeadline(), task.getDuration());
        return task;
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable int id) {
        return taskManager.getTaskById(id);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable int id, @RequestBody Task updatedTask) {
        taskManager.updateTask(id, updatedTask.getTitle(), updatedTask.getPriority(), updatedTask.getDeadline(), updatedTask.getDuration());
        return updatedTask;
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable int id) {
        taskManager.deleteTask(id);
    }

    @PostMapping("/{id}/complete")
    public boolean markTaskComplete(@PathVariable int id) {
        return taskManager.markTaskCompleted(id);
    }

    @GetMapping("/pending")
    public List<Task> getPendingTasks() {
        return taskManager.getPendingTasks();
    }

    @GetMapping("/completed")
    public List<Task> getCompletedTasks() {
        return taskManager.getCompletedTasks();
    }

    @GetMapping("/scheduled")
    public List<Task> getScheduledTasks() {
        return taskManager.generateOptimalSchedule();
    }

    @GetMapping("/statistics")
    public java.util.Map<String, Object> getStatistics() {
        return taskManager.getStatistics();
    }
}