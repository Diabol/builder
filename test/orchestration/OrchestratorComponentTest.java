package orchestration;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.PipeVersion;
import models.StatusInterface.State;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.Pipe;
import models.statusdata.Task;
import notification.PhaseStatusChangedListener;
import notification.PipeNotificationHandler;
import notification.PipeStatusChangedListener;
import notification.TaskStatusChangedListener;

import org.junit.AfterClass;
import org.junit.Test;

import play.Logger;
import utils.DBHelper;
import utils.DataNotFoundException;

/**
 * 'Component' test of {@link Orchestrator}.
 * 
 * This will execute the full system: read the pipe config, start a pipe and
 * check that notifications are sent.
 * 
 * TODO: When we finish the {@link Orchestrator} to start next phase this will
 * need to be updated.
 * 
 * @author marcus
 */
public class OrchestratorComponentTest implements TaskStatusChangedListener,
        PhaseStatusChangedListener, PipeStatusChangedListener {

    private final List<String> taskStartedReceived = new ArrayList<String>();
    private final List<String> taskFinishedSuccessfullyReceived = new ArrayList<String>();
    private boolean newPipeNotificationReceived = false;
    private boolean successFullPhaseStatusReceived = false;

    @AfterClass
    public static void after() {
        PipeNotificationHandler.getInstance().removeAllPhaseListeners();
        PipeNotificationHandler.getInstance().removeAllTaskListeners();
        PipeNotificationHandler.getInstance().removeAllPipeListeners();
    }

    @Test
    public void testStart() throws Exception {
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        handler.addTaskStatusChangedListener(this);
        handler.addPhaseStatusChangedListener(this);
        handler.addPipeStatusChangedListener(this);

        running(fakeApplication(), new Runnable() {
            @Override
            public void run() {
                Orchestrator target = new Orchestrator();
                PipeVersion pipeVersion = target.start("Component-A");
                assertThat(pipeVersion).isNotNull();
                Date started = new Date();
                while (!successFullPhaseStatusReceived
                        || (new Date().getTime() - started.getTime() > 60000)) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        assertThat(true).isFalse();
                    }
                }
                assertThat(taskStartedReceived.size()).isEqualTo(4);
                assertThat(taskFinishedSuccessfullyReceived.size()).isEqualTo(4);
                try {
                    Pipe persistedPipe = DBHelper.getInstance().getPipe(pipeVersion);

                    assertThat(persistedPipe.phases.get(0).tasks.size()).isGreaterThan(0);
                    assertThat(persistedPipe.phases.get(0).state).isEqualTo(State.SUCCESS);
                    for (Task task : persistedPipe.phases.get(0).tasks) {
                        assertThat(task.state).isEqualTo(State.SUCCESS);
                    }
                    assertThat(newPipeNotificationReceived).isTrue();
                    assertThat(successFullPhaseStatusReceived).isTrue();
                } catch (DataNotFoundException ex) {
                    Logger.error(ex.getMessage());
                    assertThat(true).isFalse();
                }
            }
        });
    }

    @Override
    public void recieveStatusChanged(TaskStatus status) {
        if (status.isRunning()) {
            taskStartedReceived.add(status.getTaskName());
        } else if (status.isSuccess()) {
            taskFinishedSuccessfullyReceived.add(status.getTaskName());
        }

        if (status.getTaskName().equals("Sonar") && status.isSuccess()) {
            assertThat(status.getOut()).contains("Sonar");
        }
    }

    @Override
    public void receiveNewVersion(PipeVersion version) {
        newPipeNotificationReceived = true;
    }

    @Override
    public void recieveStatusChanged(PhaseStatus status) {
        successFullPhaseStatusReceived = status.getState() == State.SUCCESS;
    }

}
