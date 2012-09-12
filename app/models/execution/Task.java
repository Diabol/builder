package models.execution;

import static models.execution.Task.State.FINISHED;
import static models.execution.Task.State.IN_PROGRESS;

import java.util.List;

import models.config.TaskConfig;
import models.state.TaskResult;
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
        return config.getTaskName();
    }

    public Boolean isAutomatic() {
        return config.isAutomatic();
    }

    public List<String> getNextTasks() {
        return config.getTriggersTasks();
    }

    public String getCommand() {
        return config.getCommand();
    }

}
