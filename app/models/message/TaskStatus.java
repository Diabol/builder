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

    private final String out;
    private final String err;

    public static TaskStatus newRunningTaskStatus(TaskExecutionContext context) {
        return new TaskStatus(Status.RUNNING, context.getVersion(), context.getPhase(), context.getStarted(), null,
                null, null);
    }

    public static TaskStatus newFinishedTaskStatus(TaskResult result) {
        TaskExecutionContext context = result.context();
        return new TaskStatus(Status.status(result.success()), context.getVersion(), context.getPhase(),
                context.getStarted(), context.getFinished(), result.out(), result.err());
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }

    /** Use the factory methods */
    private TaskStatus(Status status, PipeVersion<?> version, PhaseConfig phase, ReadableDateTime started,
            ReadableDateTime finished, String out, String err) {
        super(status, version, phase, started, finished);
        this.out = out;
        this.err = err;
    }

}
