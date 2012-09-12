package orchestration;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.PipeValidationException;
import models.config.TaskConfig;
import utils.PipeConfReader;
import executor.ExecutionContext;
import executor.TaskCallback;
import executor.TaskExecutor;
import executor.TaskResult;

/**
 * Service/Controller that orchestrates the pipe execution.
 * 
 * <ul>
 *  <li>Uses the Executor to execute tasks.</li>
 *  <li>Notifies the client of status changes.</li>
 *  <li>Notifies persistence manager of status changes.</li>
 * </ul>
 * 
 * @author marcus
 */
public class Orchestrator implements TaskCallback {

    private static final PipeConfReader configReader = PipeConfReader.getInstance();

    /** Start first task of first phase of pipe */
    public void start(String pipeName) throws PipeValidationException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getFirstPhaseConfig();
        TaskConfig task = phase.getInitialTask();
        startTask(task, phase, pipe);
    }

    /** Try to start given task in given phase and pipe. */
    public void startTask(String taskName, String phaseName, String pipeName) throws PipeValidationException {
        PipeConfig pipe = getPipe(pipeName);
        PhaseConfig phase = pipe.getPhaseByName(phaseName);
        TaskConfig task = phase.getTaskByName(taskName);
        startTask(task, phase, pipe);
    }

    private void startTask(TaskConfig taskConfig, PhaseConfig phaseConfig, PipeConfig pipeConfig) {
        // TODO: Implement main logic
        // Pipe version...
        ExecutionContext context = new ExecutionContext();

        TaskExecutor.getInstance().execute(taskConfig, context, this);

        // notify via handler?

    }

    @Override
    public void receiveTaskResult(TaskResult result) {
        // TODO notify via handler

    }

    private PipeConfig getPipe(String pipeName) throws PipeValidationException {
        return configReader.get(pipeName);
    }
}
