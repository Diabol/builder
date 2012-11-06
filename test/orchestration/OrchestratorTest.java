package orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import models.PipeVersion;
import models.StatusInterface.State;
import models.config.PhaseConfig;
import models.config.PipeConfig;
import models.config.TaskConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Committer;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import models.statusdata.VersionControlInfo;
import notification.PipeNotificationHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import utils.DBHelper;
import utils.DataInconsistencyException;
import utils.DataNotFoundException;
import utils.LogHandler;
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
    @Mock
    LogHandler logHandler;
    Orchestrator orchestrator;
    @Mock
    TaskExecutor taskExecutor;
    PipeConfig pipeConf;
    PipeVersion version;
    Pipe runningPipe;
    Pipe failedPipe;
    Pipe successfullPipe;
    String stringVersion = "1";
    Committer committer = new Committer("John", "john@company.com");
    private final VersionControlInfo firstCommit = new VersionControlInfo("#1", "FirstCommit",
            committer);

    @Before
    public void prepare() throws Exception {
        orchestrator = new Orchestrator(confReader, dbHelper, notificationHandler, taskExecutor,
                logHandler);
        pipeConf = mockConfig();
        version = PipeVersion.fromString(stringVersion, firstCommit, pipeConf);
        runningPipe = Pipe.createNewFromConfig(version.getVersion(), pipeConf, firstCommit);
        runningPipe.startNow();
        failedPipe = Pipe.createNewFromConfig(version.getVersion(), pipeConf,
                new VersionControlInfo("#2", "SecondCommit", committer));
        failedPipe.finishNow(false);
        successfullPipe = Pipe.createNewFromConfig(version.getVersion(), pipeConf,
                new VersionControlInfo("#3", "ThirdCommit", committer));
        successfullPipe.finishNow(true);
        when(confReader.get("ThePipe")).thenReturn(pipeConf);
    }

    @Test
    public void testStartPipeForTheFirstTimePersistsNewVersionOfPipeAndNotifiesAboutNewVersion()
            throws Exception {
        when(dbHelper.getLatestPipe(pipeConf)).thenThrow(new DataNotFoundException(""));
        orchestrator.start("ThePipe", firstCommit);
        verify(dbHelper).persistNewPipe(version, pipeConf);
        verify(notificationHandler).notifyNewVersionOfPipe(version);
    }

    @Test
    public void testStartPipeIncrementsVersionWhenEarlierExists() throws Exception {
        Pipe persisted = Pipe.createNewFromConfig(version.getVersion(), pipeConf, firstCommit);
        when(dbHelper.getLatestPipe(pipeConf)).thenReturn(persisted);
        VersionControlInfo secondCommit = new VersionControlInfo("#2", "SecondCommit", committer);
        orchestrator.start("ThePipe", secondCommit);
        PipeVersion newVersion = PipeVersion.fromString("2", secondCommit, pipeConf);
        verify(dbHelper).persistNewPipe(newVersion, pipeConf);
        verify(notificationHandler).notifyNewVersionOfPipe(newVersion);
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
    public void testHandleTaskResultSuccessSetsSuccessOnPhaseAndTaskWhenAllBlockingTasksInPhaseSuccessful()
            throws Exception {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase phaseWithAllTasksSuccess = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());

        when(dbHelper.getPhaseForContext(context)).thenReturn(phaseWithAllTasksSuccess);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);
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
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

        Pipe pipeWithAllPhasesSuccess = Pipe.createNewFromConfig(stringVersion, pipeConf,
                firstCommit);
        pipeWithAllPhasesSuccess.phases.add(firstPhase);
        pipeWithAllPhasesSuccess.phases.add(lastPhase);

        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(
                pipeWithAllPhasesSuccess);

        orchestrator.handleTaskResult(success);

        TaskStatus successTaskStatus = TaskStatus.newFinishedTaskStatus(success);
        verifyTaskStatusForDBHelperAndNotificationHandler(successTaskStatus);

        PhaseStatus successPhaseStatus = PhaseStatus.newFinishedPhaseStatus(context, true);
        verifyPhaseStatusForDBHelperAndNotificationHandler(successPhaseStatus);

        verify(dbHelper).updatePipeToFinished(version, true);
    }

    @Test
    public void testHandleTaskResultSuccessStartsFirstTaskOfNextPhaseWhenAllTasksAreSuccessful()
            throws Exception {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

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
    public void testHandleTaskresultSuccessDoesNotStartNextPhaseIfTasksAreStillRunning()
            throws Exception {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(1).state = State.RUNNING;
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

        orchestrator.handleTaskResult(success);
        verify(dbHelper).updateTaskToFinished((TaskStatus) Mockito.notNull());
        verify(notificationHandler).notifyTaskStatusListeners((TaskStatus) Mockito.notNull());
        verify(dbHelper, Mockito.atLeastOnce()).getPhaseForContext(context);
        verify(dbHelper).getPipe(version.getPipeName(), version.getVersion());
        verifyNoMoreInteractions(dbHelper);
        verifyNoMoreInteractions(notificationHandler);
        verifyNoMoreInteractions(taskExecutor);

    }

    @Test
    public void testHandleTaskResultSuccessStartsFirstTaskOfNextPhaseWhenANonBlockingTaskIsFailed()
            throws Exception {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.getPhase().getTasks().get(1).setIsBlocking(false);
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(1).state = State.FAILURE;

        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

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
    public void testHandleNonBlockingSuccessDoesNotStartFirstTaskOfNextPhase() throws Exception {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.getTask().setIsBlocking(false);
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        orchestrator.handleTaskResult(success);
        verify(dbHelper).updateTaskToFinished((TaskStatus) Mockito.notNull());
        verify(notificationHandler).notifyTaskStatusListeners((TaskStatus) Mockito.notNull());
        verifyNoMoreInteractions(dbHelper);
        verifyNoMoreInteractions(notificationHandler);
        verifyNoMoreInteractions(taskExecutor);

    }

    @Test
    public void testHandleTaskResultSuccessStartsFirstTaskOfNextPhaseWhenANonBlockingTaskIsRunning()
            throws Exception {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.getPhase().getTasks().get(1).setIsBlocking(false);
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);

        Phase firstPhase = createSuccessfullPhase(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(1).state = State.RUNNING;

        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

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
    public void testHandleTaskResultFailedDoesNotFailPhaseAndPipeIfNonBlocking() {
        TaskExecutionContext context = createContextForLastTaskInFirstPhase();
        context.finishedNow();
        context.getTask().setIsBlocking(false);
        TaskResult failure = new TaskResult(false, context);
        Phase firstPhase = createPhaseFromConf(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(0).state = State.SUCCESS;
        firstPhase.tasks.get(1).state = State.RUNNING;
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        orchestrator.handleTaskResult(failure);

        TaskStatus failedTaskStatus = TaskStatus.newFinishedTaskStatus(failure);
        verifyTaskStatusForDBHelperAndNotificationHandler(failedTaskStatus);

        verifyNoMoreInteractions(dbHelper);
        verifyNoMoreInteractions(notificationHandler);
    }

    @Test
    public void testHandleTaskResultSuccessStartsNextAutomaticTaskAndNoManual() throws Exception {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);
        Phase firstPhase = createPhaseFromConf(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(0).finishNow(true);
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(runningPipe);

        orchestrator.handleTaskResult(success);

        ArgumentCaptor<TaskExecutionContext> taskExecCapt = ArgumentCaptor
                .forClass(TaskExecutionContext.class);
        verify(taskExecutor).execute(taskExecCapt.capture(), (Orchestrator) Mockito.notNull());
        assertTrue(taskExecCapt.getValue().getTask().getTaskName().equals("SecondAutomatic"));
    }

    @Test
    public void testHandleTaskResultSuccessDoesNotStartNextTaskIfPipeIsSetToFailed()
            throws Exception {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);
        Phase firstPhase = createPhaseFromConf(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(0).finishNow(true);
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenReturn(failedPipe);

        orchestrator.handleTaskResult(success);

        verifyZeroInteractions(taskExecutor);
    }

    @Test
    public void testHandleTaskResultThrowsDataInconsistencyExceptionIfPipeNotFound()
            throws Exception {
        TaskExecutionContext context = createContextForFirstTaskInPhase(pipeConf.getPhases().get(0));
        context.finishedNow();
        TaskResult success = new TaskResult(true, context);
        Phase firstPhase = createPhaseFromConf(pipeConf.getFirstPhaseConfig());
        firstPhase.tasks.get(0).finishNow(true);
        when(dbHelper.getPhaseForContext(context)).thenReturn(firstPhase);
        when(dbHelper.getPipe(version.getPipeName(), version.getVersion())).thenThrow(
                new DataNotFoundException("Message"));
        try {
            orchestrator.handleTaskResult(success);
            assertTrue(false);
        } catch (DataInconsistencyException ex) {
            assertTrue(ex != null);
        }
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
