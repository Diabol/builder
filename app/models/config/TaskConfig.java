package models.config;

import java.util.ArrayList;
import java.util.List;

import models.Task;

/**
 * @author danielgronberg, marcus
 */
public class TaskConfig {

    private String taskName;
    private boolean isAutomatic;
    private String cmd;
    private List<String> triggersTasks;

    public Task createTask() {
        return new Task(this);
    }

    public String getTaskName() {
        return taskName;
    }

    public void setName(String taskName) {
        this.taskName = taskName;
    }

    public Boolean isAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

    public String getCommand() {
        return cmd;
    }

    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    public List<String> getTriggersTasks() {
        return triggersTasks;
    }

    public void setTriggersTasks(List<String> triggersTasks) {
        this.triggersTasks = triggersTasks;
    }
}
