package executor;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import models.config.TaskConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class TaskTest extends MockitoTestBase implements TaskCallback {

    private TaskResult result;
    private ExecutionContext context;
    @Mock private TaskConfig config;
    private boolean hasReceiveTaskStarted = false;

    @Before
    public void createContext() {
        context = new ExecutionContext(config, null, null, null);
    }

    @Test
    public void testRun_ls_Successful() {
        Task target = new Task(context, this);

        when(config.getCommand()).thenReturn("ls");

        assertThat(hasReceiveTaskStarted).isFalse();

        target.run();

        assertThat(hasReceiveTaskStarted).isTrue();

        assertThat(result.exitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.err()).isEmpty();
        assertThat(result.out()).contains("app");
    }

    @Test
    public void testRunFail() {
        Task target = new Task(context, this);

        when(config.getCommand()).thenReturn("cmdDoesNotExist");
        when(config.getTaskName()).thenReturn("test cmd that does not exist");

        assertThat(hasReceiveTaskStarted).isFalse();

        target.run();

        assertThat(hasReceiveTaskStarted).isTrue();

        assertThat(result.exitValue()).isNotEqualTo(0);
        assertThat(result.success()).isEqualTo(false);
        assertThat(result.err()).contains("Unknown error");
    }

    @Override
    public void receiveTaskResult(TaskResult result) {
        this.result = result;
    }

    @Override
    public void receiveTaskStarted(ExecutionContext context) {
        // we already have the context
        hasReceiveTaskStarted = true;
    }
}
