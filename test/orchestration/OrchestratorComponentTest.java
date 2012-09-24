package orchestration;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import models.PipeVersion;
import models.message.TaskStatus;
import notification.PipeNotificationHandler;
import notification.TaskStatusChangedListener;

import org.junit.Test;

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
public class OrchestratorComponentTest implements TaskStatusChangedListener {

    private final List<String> taskStartedReceived = new ArrayList<String>();
    private final List<String> taskFinishedSuccessfullyReceived = new ArrayList<String>();

    @Test
    public void testStart() throws Exception {
        PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
        handler.addTaskStatusChangedListener(this);

        Orchestrator target = new Orchestrator();
        PipeVersion pipeVersion = target.start("Component-A");
        assertThat(pipeVersion).isNotNull();

        while (taskFinishedSuccessfullyReceived.size() < 4) {
            Thread.sleep(200);
        }
        assertThat(taskStartedReceived.size()).isEqualTo(4);
        assertThat(taskFinishedSuccessfullyReceived.size()).isEqualTo(4);
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

}
