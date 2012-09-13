package executor;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import orchestration.PipeVersion;

/**
 * Encapsulates the information that the {@link Task} need to execute a task and
 * receieves callbacks from the task which are sent to PipeNotificationHandler.
 * 
 * @author marcus
 */
public class TaskExecutionContext implements TaskCallback {

    private final TaskConfig task;
    private PipeConfig pipe;
    private PhaseConfig phase;
    private PipeVersion<?> version;

    public TaskExecutionContext(TaskConfig task, PipeConfig pipe, PhaseConfig phase, PipeVersion<?> version) {
        this.task = task;
        // TODO Rest of fields
    }

    @Override
    public void receiveTaskStarted(TaskExecutionContext context) {
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
