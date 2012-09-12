package models;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import models.config.TaskConfig;
import models.execution.Task;
import models.result.TaskResult;

import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class TaskTest extends MockitoTestBase {

    @Mock private TaskConfig config;

    @Test
    public void testStart() {
        Task target = new Task(config);

        when(config.getCommand()).thenReturn("ls");

        TaskResult result = target.start();

        assertThat(result.getExitValue()).isEqualTo(0);
        assertThat(result.success()).isEqualTo(true);
        assertThat(result.getErr()).isEmpty();
        assertThat(result.getOut()).contains("app");
    }

}
