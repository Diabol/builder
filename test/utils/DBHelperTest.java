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
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import play.db.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import executor.TaskExecutionContext;
import executor.TaskResult;

public class DBHelperTest {

    PipeConfig configuredPipe;
    PipeVersion version;

    @Before
    public void setup() {
        configuredPipe = PipeConfReader.getInstance().getConfiguredPipes().get(0);
        version = PipeVersion.fromString("Version", configuredPipe);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                try {
                    Pipe persisted = DBHelper.getInstance().getPipe(version);
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
                    DBHelper.getInstance().getPipe(version);
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
                    DBHelper.getInstance().getPipe(version);
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

                    DBHelper.getInstance().persistNewPipe(
                            PipeVersion.fromString("Version1", configuredPipe), configuredPipe);
                    DBHelper.getInstance().persistNewPipe(
                            PipeVersion.fromString("Version2", configuredPipe), configuredPipe);
                    DBHelper.getInstance().persistNewPipe(
                            PipeVersion.fromString("Version3", configuredPipe), configuredPipe);
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
    public void testUpdateTaskToOngoingSetsRunningStateAndStartedDate() {
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskStatus taskStatus = TaskStatus.newRunningTaskStatus(context);
                DBHelper.getInstance().updateTaskToOngoing(taskStatus);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskResult successResult = new TaskResult(true, context);

                DBHelper.getInstance().updateTaskToFinished(
                        TaskStatus.newFinishedTaskStatus(successResult));

                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                TaskResult failedResult = new TaskResult(false, context);
                DBHelper.getInstance().updateTaskToFinished(
                        TaskStatus.newFinishedTaskStatus(failedResult));

                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                PhaseStatus phaseStatus = PhaseStatus.newRunningPhaseStatus(context);
                DBHelper.getInstance().updatePhaseToOngoing(phaseStatus);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                DBHelper.getInstance().updatePhaseToFinished(
                        PhaseStatus.newFinishedPhaseStatus(context, true));
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                TaskExecutionContext context = createContextForFirstTask();
                DBHelper.getInstance().updatePhaseToFinished(
                        PhaseStatus.newFinishedPhaseStatus(context, false));
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                DBHelper.getInstance().updatePipeToOnging(version);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                DBHelper.getInstance().updatePipeToFinished(version, true);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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
                DBHelper.getInstance().persistNewPipe(version, configuredPipe);
                DBHelper.getInstance().updatePipeToFinished(version, false);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(version);
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

    private TaskExecutionContext createContextForFirstTask() {
        PhaseConfig firstPhase = configuredPipe.getPhases().get(0);
        TaskConfig firstTask = firstPhase.getInitialTask();
        TaskExecutionContext context = new TaskExecutionContext(firstTask, configuredPipe,
                configuredPipe.getPhases().get(0), version);
        return context;
    }
}