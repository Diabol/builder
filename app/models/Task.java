package models;

import java.util.List;

import models.config.TaskConfig;
import models.result.TaskResult;

public class Task {

    public enum State {NOT_STARTED, IN_PROGRESS, FINISHED}

    private final TaskConfig config;
    private State state = State.NOT_STARTED;

    public Task(TaskConfig config) {
        this.config = config;
    }

    public TaskResult start() {
        TaskResult result = new TaskResult(this);
        // TODO
        return result;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
    }

    public String getName() {
        return config.getName();
    }

    public Boolean isAutomatic() {
        return config.isAutomatic();
    }

    public List<TaskConfig> getNextTasks() {
        return config.getNextTasks();
    }

}
