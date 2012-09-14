package orchestration;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PipeNotificationHandler;
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
 * <li>Notifies the client of status changes.</li>
 * <li>Notifies persistence manager of status changes.</li>
 * </ul>
 * 
 * @author marcus
 */
public class Orchestrator implements TaskCallback {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    /** Start first task of first phase of pipe */
    public PipeVersion<?> start(String pipeName) throws PipeValidationException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();

        PipeVersion<?> version = getNextPipeVersion(pipe);

        startTask(task, phase, pipe, version);

        return version;
    }

    /** Try to start given task in given phase and pipe version. */
    public void startTask(String taskName, String phaseName, String pipeName, String pipeVersion)
            throws PipeValidationException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getPhaseByName(phaseName);
        TaskConfig task = phase.getTaskByName(taskName);
        PipeVersion<?> version = createPipeVersion(pipe, pipeVersion);
        startTask(task, phase, pipe, version);
    }

    private PipeVersion<?> getNextPipeVersion(PipeConfig pipe) {
        // TODO Auto-generated method stub
        return null;
    }

    private PipeVersion<?> createPipeVersion(PipeConfig pipe, String pipeVersion) throws PipeVersionValidationException {
        // TODO: Here we could look up the version implementation we would like to use from config...
        return new PipeStringVersion(pipeVersion, pipe);
    }

    private void startTask(TaskConfig taskConfig, PhaseConfig phaseConfig, PipeConfig pipeConfig,
            PipeVersion<?> pipeVersion) {
        // TODO: Persist new state
        TaskExecutionContext context = new TaskExecutionContext(taskConfig, pipeConfig, phaseConfig, pipeVersion);
        TaskExecutor.getInstance().execute(context, this);
    }

    @Override
    public void handleTaskStarted(TaskExecutionContext context) {
        TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        handler.recieveTaskStatusChanged(taskStatus);
        if (isNewPhaseStatus(context)) {
            PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
            handler.recievePhaseStatusChanged(phaseStatus);
        }
    }

    @Override
    public void handleTaskResult(TaskResult result) {
        TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        handler.recieveTaskStatusChanged(taskStatus);

        PhaseStatus phaseStatus = getNewPhaseStatus(result);
        if (phaseStatus != null) {
            handler.recievePhaseStatusChanged(phaseStatus);
        }
    }

    private boolean isNewPhaseStatus(TaskExecutionContext context) {
        // TODO Implement logic to compute if this means new phase status.
        // Phase changes status when:
        //   1. First task starts
        //   2. Any task fails
        //   3. All tasks finished successful (there is no 'last' task)
        return true;
    }

    /**
     * @return new {@link PhaseStatus}, null if no status change.
     */
    private PhaseStatus getNewPhaseStatus(TaskResult latestTaskResult) {
        // TODO Implement. See isNewPhaseStatus above
        return PhaseStatus.newFinishedPhaseStatus(latestTaskResult.context(), true);
    }

    private PipeConfig getPipe(String pipeName) throws PipeValidationException {
        return configReader.get(pipeName);
    }
}
