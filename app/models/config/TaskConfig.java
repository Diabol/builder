package models.config;

import java.util.ArrayList;
import java.util.List;

import models.Task;

/**
 * @author danielgronberg, marcus
 */
public class TaskConfig {

    private String name;
    private boolean isAutomatic;
    private List<TaskConfig> nextTasks = new ArrayList<TaskConfig>();

    public Task createTask() {
        return new Task(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    public List<TaskConfig> getNextTasks() {
        return nextTasks;
    }

    public void setNextTasks(List<TaskConfig> nextTasks) {
        this.nextTasks = nextTasks;
    }
}
