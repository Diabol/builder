package models;

import static models.Task.State.FINISHED;
import static models.Task.State.IN_PROGRESS;

import java.util.List;

import models.config.TaskConfig;
import models.result.TaskResult;
import executor.TaskExecutor;

public class Task {

    public enum State {NOT_STARTED, IN_PROGRESS, FINISHED}

    private State state = State.NOT_STARTED;
    private final TaskConfig config;
    private TaskResult result;

    public Task(TaskConfig config) {
        this.config = config;
    }

    public TaskResult start() {
        setState(IN_PROGRESS);
        result = new TaskExecutor(this).execute();
        setState(FINISHED);
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

    public String getCommand() {
        return config.getCommand();
    }

}
