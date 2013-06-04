package orchestration;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import helpers.MockConfigHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import models.PipeVersion;
import models.StatusInterface.State;
import models.config.PipeConfig;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Committer;
import models.statusdata.Phase;
import models.statusdata.Pipe;
import models.statusdata.Task;
import models.statusdata.VersionControlInfo;
import notification.PhaseStatusChangedListener;
import notification.PipeNotificationHandler;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

//import org.apache.xpath.operations.String;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import play.Logger;
import play.api.mvc.RequestHeader;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.RequestBody;
import test.MockitoTestBase;
import utils.DBHelper;
import utils.DataNotFoundException;
import utils.LogHandler;
import utils.PipeConfReader;
import controllers.GitHub;
import controllers.Pipes;
import executor.TaskExecutor;

/**
 * 'Component' test of {@link Orchestrator}.
 * 
 * This will execute the full system except for the config which is mocked to
 * ease testing. It starts a pipe and check that notifications are sent and that
 * the correct result is persisted.
 * 
 * @author marcus
 */
@RunWith(MockitoJUnitRunner.class)
public class OrchestratorComponentTest extends MockitoTestBase implements
        TaskStatusChangedListener, PhaseStatusChangedListener, PipeStatusChangedListener {

    private int numberOfPendingTaskStatusReceived;
    private int numberOfRunningTaskStatusRecieved;
    private int numberOfSuccessfullTaskStatusRecieved;
    private int numberOfFailedTaskStatusRecieved;
    private int numberOfRunningPhaseStatusReceived;
    private int numberOfSuccessfullPhaseStatusRecieved;
    private int numberOfNewPipereceived;
    private int numberOfFailedPhaseStatusRecieved;
    private PipeConfig mockedConf;
    private Orchestrator target;
    private VersionControlInfo vcInfo;
    private PipeNotificationHandler handler;

    // Mocking config reader for better control of what to test.
    @Mock
    private PipeConfReader confReader;

    private final PipeNotificationHandler notificationHandler = PipeNotificationHandler
            .getInstance();
    private final DBHelper dbHelper = DBHelper.getInstance();
    private final TaskExecutor taskExecutor = TaskExecutor.getInstance();
    private final LogHandler logHandler = LogHandler.getInstance();

    @Before
    public void prepare() {
        numberOfPendingTaskStatusReceived = 0;
        numberOfRunningTaskStatusRecieved = 0;
        numberOfSuccessfullTaskStatusRecieved = 0;
        numberOfFailedTaskStatusRecieved = 0;
        numberOfRunningPhaseStatusReceived = 0;
        numberOfSuccessfullPhaseStatusRecieved = 0;
        numberOfNewPipereceived = 0;
        numberOfFailedPhaseStatusRecieved = 0;
        target = new Orchestrator(confReader, dbHelper, notificationHandler, taskExecutor,
                logHandler);
        mockedConf = MockConfigHelper.mockConfig();
        vcInfo = new VersionControlInfo("#1", "Commit text", new Committer("John",
                "john@company.com"));

        handler = PipeNotificationHandler.getInstance();
        handler.addTaskStatusChangedListener(this);
        handler.addPhaseStatusChangedListener(this);
        handler.addPipeStatusChangedListener(this);
    }

    @After
    public void after() {
        handler.removeAllPhaseListeners();
        handler.removeAllPipeListeners();
        handler.removeAllTaskListeners();
    }

    @Test
    public void testRunPipeWithManualTask() throws Exception {
        // Making first task of second phase manual.
        mockedConf.getPhases().get(1).getInitialTask().setIsAutomatic(false);
        Mockito.when(confReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    PipeVersion pipeVersion = target.start("ThePipe", vcInfo);

                    assertThat(pipeVersion).isNotNull();
                    // Wait for the tasks of the first phase to finish.
                    while (numberOfSuccessfullTaskStatusRecieved < 4) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            assertThat(true).isFalse();
                        }
                    }

                    Pipe persistedPipe = DBHelper.getInstance().getPipe(pipeVersion.getPipeName(),
                            pipeVersion.getVersion());
                    assertThat(persistedPipe.state).isEqualTo(State.RUNNING);
                    assertFirstPhaseSuccessFull(persistedPipe);
                    assertThat(persistedPipe.phases.get(1).tasks.get(0).state).isEqualTo(
                            State.PENDING);
                    assertThat(numberOfPendingTaskStatusReceived).isEqualTo(1);
                    assertThat(numberOfRunningTaskStatusRecieved).isEqualTo(4);
                    assertThat(numberOfSuccessfullTaskStatusRecieved).isEqualTo(4);
                    assertThat(numberOfRunningPhaseStatusReceived).isEqualTo(1);
                    assertThat(numberOfSuccessfullPhaseStatusRecieved).isEqualTo(1);
                    assertThat(numberOfNewPipereceived).isEqualTo(1);

                    // Start the manual task and verify that the whole pipe is
                    // executed successfully.
                    target.startTask(mockedConf.getPhases().get(1).getInitialTask().getTaskName(),
                            mockedConf.getPhases().get(1).getName(), mockedConf.getName(),
                            pipeVersion.getVersion());
                    waitAndAssertSuccessfullPipe(pipeVersion);
                } catch (DataNotFoundException ex) {
                    ex.printStackTrace();
                    assertThat(true).isFalse();
                }
            }
        });
    }

    @Test
    public void testRunAutomaticPipe() throws Exception {
        Mockito.when(confReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    PipeVersion pipeVersion = target.start("ThePipe", vcInfo);
                    assertThat(pipeVersion).isNotNull();
                    waitAndAssertSuccessfullPipe(pipeVersion);
                } catch (DataNotFoundException ex) {
                    ex.printStackTrace();
                    assertThat(true).isFalse();
                }
            }

        });
    }

    @Test
    public void testRunAutomaticTriggeredByGithubAndIdAndTextIsParsedAndPersisted()
            throws Exception {
        Pipes.setPipeConfigReader(confReader);
        Mockito.when(confReader.get("ThePipe")).thenReturn(mockedConf);
        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                try {
                    // Mock the request object where the json payload from
                    // Github is read.
                    String commitMsg = "Commit msg";
                    String commitId = "#asd120923rf";
                    String name = "John Doe";
                    String email = "john@company.com";
                    Map<String, String[]> encodedJson = createJsonWithCommit(commitId, commitMsg,
                            name, email);
                    Request requestMock = Mockito.mock(Request.class);
                    RequestHeader requestHeaderMock = Mockito.mock(RequestHeader.class);
                    RequestBody body = Mockito.mock(RequestBody.class);
                    Mockito.when(body.asFormUrlEncoded()).thenReturn(encodedJson);
                    Mockito.when(requestMock.body()).thenReturn(body);
                    Context.current.set(new Context(new Long(123123123), requestHeaderMock, requestMock, new HashMap<String, String>(),
                            new HashMap<String, String>(), new HashMap<String,Object>()));
                    GitHub.start("ThePipe");
                    while (numberOfSuccessfullPhaseStatusRecieved < 3) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            assertThat(true).isFalse();
                        }
                    }
                    Pipe latest = DBHelper.getInstance().getLatestPipe(mockedConf);
                    VersionControlInfo persistedVC = VersionControlInfo.find.where()
                            .eq("pipe_id", latest.pipeId).findList().get(0);
                    assertThat(persistedVC.versionControlId).isEqualTo(commitId);
                    assertThat(persistedVC.versionControlText).isEqualTo(commitMsg);
                    Committer persistedCm = Committer.find.where().eq("vc_id", persistedVC.vcId)
                            .findList().get(0);
                    assertEquals(name, persistedCm.name);
                    assertEquals(email, persistedCm.email);
                    assertLogFiles(latest);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Assert.assertTrue(false);
                }

            }

        });
    }

    private void assertLogFiles(Pipe latest) {
        for (Phase phase : latest.phases) {
            for (Task task : phase.tasks) {
                try {
                    String log = logHandler.getLog(task.name + phase.name + latest.name
                            + latest.version);
                    assertThat(log).isNotNull();
                } catch (DataNotFoundException ex) {
                    ex.printStackTrace();
                    assertThat(true).isFalse();
                }
            }
        }

    }

    private Map<String, String[]> createJsonWithCommit(String id, String message, String name,
            String email) throws Exception {
        Map<String, String[]> result = new HashMap<String, String[]>();
        StringBuilder json = new StringBuilder();
        json.append("{\"commits\":[");
        json.append("{\"message\":\"" + message + "\"");
        json.append(",\"id\":\"" + id + "\"");
        json.append(",\"author\":{");
        json.append("\"name\":\"" + name + "\"");
        json.append(",\"email\":\"" + email + "\"");
        json.append("}");
        json.append("}");
        json.append("]}");
        String[] array = { json.toString() };
        result.put("payload", array);
        return result;

    }

    private void waitAndAssertSuccessfullPipe(PipeVersion pipeVersion) {
        while (numberOfSuccessfullPhaseStatusRecieved < 3) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
                assertThat(true).isFalse();
            }
        }

        try {
            Pipe persistedPipe = DBHelper.getInstance().getPipe(pipeVersion.getPipeName(),
                    pipeVersion.getVersion());
            // For some reason pipe.versionControlInfo is null when
            // using fakeApplication() but not when running a real
            // application.
            // Looking up the vci based on pipe id instead.
            VersionControlInfo persistedVC = VersionControlInfo.find.where()
                    .eq("pipe_id", persistedPipe.pipeId).findList().get(0);
            assertEquals(vcInfo.versionControlId, persistedVC.versionControlId);
            assertEquals(vcInfo.versionControlText, persistedVC.versionControlText);
            Committer persistedCm = Committer.find.where().eq("vc_id", persistedVC.vcId).findList()
                    .get(0);
            assertEquals(vcInfo.committer.name, persistedCm.name);
            assertEquals(vcInfo.committer.email, persistedCm.email);
            assertFirstPhaseSuccessFull(persistedPipe);
            assertSecondPhaseSuccessFull(persistedPipe);
            assertThirdPhaseSuccessFull(persistedPipe);
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

    @Override
    public void recieveStatusChanged(TaskStatus status) {
        if (status.isRunning()) {
            numberOfRunningTaskStatusRecieved++;
        } else if (status.isPending()) {
            numberOfPendingTaskStatusReceived++;
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

    private void assertThirdPhaseSuccessFull(Pipe persistedPipe) {
        Phase third = persistedPipe.phases.get(2);
        assertThat(third.state).isEqualTo(State.SUCCESS);
        List<Task> tasks = third.tasks;
        for (Task task : tasks) {
            assertThat(task.state).isEqualTo(State.SUCCESS);
        }
    }

    private void assertSecondPhaseSuccessFull(Pipe persistedPipe) {
        Phase second = persistedPipe.phases.get(1);
        assertThat(second.state).isEqualTo(State.SUCCESS);
        Task firstTask = second.tasks.get(0);
        assertThat(firstTask.state).isEqualTo(State.SUCCESS);
        Task secondtask = second.tasks.get(1);
        assertThat(secondtask.state).isEqualTo(State.FAILURE);
    }

    private void assertFirstPhaseSuccessFull(Pipe persistedPipe) {
        Phase first = persistedPipe.phases.get(0);
        assertThat(first.state).isEqualTo(State.SUCCESS);
        List<Task> tasks = first.tasks;
        for (Task task : tasks) {
            assertThat(task.state).isEqualTo(State.SUCCESS);
        }
    }
}
