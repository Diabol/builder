package executor;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import orchestration.PipeVersion;

/**
 * Encapsulates the information that the Executor need to execute a task that is
 * not in task itself.
 * 
 * @author marcus
 */
public class ExecutionContext implements TaskCallback {

    private final TaskConfig task;
    private PipeConfig pipe;
    private PhaseConfig phase;
    private PipeVersion<?> version;

    public ExecutionContext(TaskConfig task, PipeConfig pipe, PhaseConfig phase, PipeVersion<?> version) {
        // TODO Rest of fields
        this.task = task;
    }
    @Override
    public void receiveTaskStarted(ExecutionContext context) {
        // TODO notify via handler

    }

    @Override
    public void receiveTaskResult(TaskResult result) {
        // TODO notify via handler

    }

    public TaskConfig getTaskConfig() {
        return task;
    }

}
