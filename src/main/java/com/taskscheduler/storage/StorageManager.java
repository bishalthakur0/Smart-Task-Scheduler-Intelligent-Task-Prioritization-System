package com.taskscheduler.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.taskscheduler.model.Task;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * StorageManager handles persistence of tasks to JSON file.
 * Uses Gson library for JSON serialization/deserialization.
 */
public class StorageManager {
    
    private static final String DEFAULT_STORAGE_PATH = "data/tasks.json";
    private final String filePath;
    private final Gson gson;
    
    // Custom date-time adapter for LocalDateTime
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StorageManager() {
        this(DEFAULT_STORAGE_PATH);
    }

    public StorageManager(String filePath) {
        this.filePath = filePath;
        // Configure Gson with LocalDateTime adapter
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        
        // Ensure data directory exists
        ensureDataDirectoryExists();
    }

    /**
     * Save tasks to JSON file
     * @param tasks List of tasks to save
     * @return true if save successful
     */
    public boolean saveTasks(List<Task> tasks) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(tasks, writer);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            return false;
        }
    }

    /**
     * Load tasks from JSON file
     * @return List of tasks, empty list if file doesn't exist or error occurs
     */
    public List<Task> loadTasks() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            return tasks != null ? tasks : new ArrayList<>();
        } catch (FileNotFoundException e) {
            System.err.println("Tasks file not found: " + filePath);
            return new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error reading tasks: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error parsing tasks JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Ensure data directory exists
     */
    private void ensureDataDirectoryExists() {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        if (parentDir != null) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                System.err.println("Error creating data directory: " + e.getMessage());
            }
        }
    }

    /**
     * Get storage file path
     * @return File path string
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Custom adapter for LocalDateTime serialization/deserialization
     */
    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>,
            com.google.gson.JsonDeserializer<LocalDateTime> {
        
        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, 
                java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json,
                java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            String dateTimeStr = json.getAsString();
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        }
    }
}

