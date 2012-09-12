package models.execution;

import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;
import models.result.PhaseResult;
import models.result.TaskResult;
import play.Play;

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

    public PhaseResult start() throws PipeValidationException {
        result = new PhaseResult(this);
        runTask(config.getInitialTask());
        return result;
    }

    private void runTask(TaskConfig taskConfig) throws PipeValidationException{
        Task task = taskConfig.createTask();
        TaskResult taskResult = task.start();
        result.addTaskResult(taskResult);
        if (!taskResult.success()) {
            return;
        }
        List<String> nextTasks = task.getNextTasks();
        for (String nextTaskName : nextTasks) {
            // TODO Add isAutomatic check
            runTask(config.getTaskByName(nextTaskName));
        }
    }

    public String getName() {
        return config.getName();
    }

}
