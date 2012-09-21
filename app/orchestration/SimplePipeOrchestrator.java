package orchestration;

import java.util.List;

import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import notification.PipeNotificationHandler;
import play.Logger;
import executor.TaskExecutionContext;
import executor.TaskResult;

/**
 * Autor: Daniel Grönberg
 */
public class SimplePipeOrchestrator implements Runnable {
    private final PipeConfig pipe;
    private final String version;

    public SimplePipeOrchestrator(PipeConfig pipe, String version) {
        this.pipe = pipe;
        this.version = version;
    }

    @Override
    public void run() {
    	persistNewPipe();
        for (PhaseConfig phase : pipe.getPhases()) {
        	TaskExecutionContext context = new TaskExecutionContext(null, pipe, phase, new PipeStringVersion(version, pipe));
        	context.startedNow();
        	PhaseStatus ongoingPhase = PhaseStatus.newRunningPhaseStatus(context);
            PipeNotificationHandler.getInstance().notifyPhaseStatusListeners(ongoingPhase);
        	for(TaskConfig task: phase.getTasks()) {
	            TaskExecutionContext taskContext = new TaskExecutionContext(task, pipe, phase, new PipeStringVersion(version, pipe));
	            taskContext.startedNow();
	            TaskStatus ongoing = TaskStatus.newRunningTaskStatus(taskContext);
	            PipeNotificationHandler.getInstance().notifyTaskStatusListeners(ongoing); 
	            try {
	                Thread.sleep(500);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	            taskContext.finishedNow();
				TaskResult result = new TaskResult(true, taskContext);
				TaskStatus taskStatus = TaskStatus.newFinishedTaskStatus(result);
				PipeNotificationHandler.getInstance().notifyTaskStatusListeners(taskStatus);
        	}
        	context.finishedNow();
            PhaseStatus status = PhaseStatus.newFinishedPhaseStatus(context, true);
            PipeNotificationHandler.getInstance().notifyPhaseStatusListeners(status);
        }
        List<Pipe> savedPipes = Pipe.find.all();
        for(Pipe pipe: savedPipes) {
        	Logger.error(pipe.toString());
        }
    }

	private void persistNewPipe() {
		//Create the pipe
		Pipe pipeData = Pipe.createNewFromConfig(version, pipe);
		pipeData.save();
		//Create the phases belonging to the pipes
		for(PhaseConfig phaseConf: pipe.getPhases()) {
			//Create phase
			Phase phase = Phase.createNewFromConfig(phaseConf);
			phase.pipe = pipeData;
			phase.save();
			for(TaskConfig taskConf: phaseConf.getTasks()){
				Task task = Task.createNewFromConfig(taskConf);
				task.phase = phase;
				task.save();
			}
		}
	}
	
	private void updateTaskStatus(TaskResult taskResult) {
		/*
	     * TODO: Find and update status for every Task and Phase
	     */
	}
}
