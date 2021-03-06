package models.message;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.TaskConfig;

import org.joda.time.ReadableDateTime;

import executor.TaskExecutionContext;
import executor.TaskResult;

/**
 * Use the factory methods.
 * 
 * @author marcus
 */
public class TaskStatus extends PhaseStatus {

    private final TaskConfig task;
    private final String out;
    private final String err;

    public static TaskStatus newRunningTaskStatus(TaskExecutionContext context) {
        return new TaskStatus(State.RUNNING, context.getPipeVersion(), context.getPhase(),
                context.getTask(), context.getStarted(), null, null, null);
    }

    public static TaskStatus newFinishedTaskStatus(TaskResult result) {
        TaskExecutionContext context = result.context();
        return new TaskStatus(State.state(result.success()), context.getPipeVersion(),
                context.getPhase(), context.getTask(), context.getStarted(), context.getFinished(),
                result.out(), result.err());
    }

    public static TaskStatus newPendingTaskStatus(TaskExecutionContext context) {
        return new TaskStatus(State.PENDING, context.getPipeVersion(), context.getPhase(),
                context.getTask(), null, null, null, null);
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }

    public String getTaskName() {
        return task.getTaskName();
    }

    /** Use the factory methods */
    private TaskStatus(State state, PipeVersion version, PhaseConfig phase, TaskConfig task,
            ReadableDateTime started, ReadableDateTime finished, String out, String err) {
        super(version, phase, state, started, finished);
        this.task = task;
        this.out = out;
        this.err = err;
    }

    public boolean isPending() {
        return getState() == State.PENDING;
    }

}
