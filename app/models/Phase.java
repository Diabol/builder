package models;

import java.util.List;

import models.config.PhaseConfig;
import models.config.TaskConfig;
import models.result.PhaseResult;
import models.result.TaskResult;

/**
 * An instance of a phase, i.e. a specific run of a phase in a pipeline.
 * Config is taken from {@link PhaseConfig}.
 * Result is stored in {@link PhaseResult}.
 * 
 * @author marcus
 */
public class Phase {

    private final PhaseConfig config;
    private PhaseResult result;

    public Phase(PhaseConfig config) {
        this.config = config;
    }

    public PhaseResult start() {
        result = new PhaseResult(this);
        runTask(getInitialTaskConfig());
        return result;
    }

    private void runTask(TaskConfig taskConfig) {
        Task task = taskConfig.createTask();
        TaskResult taskResult = task.start();
        result.addTaskResult(taskResult);
        if (!taskResult.success()) {
            return;
        }
        List<TaskConfig> nextTasks = task.getNextTasks();
        for (TaskConfig nextTask : nextTasks) {
            // TODO Add isAutomatic check
            runTask(nextTask);
        }
    }

    public String getName() {
        return config.getName();
    }

    public TaskConfig getInitialTaskConfig() {
        return config.getInitialTask();
    }

}
