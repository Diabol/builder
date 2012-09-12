package executor;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import models.config.TaskConfig;

import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class TaskTest extends MockitoTestBase implements TaskCallback {

    TaskResult result;
    @Mock private TaskConfig config;

    @Test
    public void testRun_ls_Successful() {

        Task target = new Task(config, this);

        when(config.getCommand()).thenReturn("ls");

        target.run();

        assertThat(result.exitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.err()).isEmpty();
        assertThat(result.out()).contains("app");
    }

    @Test
    public void testRunFail() {
        Task target = new Task(config, this);

        when(config.getCommand()).thenReturn("cmdDoesNotExist");
        when(config.getTaskName()).thenReturn("test cmd that does not exist");

        target.run();

        assertThat(result.exitValue()).isNotEqualTo(0);
        assertThat(result.success()).isEqualTo(false);
        assertThat(result.err()).contains("Unknown error");
    }

    @Override
    public void receiveTaskResult(TaskResult result) {
        this.result = result;
    }
}
