package executor;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import notification.PipeNotificationHandler;
import orchestration.PipeVersion;

/**
 * Encapsulates the information that the {@link Task} need to execute a task and
 * receives callbacks from the task which are sent to PipeNotificationHandler.
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
    public void receiveTaskStarted() {
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        // TODO Create TaskStatus and PhaseStatus and notify
    }

    @Override
    public void receiveTaskResult(TaskResult result) {
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        // TODO Create TaskStatus and PhaseStatus and notify

    }

    public TaskConfig getTaskConfig() {
        return task;
    }

}
