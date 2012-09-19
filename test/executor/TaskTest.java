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
    private TaskExecutionContext context;
    @Mock private TaskConfig config;
    private boolean hasReceiveTaskStartedCallback = false;

    @Before
    public void createContext() {
        context = new TaskExecutionContext(config, null, null, null);
    }

    @Test
    public void testRun_ls_Successful() {
        TaskRunner target = new TaskRunner(context, this);

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
        TaskRunner target = new TaskRunner(context, this);

        when(config.getCommand()).thenReturn("cmdDoesNotExist");
        when(config.getTaskName()).thenReturn("test cmd that does not exist");

        assertThat(hasReceiveTaskStartedCallback).isFalse();

        target.run();

        assertThat(hasReceiveTaskStartedCallback).isTrue();

        assertThat(result.exitValue()).isNotEqualTo(0);
        assertThat(result.success()).isEqualTo(false);
        assertThat(result.err()).contains("Unknown error");
    }

    @Test
    public void testRunCommandWithArgument() {
        TaskRunner target = new TaskRunner(context, this);
        when(config.getCommand()).thenReturn("ls app/");
        target.run();

        assertThat(result.exitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.err()).isEmpty();
        assertThat(result.out()).contains("models");
    }

    @Test
    public void testRunShell() {
        TaskRunner target = new TaskRunner(context, this);
        // This is a bit fragile but seems to work...
        String cmd = "conf/test/" + getClass().getName() + ".sh";
        when(config.getCommand()).thenReturn(cmd);
        target.run();

        assertThat(result.exitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.err()).isEmpty();
        assertThat(result.out()).contains("1").contains("2").contains("3");
    }

    @Override
    public void handleTaskResult(TaskResult taskResult) {
        result = taskResult;
    }

    @Override
    public void handleTaskStarted(TaskExecutionContext context) {
        // we already have the context
        hasReceiveTaskStartedCallback = true;
    }

}