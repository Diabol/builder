package executor;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import models.config.TaskConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class TaskTest extends MockitoTestBase {

    private TaskResult result;
    private TaskExecutionContext context;
    @Mock private TaskConfig config;
    private boolean hasReceiveTaskStartedCallback = false;

    @Before
    public void createContext() {
        context = new TaskExecutionContext(config, null, null, null) {
            @Override
            public void receiveTaskResult(TaskResult taskResult) {
                result = taskResult;
            }
            @Override
            public void receiveTaskStarted(TaskExecutionContext context) {
                // we already have the context
                hasReceiveTaskStartedCallback = true;
            }
        };
    }

    @Test
    public void testRun_ls_Successful() {
        Task target = new Task(context);

        when(config.getCommand()).thenReturn("ls");

        assertThat(hasReceiveTaskStartedCallback).isFalse();

        target.run();

        assertThat(hasReceiveTaskStartedCallback).isTrue();

        assertThat(result.exitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.err()).isEmpty();
        assertThat(result.out()).contains("app");
    }

    @Test
    public void testRunFail() {
        Task target = new Task(context);

        when(config.getCommand()).thenReturn("cmdDoesNotExist");
        when(config.getTaskName()).thenReturn("test cmd that does not exist");

        assertThat(hasReceiveTaskStartedCallback).isFalse();

        target.run();

        assertThat(hasReceiveTaskStartedCallback).isTrue();

        assertThat(result.exitValue()).isNotEqualTo(0);
        assertThat(result.success()).isEqualTo(false);
        assertThat(result.err()).contains("Unknown error");
    }

}