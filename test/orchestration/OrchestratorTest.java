package orchestration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import notification.PipeNotificationHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import utils.DBHelper;
import utils.PipeConfReader;
import executor.TaskExecutionContext;
import executor.TaskResult;

public class OrchestratorTest {
    DBHelper dbHelper;
    PipeNotificationHandler notificationHandler;
    PipeConfReader confReader;
    Orchestrator orchestrator;
    PipeConfig pipeConf;
    PipeVersion version;
    String stringVersion = "1";

    @Before
    public void prepare() {
        dbHelper = mock(DBHelper.class);
        notificationHandler = mock(PipeNotificationHandler.class);
        orchestrator = new Orchestrator();
        orchestrator.setDBHelper(dbHelper);
        orchestrator.setPipeNotificationHandler(notificationHandler);
        confReader = PipeConfReader.getInstance();
        // Verify against the first configured pipe.
        pipeConf = confReader.getConfiguredPipes().get(0);
        version = PipeVersion.fromString(stringVersion, pipeConf);
    }

    @Test
    public void testHandleTaskStartedNotifiesAndPersistsOngoinStatusForTaskPhaseAndPipe() {
        TaskExecutionContext context = createContextForFirstTask();
        context.startedNow();
        orchestrator.handleTaskStarted(context);

        TaskStatus expectedTaskStatus = TaskStatus.newRunningTaskStatus(context);
        verifyTaskStatusForDBHelperAndNotificationHandler(expectedTaskStatus);

        PhaseStatus expectedPhaseStatus = PhaseStatus.newRunningPhaseStatus(context);
        verifyPhaseStatusForDBHelperAndNotificationHandler(expectedPhaseStatus);

        verify(dbHelper).updatePipeToOnging(version);
    }

    @Test
    public void testHandleTaskResultFailureSetsFailureOnTaskPhaseAndPipe() {
        TaskExecutionContext context = createContextForFirstTask();
        context.finishedNow();
        TaskResult failure = new TaskResult(false, context);
        orchestrator.handleTaskResult(failure);

        TaskStatus failedTaskStatus = TaskStatus.newFinishedTaskStatus(failure);
        verifyTaskStatusForDBHelperAndNotificationHandler(failedTaskStatus);

        PhaseStatus failedPhaseStatus = PhaseStatus.newFinishedPhaseStatus(context, false);
        verifyPhaseStatusForDBHelperAndNotificationHandler(failedPhaseStatus);

        verify(dbHelper).updatePipeToFinished(version, false);
    }

    @Test
    public void testHandleTaskResultSuccessSetsSuccessOnTaskAndStartsNextAutomaticTasks() {

    }

    @Test
    public void testHandleTaskResultSuccessSetsSuccessOnPhaseAndTaskWhenAllTasksInPhaseSuccessful() {

    }

    @Test
    public void testHandleTaskResultSuccessSetsSuccessOnPipeWhenAllPhasesAreSuccessfull() {

    }

    @Test
    public void testHandleTaskResultSuccessStartsFirstTaskOfNextPhaseWhenAllTasksAreSuccessful() {

    }

    private TaskExecutionContext createContextForFirstTask() {
        PhaseConfig firstPhase = pipeConf.getPhases().get(0);
        TaskConfig firstTask = firstPhase.getInitialTask();

        TaskExecutionContext context = new TaskExecutionContext(firstTask, pipeConf, pipeConf
                .getPhases().get(0), version);
        return context;
    }

    private void verifyPhaseStatusForDBHelperAndNotificationHandler(PhaseStatus expected) {
        ArgumentCaptor<PhaseStatus> dbHelpCapt = ArgumentCaptor.forClass(PhaseStatus.class);
        ArgumentCaptor<PhaseStatus> notifyCapt = ArgumentCaptor.forClass(PhaseStatus.class);
        if (expected.isRunning()) {
            verify(dbHelper).updatePhaseToOngoing(dbHelpCapt.capture());
            assertEquals(expected.getStarted(), dbHelpCapt.getValue().getStarted());
        } else {
            verify(dbHelper).updatePhaseToFinished(dbHelpCapt.capture());
            assertEquals(expected.getFinished(), dbHelpCapt.getValue().getFinished());
        }
        verify(notificationHandler).notifyPhaseStatusListeners(notifyCapt.capture());
        assertEquals(expected.getState(), dbHelpCapt.getValue().getState());
        assertEquals(expected.getVersion(), stringVersion);
        assertEquals(expected.getPhaseName(), dbHelpCapt.getValue().getPhaseName());
        assertEquals(dbHelpCapt.getValue(), notifyCapt.getValue());
    }

    private void verifyTaskStatusForDBHelperAndNotificationHandler(TaskStatus expected) {
        ArgumentCaptor<TaskStatus> dbHelpCapt = ArgumentCaptor.forClass(TaskStatus.class);
        ArgumentCaptor<TaskStatus> notifyCapt = ArgumentCaptor.forClass(TaskStatus.class);
        if (expected.isRunning()) {
            verify(dbHelper).updateTaskToOngoing(dbHelpCapt.capture());
            assertEquals(expected.getStarted(), dbHelpCapt.getValue().getStarted());
        } else {
            verify(dbHelper).updateTaskToFinished(dbHelpCapt.capture());
            assertEquals(expected.getFinished(), dbHelpCapt.getValue().getFinished());
        }
        verify(notificationHandler).notifyTaskStatusListeners(notifyCapt.capture());
        assertEquals(expected.getState(), dbHelpCapt.getValue().getState());
        assertEquals(expected.getTaskName(), dbHelpCapt.getValue().getTaskName());
        assertEquals(expected.getVersion(), stringVersion);
        assertEquals(expected.getPhaseName(), dbHelpCapt.getValue().getPhaseName());
        assertEquals(dbHelpCapt.getValue(), notifyCapt.getValue());
    }
}
