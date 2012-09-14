package orchestration;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;
import utils.PipeConfReader;
import executor.TaskExecutionContext;
import executor.TaskExecutor;

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
public class Orchestrator {

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

    private static PipeVersion<?> createPipeVersion(PipeConfig pipe, String pipeVersion)
            throws PipeVersionValidationException {
        return new PipeStringVersion(pipeVersion, pipe);
    }

    private void startTask(TaskConfig taskConfig, PhaseConfig phaseConfig, PipeConfig pipeConfig,
            PipeVersion<?> pipeVersion) {
        // TODO: Persistence...
        TaskExecutionContext context = new TaskExecutionContext(taskConfig, pipeConfig, phaseConfig, pipeVersion);
        TaskExecutor.getInstance().execute(context);
    }

    private PipeConfig getPipe(String pipeName) throws PipeValidationException {
        return configReader.get(pipeName);
    }
}