package orchestration;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.util.ArrayList;
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
import notification.PhaseStatusChangedListener;
import notification.PipeNotificationHandler;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import play.Logger;
import test.MockitoTestBase;
import utils.DBHelper;
import utils.DataNotFoundException;
import utils.PipeConfReader;

/**
 * 'Component' test of {@link Orchestrator}.
 * 
 * This will execute the full system: read the pipe config, start a pipe and
 * check that notifications are sent and that the correct result is persisted.
 * 
 * @author marcus
 */
@RunWith(MockitoJUnitRunner.class)
public class OrchestratorComponentTest extends MockitoTestBase implements
        TaskStatusChangedListener, PhaseStatusChangedListener, PipeStatusChangedListener {

    private int numberOfRunningTaskStatusRecieved;
    private int numberOfSuccessfullTaskStatusRecieved;
    private int numberOfFailedTaskStatusRecieved;
    private int numberOfRunningPhaseStatusReceived;
    private int numberOfSuccessfullPhaseStatusRecieved;
    private int numberOfNewPipereceived;
    private int numberOfFailedPhaseStatusRecieved;
    private final PipeConfig mockedConf = mockConfig();
    private Orchestrator target;

    @Mock
    private PipeConfReader confReader;

    @After
    public void after() {
        target.setPipeConfigReader(PipeConfReader.getInstance());
        PipeNotificationHandler.getInstance().removeAllPhaseListeners();
        PipeNotificationHandler.getInstance().removeAllTaskListeners();
        PipeNotificationHandler.getInstance().removeAllPipeListeners();
    }

    @Before
    public void prepare() {
        numberOfRunningTaskStatusRecieved = 0;
        numberOfSuccessfullTaskStatusRecieved = 0;
        numberOfFailedTaskStatusRecieved = 0;
        numberOfRunningPhaseStatusReceived = 0;
        numberOfSuccessfullPhaseStatusRecieved = 0;
        numberOfNewPipereceived = 0;
        numberOfFailedPhaseStatusRecieved = 0;
        target = new Orchestrator();
        target.setPipeConfigReader(confReader);
    }

    @Test
    public void testRunAutomaticPipe() throws Exception {
        Mockito.when(confReader.get("ThePipe")).thenReturn(mockedConf);
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        handler.addTaskStatusChangedListener(this);
        handler.addPhaseStatusChangedListener(this);
        handler.addPipeStatusChangedListener(this);

        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                PipeVersion pipeVersion = target.start("ThePipe");
                assertThat(pipeVersion).isNotNull();
                while (numberOfSuccessfullPhaseStatusRecieved < 3) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        assertThat(true).isFalse();
                    }
                }

                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(pipeVersion);
                    assertThat(persistedPipe.state).isEqualTo(State.SUCCESS);
                    assertFirstPhase(persistedPipe);
                    assertSecondPhase(persistedPipe);
                    assertThirdPhase(persistedPipe);
                } catch (DataNotFoundException ex) {
                    Logger.error(ex.getMessage());
                    assertThat(true).isFalse();
                }

                assertThat(numberOfRunningTaskStatusRecieved).isEqualTo(8);
                assertThat(numberOfSuccessfullTaskStatusRecieved).isEqualTo(7);
                // The boguscmd will fail the task but not the phase.
                assertThat(numberOfFailedTaskStatusRecieved).isEqualTo(1);
                assertThat(numberOfRunningPhaseStatusReceived).isEqualTo(3);
                assertThat(numberOfSuccessfullPhaseStatusRecieved).isEqualTo(3);
                assertThat(numberOfFailedPhaseStatusRecieved).isEqualTo(0);
                assertThat(numberOfNewPipereceived).isEqualTo(1);
            }
        });
    }

    @Override
    public void recieveStatusChanged(TaskStatus status) {
        if (status.isRunning()) {
            numberOfRunningTaskStatusRecieved++;
        } else if (status.isSuccess()) {
            numberOfSuccessfullTaskStatusRecieved++;
        } else if (!status.isSuccess()) {
            numberOfFailedTaskStatusRecieved++;
        }
    }

    @Override
    public void receiveNewVersion(PipeVersion version) {
        numberOfNewPipereceived++;
    }

    @Override
    public void recieveStatusChanged(PhaseStatus status) {
        if (status.isSuccess()) {
            numberOfSuccessfullPhaseStatusRecieved++;
        } else if (status.getState() == State.RUNNING) {
            numberOfRunningPhaseStatusReceived++;
        } else if (!status.isSuccess()) {
            numberOfFailedPhaseStatusRecieved++;
        }
    }

    private void assertThirdPhase(Pipe persistedPipe) {
        Phase third = persistedPipe.phases.get(2);
        assertThat(third.state).isEqualTo(State.SUCCESS);
        List<Task> tasks = third.tasks;
        for (Task task : tasks) {
            assertThat(task.state).isEqualTo(State.SUCCESS);
        }
    }

    private void assertSecondPhase(Pipe persistedPipe) {
        Phase second = persistedPipe.phases.get(1);
        assertThat(second.state).isEqualTo(State.SUCCESS);
        Task firstTask = second.tasks.get(0);
        assertThat(firstTask.state).isEqualTo(State.SUCCESS);
        Task secondtask = second.tasks.get(1);
        assertThat(secondtask.state).isEqualTo(State.FAILURE);
    }

    private void assertFirstPhase(Pipe persistedPipe) {
        Phase first = persistedPipe.phases.get(0);
        assertThat(first.state).isEqualTo(State.SUCCESS);
        List<Task> tasks = first.tasks;
        for (Task task : tasks) {
            assertThat(task.state).isEqualTo(State.SUCCESS);
        }
    }

    private PipeConfig mockConfig() {
        PipeConfig config = new PipeConfig("ThePipe");
        List<PhaseConfig> phaseList = new ArrayList<PhaseConfig>();
        phaseList.add(creatFirstPhase());
        phaseList.add(createSecondPhase());
        phaseList.add(createThirdPhase());
        config.setPhases(phaseList);
        return config;
    }

    private PhaseConfig createThirdPhase() {
        PhaseConfig thirdPhase = new PhaseConfig("ThirdPhase");
        TaskConfig task1 = new TaskConfig("Task1", "sleep 1", true);
        TaskConfig task2 = new TaskConfig("Task2", "sleep 1", true);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(task2.getTaskName());
        task1.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(task1);
        taskList.add(task2);
        thirdPhase.setTasks(taskList);
        return thirdPhase;
    }

    private PhaseConfig createSecondPhase() {
        PhaseConfig secondPhase = new PhaseConfig("SecondPhase");
        TaskConfig automaticTask = new TaskConfig("AutomaticTask", "sleep 1", true);
        TaskConfig nonFailing = new TaskConfig("NonFailing", "boguscmd", true);
        nonFailing.setIsBlocking(false);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(nonFailing.getTaskName());
        automaticTask.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(automaticTask);
        taskList.add(nonFailing);
        secondPhase.setTasks(taskList);
        return secondPhase;
    }

    private PhaseConfig creatFirstPhase() {
        PhaseConfig firstPhase = new PhaseConfig("FirstPhase");
        TaskConfig firstTask = new TaskConfig("FirstTask", "sleep 1", true);
        TaskConfig parallell1 = new TaskConfig("Parallell1", "sleep 1", true);
        TaskConfig parallell2 = new TaskConfig("Parallell2", "sleep 1", true);
        TaskConfig nonFailing = new TaskConfig("NonFailing", "sleep 2", true);
        nonFailing.setIsBlocking(false);
        List<String> firstTaskTriggers = new ArrayList<String>();
        firstTaskTriggers.add(parallell1.getTaskName());
        firstTaskTriggers.add(parallell2.getTaskName());
        firstTaskTriggers.add(nonFailing.getTaskName());
        firstTask.setTriggersTasks(firstTaskTriggers);
        List<TaskConfig> taskList = new ArrayList<TaskConfig>();
        taskList.add(firstTask);
        taskList.add(parallell2);
        taskList.add(parallell1);
        taskList.add(nonFailing);
        firstPhase.setTasks(taskList);
        return firstPhase;
    }
}
