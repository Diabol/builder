package executor;

import java.io.IOException;

import models.config.TaskConfig;
import play.Logger;

public class Task implements Runnable {

    private final TaskConfig config;
    private final TaskCallback callback;
    private TaskResult result;

    public Task(TaskConfig config, TaskCallback callback) {
        this.config = config;
        this.callback = callback;
    }

    @Override
    public void run() {
        execute();
        callback.receiveTaskResult(result);
    }

    private void execute() {
        Process process = null;
        try {
            process = new ProcessBuilder(getCommand()).start();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Logger.warn("Thread for task: '" + getName() + "' interrupted while waiting for process to finish.", e);
            }
        } catch (IOException e) {
            Logger.error("Failed while running task: '" + getName() + "'.", e);
        } finally {
            if (process != null) {
                result = new TaskResult(process);
                process.destroy();
            } else {
                result = TaskResult.EMPTY_FAILED_RESULT;
            }
        }
    }

    public String getName() {
        return config.getTaskName();
    }

    public boolean isAutomatic() {
        return config.isAutomatic();
    }

    public String getCommand() {
        return config.getCommand();
    }

}
