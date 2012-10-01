package orchestration;

import java.util.List;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PipeNotificationHandler;
import utils.DBHelper;
import utils.PipeConfReader;
import executor.TaskCallback;
import executor.TaskExecutionContext;
import executor.TaskExecutor;
import executor.TaskResult;

/**
 * Service/Controller that orchestrates the pipe execution.
 * 
 * <ul>
 * <li>Uses the Executor to execute tasks.</li>
 * <li>Manages/creates pipe versions</li>
 * <li>Starts tasks triggered by finished tasks.</li>
 * <li>Notifies the client of status changes.</li>
 * <li>Notifies persistence manager of status changes.</li>
 * </ul>
 * 
 * @author marcus
 */
public class Orchestrator implements TaskCallback {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();
    private static DBHelper dbHelper = DBHelper.getInstance();
    private static PipeNotificationHandler notifictionHandler = PipeNotificationHandler
            .getInstance();

    /**
     * For test
     * 
     * @param dbHelper
     */
    public void setDBHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setPipeNotificationHandler(PipeNotificationHandler notifictionHandler) {
        this.notifictionHandler = notifictionHandler;
    }

    /** Start first task of first phase of pipe */
    public PipeVersion start(String pipeName) {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();

        PipeVersion version = getNextPipeVersion(pipe);
        dbHelper.persistNewPipe(version, pipe);
        startTask(task, phase, pipe, version);

        return version;
    }

    /** Try to start given task in given phase and pipe version. */
    public void startTask(String taskName, String phaseName, String pipeName, String pipeVersion) {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getPhaseByName(phaseName);
        TaskConfig task = phase.getTaskByName(taskName);
        PipeVersion version = PipeVersion.fromString(pipeVersion, pipe);
        startTask(task, phase, pipe, version);
    }

    private PipeVersion getNextPipeVersion(PipeConfig pipe) {
        // TODO Implement getNextPipeVersion. We need to check persistence...
        return PipeVersion.fromString("0.1.2", pipe);
    }

    private void startTask(TaskConfig task, PhaseConfig phase, PipeConfig pipe, PipeVersion version) {
        TaskExecutionContext executionContext = new TaskExecutionContext(task, pipe, phase, version);
        startTask(executionContext);
    }

    private void startTask(TaskExecutionContext executionContext) {
        TaskExecutor.getInstance().execute(executionContext, this);
    }

    @Override
    public void handleTaskStarted(TaskExecutionContext context) {
        TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
        if (isNewPhaseStatus(context, taskStatus)) {
            PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
            dbHelper.updatePhaseToOngoing(phaseStatus);
            notifictionHandler.notifyPhaseStatusListeners(phaseStatus);
            if (isContextForFirstTaskOfPipe(context)) {
                dbHelper.updatePipeToOnging(context);
            }
        }
        dbHelper.updateTaskToOngoing(taskStatus);
        notifictionHandler.notifyTaskStatusListeners(taskStatus);

    }

    @Override
    public void handleTaskResult(TaskResult result) {
        TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
        dbHelper.updateTaskToFinished(taskStatus);
        notifictionHandler.notifyTaskStatusListeners(taskStatus);

        PhaseStatus phaseStatus = getNewFinishedPhaseStatus(result);
        if (phaseStatus != null) {
            dbHelper.updatePhaseToFinished(phaseStatus);
            notifictionHandler.notifyPhaseStatusListeners(phaseStatus);
        }

        startNextTask(result);
    }

    /**
     * Decide if we should start a new task and then start it.
     */
    private void startNextTask(TaskResult lastTaskResult) {
        // 1. Check if task was successful
        if (!lastTaskResult.success()) {
            return;
        }
        // 2. Start triggered tasks if automatic
        TaskExecutionContext taskContext = lastTaskResult.context();
        List<TaskConfig> triggeredTasks = taskContext.getTriggedTasks();
        for (TaskConfig task : triggeredTasks) {
            if (task.isAutomatic()) {
                startTask(task, taskContext.getPhase(), taskContext.getPipe(),
                        taskContext.getPipeVersion());
            }
        }
        // 3. Trigger first task in next phase if all tasks in this phase
        // finished and auto.
        if (allTasksInPhaseFinishedSuccessfully(taskContext)) {
            TaskExecutionContext newTaskContext = taskContext.getFirstTaskInNextPhase();
            if (newTaskContext != null) {
                if (newTaskContext.getTask().isAutomatic()) {
                    startTask(newTaskContext);
                }
            }
        }
    }

    private boolean allTasksInPhaseFinishedSuccessfully(TaskExecutionContext taskContext) {
        // TODO: Fix later.
        // for (Task task : dbHelper.getPhaseForContext(taskContext).tasks) {
        // if (task.state != State.SUCCESS) {
        // return false;
        // }
        // }
        return false;
    }

    private boolean isNewPhaseStatus(TaskExecutionContext context, TaskStatus taskStatus) {
        TaskConfig currentTask = context.getTask();
        boolean firstTaskJustStarted = currentTask.equals(context.getPhase().getInitialTask())
                && taskStatus.isRunning();
        boolean taskSuccceeded = taskStatus.isSuccess();
        return firstTaskJustStarted || !taskSuccceeded
                || allTasksInPhaseFinishedSuccessfully(context);
    }

    private boolean isContextForFirstTaskOfPipe(TaskExecutionContext context) {
        TaskConfig task = context.getTask();
        return task == context.getPipe().getPhases().get(0).getInitialTask();
    }

    /**
     * @return new finsihed {@link PhaseStatus}, success or fail, null if no
     *         status change.
     */
    private PhaseStatus getNewFinishedPhaseStatus(TaskResult latestTaskResult) {
        // TODO Implement. See isNewPhaseStatus(context) above
        boolean success = true;
        return PhaseStatus.newFinishedPhaseStatus(latestTaskResult.context(), success);
    }

    private PipeConfig getPipe(String pipeName) {
        return configReader.get(pipeName);
    }
}
