package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.util.Arrays;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import play.db.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import executor.TaskExecutionContext;
import executor.TaskResult;

/**
 * To be able to run these test in eclipse install the ebean enhancer plugin
 * from Avaje. Found at: http://www.avaje.org/eclipseupdate/ Then right click on
 * project and choose 'Add/remove bytecode enhancer'.
 * 
 * @author danielgronberg
 * 
 */
public class DBHelperTest {

    PipeConfig configuredPipe;
    PipeVersion version;
    VersionControlInfo vcInfo;

    @Before
    public void setup() {
        configuredPipe = PipeConfReader.getInstance().getConfiguredPipes().get(0);
        vcInfo = new VersionControlInfo("#version", "Commit msg", new Committer("john",
                "john@company.com"));
        version = PipeVersion.fromString("Version", vcInfo, configuredPipe);
    }

    @After
    public void after() {
        // Make sure the original finders are used for next test case
        DBHelper.getInstance().setPhaseFinder(Phase.find);
        DBHelper.getInstance().setPipeFinder(Pipe.find);
        DBHelper.getInstance().setTaskFinder(Task.find);
    }

    @Test
    public void testPersistNewPipePersistsTheWholePipeWithStateNotStartedAndCorrectVersion() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                try {
                    Pipe persisted = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(configuredPipe.getName(), persisted.name);
                    assertEquals(State.NOT_STARTED, persisted.state);
                    /**
                     * Assert that all phases are persisted with correct
                     */
                    for (int i = 0; i < configuredPipe.getPhases().size(); i++) {
                        PhaseConfig phaseConf = configuredPipe.getPhases().get(i);
                        Phase persistedPh = persisted.phases.get(i);
                        assertEquals(phaseConf.getName(), persistedPh.name);
                        assertEquals(State.NOT_STARTED, persistedPh.state);
                        for (int j = 0; j < phaseConf.getTasks().size(); j++) {
                            TaskConfig taskConf = phaseConf.getTasks().get(j);
                            Task persistedTa = persisted.phases.get(i).tasks.get(j);
                            assertEquals(taskConf.getTaskName(), persistedTa.name);
                            assertEquals(State.NOT_STARTED, persistedTa.state);
                        }
                    }
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testDataNotFoundExceptionThrownWhenVersionNotFoundForPipe() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().getPipe(version.getPipeName(), version.getVersion());
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }
            }
        });
    }

    @Test
    public void testThatSeveralInstancesForTheSamePipeVersionResultsInDataInconsistencyException() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    @SuppressWarnings("unchecked")
                    Finder<Long, Pipe> mockedFinder = mock(Finder.class);
                    // Mock the ExpressionList to return two pipes
                    ExpressionList<Pipe> mockedExprList = mock(ExpressionList.class);
                    when(
                            mockedExprList.eq((String) Matchers.notNull(),
                                    (String) Matchers.notNull())).thenReturn(mockedExprList);
                    List<Pipe> twoPipes = Arrays.asList(mock(Pipe.class), mock(Pipe.class));
                    when(mockedExprList.findList()).thenReturn(twoPipes);
                    when(mockedFinder.where()).thenReturn(mockedExprList);

                    DBHelper.getInstance().setPipeFinder(mockedFinder);
                    DBHelper.getInstance().getPipe(version.getPipeName(), version.getVersion());
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    // Expected exception
                    assertTrue(e != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetLatestPipereturnsTheLatestCreatedPipe() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {

                    persistPipeWithVersionControlInfo(
                            PipeVersion.fromString("Version1", vcInfo, configuredPipe),
                            configuredPipe);
                    persistPipeWithVersionControlInfo(PipeVersion.fromString(
                            "Version2",
                            new VersionControlInfo("2", "2", Committer
                                    .createCommitterNotAvailable()), configuredPipe),
                            configuredPipe);
                    persistPipeWithVersionControlInfo(PipeVersion.fromString(
                            "Version3",
                            new VersionControlInfo("3", "3", Committer
                                    .createCommitterNotAvailable()), configuredPipe),
                            configuredPipe);
                    Pipe latest = DBHelper.getInstance().getLatestPipe(configuredPipe);
                    assertEquals("Version3", latest.version);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetLatestPipeThrowsDataNotFoundExceptionWhenNoPipeIsPersisted() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().getLatestPipe(configuredPipe);
                    assertTrue(false);
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }
            }
        });
    }

    @Test
    public void testGetLatestPipeThrowsDataNotFoundExceptionWhenNoPipeIsPersistedForThatSpecificPipe() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    PipeConfig secondConfiguredPipe = PipeConfReader.getInstance()
                            .getConfiguredPipes().get(1);
                    persistPipeWithVersionControlInfo(
                            PipeVersion.fromString("Version1", vcInfo, secondConfiguredPipe),
                            secondConfiguredPipe);
                    DBHelper.getInstance().getLatestPipe(configuredPipe);
                    assertTrue(false);
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }
            }
        });
    }

    @Test
    public void testGetLatestTasksReturnsTasksOfTheLatestCreatedPipe() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    persistPipeWithVersionControlInfo(
                            PipeVersion.fromString("Version1", vcInfo, configuredPipe),
                            configuredPipe);
                    persistPipeWithVersionControlInfo(PipeVersion.fromString(
                            "Version2",
                            new VersionControlInfo("2", "2", Committer
                                    .createCommitterNotAvailable()), configuredPipe),
                            configuredPipe);
                    PhaseConfig firstPhase = configuredPipe.getPhases().get(0);
                    TaskConfig firstTask = firstPhase.getInitialTask();
                    TaskExecutionContext context = new TaskExecutionContext(firstTask,
                            configuredPipe, configuredPipe.getPhases().get(0), PipeVersion
                                    .fromString("Version2", new VersionControlInfo("2", "2",
                                            Committer.createCommitterNotAvailable()),
                                            configuredPipe));
                    TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
                    DBHelper.getInstance().updateTaskToOngoing(taskStatus);
                    List<Task> latestTasks = DBHelper.getInstance().getLatestTasks(
                            configuredPipe.getName(), firstPhase.getName());
                    assertEquals(latestTasks.get(0).state, State.RUNNING);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetLatestTasksThrowsDataNotFoundExceptionWhenNoPipeIsPersistedForThatSpecificPipe() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    PipeConfig secondConfiguredPipe = PipeConfReader.getInstance()
                            .getConfiguredPipes().get(1);
                    persistPipeWithVersionControlInfo(
                            PipeVersion.fromString("Version1", vcInfo, secondConfiguredPipe),
                            secondConfiguredPipe);
                    DBHelper.getInstance().getLatestTasks(configuredPipe.getName(),
                            configuredPipe.getFirstPhaseConfig().getName());
                    assertTrue(false);
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }
            }
        });
    }

    @Test
    public void testUpdateTaskToOngoingSetsRunningStateAndStartedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
                DBHelper.getInstance().updateTaskToOngoing(taskStatus);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.RUNNING, persistedPipe.phases.get(0).tasks.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).tasks.get(0).started != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdateTaskToOngoingThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                TaskExecutionContext context = createContextForFirstTask();
                TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
                try {
                    DBHelper.getInstance().updateTaskToOngoing(taskStatus);
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testUpdateTaskToSuccessSetsSuccessStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskResult successResult = new TaskResult(true, context);

                DBHelper.getInstance().updateTaskToFinished(
                        TaskStatus.newFinishedTaskStatus(successResult));

                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.SUCCESS, persistedPipe.phases.get(0).tasks.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).tasks.get(0).finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdateTaskToFailureSetsFailureStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskResult failedResult = new TaskResult(false, context);
                DBHelper.getInstance().updateTaskToFinished(
                        TaskStatus.newFinishedTaskStatus(failedResult));

                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.FAILURE, persistedPipe.phases.get(0).tasks.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).tasks.get(0).finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdateTaskToSuccessThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                TaskExecutionContext context = createContextForFirstTask();
                TaskResult taskResult = new TaskResult(true, context);
                try {
                    DBHelper.getInstance().updateTaskToFinished(
                            TaskStatus.newFinishedTaskStatus(taskResult));
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testUpdatePhaseToOngoingSetsRunningStateAndStartedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
                DBHelper.getInstance().updatePhaseToOngoing(phaseStatus);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.RUNNING, persistedPipe.phases.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).started != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePhaseToOngoingThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                TaskExecutionContext context = createContextForFirstTask();
                PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
                try {
                    DBHelper.getInstance().updatePhaseToOngoing(phaseStatus);
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testUpdatePhaseToSuccessSetsSuccessStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                DBHelper.getInstance().updatePhaseToFinished(
                        PhaseStatus.newFinishedPhaseStatus(context, true));
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.SUCCESS, persistedPipe.phases.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePhaseToFailureSetsFailureStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                DBHelper.getInstance().updatePhaseToFinished(
                        PhaseStatus.newFinishedPhaseStatus(context, false));
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.FAILURE, persistedPipe.phases.get(0).state);
                    assertTrue(persistedPipe.phases.get(0).finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePhaseToSuccessThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                TaskExecutionContext context = createContextForFirstTask();
                try {
                    DBHelper.getInstance().updatePhaseToFinished(
                            PhaseStatus.newFinishedPhaseStatus(context, true));
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testUpdatePipeToOngoingSetsRunningStateAndStartedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                DBHelper.getInstance().updatePipeToOnging(version);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.RUNNING, persistedPipe.state);
                    assertTrue(persistedPipe.started != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePipeToOngoingThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().updatePipeToOnging(version);
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testUpdatePipeToSuccessSetsSuccessStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                DBHelper.getInstance().updatePipeToFinished(version, true);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.SUCCESS, persistedPipe.state);
                    assertTrue(persistedPipe.finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePipeToFailureSetsFailureStateAndFinishedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                DBHelper.getInstance().updatePipeToFinished(version, false);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    assertEquals(State.FAILURE, persistedPipe.state);
                    assertTrue(persistedPipe.finished != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }

            }
        });
    }

    @Test
    public void testUpdatePipeToSuccessThrowsDataInconsistencyExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().updatePipeToFinished(version, true);
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testGetTasksForPhase() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                List<Task> persistedTasks;
                try {
                    persistedTasks = DBHelper.getInstance().getTasks(configuredPipe.getName(),
                            version.getVersion(), configuredPipe.getFirstPhaseConfig().getName());
                    List<TaskConfig> tasksConfig = configuredPipe.getFirstPhaseConfig().getTasks();
                    assertTrue(persistedTasks.size() == persistedTasks.size());
                    for (int i = 0; i < persistedTasks.size() - 1; i++) {
                        assertEquals(tasksConfig.get(i).getTaskName(), persistedTasks.get(i).name);
                    }
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetTasksForPhaseThrowsDataNotFoundExceptionWhenNotFound() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().getTasks(configuredPipe.getName(), version.getVersion(),
                            configuredPipe.getFirstPhaseConfig().getName());
                    assertTrue(false);
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }

            }
        });
    }

    @Test
    public void testThatSeveralInstancesForTheSamePhaseVersionResultsInDataInconsistencyExceptionWhenGettingTasksForPhase() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    @SuppressWarnings("unchecked")
                    Finder<Long, Phase> mockedFinder = mock(Finder.class);
                    // Mock the ExpressionList to return two pipes
                    ExpressionList<Phase> mockedExprList = mock(ExpressionList.class);
                    when(
                            mockedExprList.eq((String) Matchers.notNull(),
                                    (String) Matchers.notNull())).thenReturn(mockedExprList);
                    List<Phase> twoPhases = Arrays.asList(mock(Phase.class), mock(Phase.class));
                    when(mockedExprList.findList()).thenReturn(twoPhases);
                    when(mockedFinder.where()).thenReturn(mockedExprList);

                    DBHelper.getInstance().setPhaseFinder(mockedFinder);
                    DBHelper.getInstance().getTasks(configuredPipe.getName(), version.getVersion(),
                            configuredPipe.getFirstPhaseConfig().getName());
                    assertTrue(false);
                } catch (DataInconsistencyException e) {
                    // Expected exception
                    assertTrue(e != null);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testThatVCInfoIsPersisted() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                try {
                    Pipe persisted = DBHelper.getInstance().getPipe(version.getPipeName(),
                            version.getVersion());
                    // For some reason pipe.versionControlInfo is null when
                    // using fakeApplication() but not when running a real
                    // application.
                    // Looking up the vci based on pipe id instead.
                    VersionControlInfo persistedVC = VersionControlInfo.find.where()
                            .eq("pipe_id", persisted.pipeId).findList().get(0);
                    assertEquals(vcInfo.versionControlId, persistedVC.versionControlId);
                    assertEquals(vcInfo.versionControlText, persistedVC.versionControlText);
                    // Same comment as above
                    Committer persistedCm = Committer.find.where().eq("vc_id", persistedVC.vcId)
                            .findList().get(0);
                    assertEquals(vcInfo.committer.name, persistedCm.name);
                    assertEquals(vcInfo.committer.email, persistedCm.email);
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetAllReturnsAllRunPipes() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                persistPipeWithVersionControlInfo(version, configuredPipe);
                persistPipeWithVersionControlInfo(
                        PipeVersion.fromString("Version2",
                                VersionControlInfo.createVCInfoNotAvailable(), configuredPipe),
                        configuredPipe);
                try {
                    List<Pipe> persisted = DBHelper.getInstance().getAll(version.getPipeName());
                    assertEquals(persisted.size(), 2);
                    assertEquals(persisted.get(0).name, configuredPipe.getName());
                    assertEquals(persisted.get(1).name, configuredPipe.getName());
                    assertEquals(persisted.get(0).version, "Version");
                    assertEquals(persisted.get(1).version, "Version2");
                } catch (DataNotFoundException e) {
                    assertTrue(false);
                }
            }
        });
    }

    @Test
    public void testGetAllThrowsDataNotFoundExceptionWhenNotRun() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper.getInstance().getAll(version.getPipeName());
                    assertTrue(false);
                } catch (DataNotFoundException e) {
                    assertTrue(e != null);
                }
            }
        });
    }

    private void persistPipeWithVersionControlInfo(PipeVersion pVersion, PipeConfig pipeConfig) {
        DBHelper.getInstance().persistNewPipe(pVersion, pipeConfig);
    }

    private TaskExecutionContext createContextForFirstTask() {
        PhaseConfig firstPhase = configuredPipe.getPhases().get(0);
        TaskConfig firstTask = firstPhase.getInitialTask();
        TaskExecutionContext context = new TaskExecutionContext(firstTask, configuredPipe,
                configuredPipe.getPhases().get(0), version);
        return context;
    }
}
