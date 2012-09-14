package models.message;

import models.config.PhaseConfig;
import orchestration.PipeVersion;

import org.joda.time.ReadableDateTime;

import executor.TaskExecutionContext;
import executor.TaskResult;

/**
 * Use the factory methods.
 * 
 * @author marcus
 */
public class TaskStatus extends StatusMessage {

    // TODO Add task details field

    /** Use the factory method */
    private TaskStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished) {
        super(status, version, phase, started, finished);
    }

    /** Use the factory method */
    private TaskStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started) {
        super(status, version, phase, started, null);
    }

    public static TaskStatus newRunningTaskStatus(TaskExecutionContext context) {
        return new TaskStatus(Status.RUNNING, context.getVersion(), context.getPhase(), context.getStarted());
    }

    public static TaskStatus newFinishedTaskStatus(TaskResult result) {
        TaskExecutionContext context = result.getContext();
        return new TaskStatus(Status.status(result.success()), context.getVersion(), context.getPhase(),
                context.getStarted(), context.getFinished());
    }
}
