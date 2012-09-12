package orchestration;

import models.config.PhaseConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;

/**
 * An instance of a phase, i.e. a specific run of a phase in a pipeline.
 * Config is taken from {@link PhaseConfig}.
 * Result is stored in {@link PhaseResult}.
 * 
 * @author marcus
 */
class Phase {

    // TODO This logic should be aligned with new Orchestrator or logic.

    private final PhaseConfig config;
    private PhaseResult result;

    Phase(PhaseConfig config) {
        this.config = config;
    }

    PhaseResult start() throws PipeValidationException {
        result = new PhaseResult(this);
//        runTask(config.getInitialTask());
        return result;
    }

    private void runTask(TaskConfig taskConfig) throws PipeValidationException{
//        Task task = taskConfig.createTask();
//        TaskResult taskResult = task.start();
//        result.addTaskResult(taskResult);
//        if (!taskResult.success()) {
//            return;
//        }
//        List<String> nextTasks = taskConfig.getTriggersTasks();
//        for (String nextTaskName : nextTasks) {
//            // TODO Add isAutomatic check
//            runTask(config.getTaskByName(nextTaskName));
//        }
    }

    public String getName() {
        return config.getName();
    }

}
