package models.message;

import models.config.PhaseConfig;
import models.config.TaskConfig;
import orchestration.PipeVersion;

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
        return new TaskStatus(State.RUNNING, context.getVersion(), context.getPhase(), context.getTask(),
                context.getStarted(), null, null, null);
    }

    public static TaskStatus newFinishedTaskStatus(TaskResult result) {
        TaskExecutionContext context = result.context();
        return new TaskStatus(State.state(result.success()), context.getVersion(), context.getPhase(),
                context.getTask(), context.getStarted(), context.getFinished(), result.out(), result.err());
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

    /** Use the factory methods
     * @param task TODO*/
    private TaskStatus(State state, PipeVersion<?> version, PhaseConfig phase, TaskConfig task,
            ReadableDateTime started, ReadableDateTime finished, String out, String err) {
        super(version, phase, state, started, finished);
        this.task = task;
        this.out = out;
        this.err = err;
    }

}
