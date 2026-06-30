package com.example.visionnotes;

public class NoteModel {

    private String id;                   // Firebase key
    private String title;
    private String description;
    private Long reminderTimeMillis;     // nullable Long for reminders
    private String imagePath;            // optional
    private Long modifiedAt;             // store timestamp as Long
    private boolean isDone;              // optional for tasks

    // 🔹 EMPTY constructor required for Firebase
    public NoteModel() {}

    // 🔹 FULL constructor for creating new notes
    public NoteModel(String id,
                     String title,
                     String description,
                     Long reminderTimeMillis,
                     String imagePath,
                     Long modifiedAt,
                     boolean isDone) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.reminderTimeMillis = reminderTimeMillis;
        this.imagePath = imagePath;
        this.modifiedAt = modifiedAt;
        this.isDone = isDone;
    }

    // GETTERS
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getReminderTimeMillis() { return reminderTimeMillis; }
    public String getImagePath() { return imagePath; }
    public Long getModifiedAt() { return modifiedAt; }
    public boolean isDone() { return isDone; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setReminderTimeMillis(Long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setModifiedAt(Long modifiedAt) { this.modifiedAt = modifiedAt; }
    public void setDone(boolean done) { this.isDone = done; }

    // 🔹 Convenience constructor for new note (auto timestamp)
    public NoteModel(String title, String description, Long reminderTimeMillis, String imagePath) {
        this.title = title;
        this.description = description;
        this.reminderTimeMillis = reminderTimeMillis;
        this.imagePath = imagePath;
        this.modifiedAt = System.currentTimeMillis();
        this.isDone = false;
    }
}