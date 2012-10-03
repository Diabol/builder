package orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import models.PipeVersion;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import notification.PipeNotificationHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import utils.DBHelper;
import utils.PipeConfReader;
import executor.TaskExecutionContext;
import executor.TaskExecutor;
import executor.TaskResult;

@RunWith(MockitoJUnitRunner.class)
public class OrchestratorTest {
    @Mock
    DBHelper dbHelper;
    @Mock
    PipeNotificationHandler notificationHandler;
    @Mock
    PipeConfReader confReader;
    Orchestrator orchestrator;
    @Mock
    TaskExecutor taskExecutor;
    PipeConfig pipeConf;
    PipeVersion version;
    String stringVersion = "1";

    @Before
    public void prepare() {
        orchestrator = new Orchestrator();
        orchestrator.setDBHelper(dbHelper);
        orchestrator.setPipeNotificationHandler(notificationHandler);
        orchestrator.setTaskexecutor(taskExecutor);
        orchestrator.setPipeConfigReader(confReader);
        pipeConf = mockConfig();
        when(confReader.get("ThePipe")).thenReturn(pipeConf);
        version = PipeVersion.fromString(stringVersion, pipeConf);
    }

    @After
    public void cleanUp() {
        orchestrator.setDBHelper(DBHelper.getInstance());
        orchestrator.setPipeConfigReader(PipeConfReader.getInstance());
        orchestrator.setPipeNotificationHandler(PipeNotificationHandler.getInstance());
        orchestrator.setTaskexecutor(TaskExecutor.getInstance());
    }

    @Test
    public void testHandleTaskStartedNotifiesAndPersistsOngoinStatusForTaskPhaseAndPipe() {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
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
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
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
    public void testHandleTaskResultSuccessSetsSuccessOnPhaseAndTaskWhenAllTasksInPhaseSuccessful() {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase phaseWithAllTasksSuccess = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());

        when(dbHelper.getPhaseForContext(context)).thenReturn(phaseWithAllTasksSuccess);

        orchestrator.handleTaskResult(success);

        TaskStatus successTaskStatus = TaskStatus.newFinishedTaskStatus(success);
        verifyTaskStatusForDBHelperAndNotificationHandler(successTaskStatus);

        PhaseStatus successPhaseStatus = PhaseStatus.newFinishedPhaseStatus(context, true);
        verifyPhaseStatusForDBHelperAndNotificationHandler(successPhaseStatus);
    }

    @Test
    public void testHandleTaskResultSuccessSetsSuccessOnPipeWhenAllPhasesAreSuccessfull()
            throws Exception {

        PhaseConfig lastPhaseConf = pipeConf.getPhases().get(pipeConf.getPhases().size() - 1);
        TaskExecutionContext context = createContextForFirstTaskInPhase(lastPhaseConf);
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        Phase lastPhase = createSuccessfullPhase(lastPhaseConf);
        when(dbHelper.getPhaseForContext(context)).thenReturn(lastPhase);

        Pipe pipeWithAllPhasesSuccess = Pipe.createNewFromConfig(stringVersion, pipeConf);
        pipeWithAllPhasesSuccess.phases.add(firstPhase);
        pipeWithAllPhasesSuccess.phases.add(lastPhase);

        when(dbHelper.getPipe(version)).thenReturn(pipeWithAllPhasesSuccess);

        orchestrator.handleTaskResult(success);

        TaskStatus successTaskStatus = TaskStatus.newFinishedTaskStatus(success);
        verifyTaskStatusForDBHelperAndNotificationHandler(successTaskStatus);

        PhaseStatus successPhaseStatus = PhaseStatus.newFinishedPhaseStatus(context, true);
        verifyPhaseStatusForDBHelperAndNotificationHandler(successPhaseStatus);

        verify(dbHelper).updatePipeToFinished(version, true);
    }

    @Test
    public void testHandleTaskResultSuccessStartsFirstTaskOfNextPhaseWhenAllTasksAreSuccessful() {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);

        orchestrator.handleTaskResult(success);

        ArgumentCaptor<TaskExecutionContext> taskExecCapt = ArgumentCaptor
                .forClass(TaskExecutionContext.class);
        verify(taskExecutor).execute(taskExecCapt.capture(), (Orchestrator) Mockito.notNull());
        // Assert that the next task to execute is the first of the second
        // phase.
        assertTrue(taskExecCapt.getValue().getTask().getTaskName()
                .equals(pipeConf.getPhases().get(1).getInitialTask().getTaskName()));
    }

    @Test
    public void testHandleTaskResultSuccessStartsNextAutomaticTaskAndNoManual() {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);
        Phase firstPhase = createPhaseFromConf(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(0).finishNow(true);
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);

        orchestrator.handleTaskResult(success);

        ArgumentCaptor<TaskExecutionContext> taskExecCapt = ArgumentCaptor
                .forClass(TaskExecutionContext.class);
        verify(taskExecutor).execute(taskExecCapt.capture(), (Orchestrator) Mockito.notNull());
        assertTrue(taskExecCapt.getValue().getTask().getTaskName().equals("SecondAutomatic"));
    }

    private Phase createPhaseFromConf(PhaseConfig phaseConfig) {
        Phase phase = Phase.createNewFromConfig(phaseConfig);
        for (TaskConfig taskConf : phaseConfig.getTasks()) {
            Task task = Task.createNewFromConfig(taskConf);
            phase.tasks.add(task);
        }
        return phase;
    }

    private Phase createSuccessfullPhase(PhaseConfig phaseConfig) {
        Phase phase = createPhaseFromConf(phaseConfig);
        for (Task task : phase.tasks) {
            task.finishNow(true);
        }
        phase.finishNow(true);
        return phase;
    }

    private TaskExecutionContext createContextForFirstTaskInPhase(PhaseConfig phase) {
        TaskConfig firstTask = phase.getInitialTask();

        TaskExecutionContext context = new TaskExecutionContext(firstTask, pipeConf, phase, version);
        return context;
    }

    private TaskExecutionContext createContextForLastTaskInFirstPhase() {
        PhaseConfig firstPhase = pipeConf.getPhases().get(0);
        TaskConfig lastTask = firstPhase.getTasks().get(firstPhase.getTasks().size() - 1);

        TaskExecutionContext context = new TaskExecutionContext(lastTask, pipeConf, firstPhase,
                version);
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

    private PipeConfig mockConfig() {
        PipeConfig config = new PipeConfig("ThePipe");
        List<PhaseConfig> phaseList = new ArrayList<PhaseConfig>();
        phaseList.add(creatFirstPhase());
        phaseList.add(createSecondPhase());
        config.setPhases(phaseList);
        return config;
    }

    private PhaseConfig createSecondPhase() {
        PhaseConfig secondPhase = new PhaseConfig("SecondPhase");
        TaskConfig onlyAutomaticTask = new TaskConfig("OnlyManualTask", "deploy", true);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(onlyAutomaticTask);
        secondPhase.setTasks(taskList);
        return secondPhase;
    }

    private PhaseConfig creatFirstPhase() {
        PhaseConfig firstPhase = new PhaseConfig("FirstPhase");
        TaskConfig firstTask = new TaskConfig("FirstTask", "echo", true);
        TaskConfig secondAutomatic = new TaskConfig("SecondAutomatic", "echo", true);
        TaskConfig secondManual = new TaskConfig("SecondManual", "echo", false);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(secondAutomatic.getTaskName());
        firstTaskTriggers.add(secondManual.getTaskName());
        firstTask.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(firstTask);
        taskList.add(secondManual);
        taskList.add(secondAutomatic);
        firstPhase.setTasks(taskList);
        return firstPhase;
    }
}