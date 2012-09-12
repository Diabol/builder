package models;

import static models.result.ResultLevel.FAILURE;
import static models.result.ResultLevel.SUCESS;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import models.config.PhaseConfig;
import models.config.TaskConfig;
import models.execution.Phase;
import models.execution.Task;
import models.result.PhaseResult;
import models.result.TaskResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import test.MockitoTestBase;

public class PhaseTest extends MockitoTestBase {

    @Mock private PhaseConfig phaseConfig;
    @Mock private TaskConfig initialTaskConfig;
    @Mock private TaskConfig taskConfig2;
    @Mock private TaskConfig taskConfig3;
    @Mock private Task task;
    @Mock private Task task2;
    @Mock private Task task3;
    @Mock private TaskResult taskResult;
    @Mock private TaskResult taskResult2;
    @Mock private TaskResult taskResult3;

    private Phase target;

    @Before
    public void prepare() throws Exception{
        when(phaseConfig.getTaskByName("task3")).thenReturn(taskConfig3);
        when(taskConfig3.createTask()).thenReturn(task3);
        when(task3.start()).thenReturn(taskResult3);

        when(phaseConfig.getTaskByName("task2")).thenReturn(taskConfig2);
        when(taskConfig2.createTask()).thenReturn(task2);
        when(task2.start()).thenReturn(taskResult2);

        when(initialTaskConfig.createTask()).thenReturn(task);
        when(task.start()).thenReturn(taskResult);
        when(phaseConfig.getInitialTask()).thenReturn(initialTaskConfig);

        target = new Phase(phaseConfig);
    }

    @Test
    public void testOneTaskThatSucceeds() throws Exception {

        when(taskResult.success()).thenReturn(true);
        when(taskResult.result()).thenReturn(SUCESS);

        PhaseResult result = target.start();
        assertThat(result.success()).isTrue();
    }

    @Test
    public void testOneTaskThatFails() throws Exception {

        when(taskResult.success()).thenReturn(false);
        when(taskResult.result()).thenReturn(FAILURE);

        PhaseResult result = target.start();
        assertThat(result.executedTasks()).isEqualTo(1);
        assertThat(result.success()).isFalse();
    }

    @Test
    public void testTwoTaskWhereSecondFails() throws Exception {


        when(taskResult.success()).thenReturn(true);
        when(taskResult.result()).thenReturn(SUCESS);

        when(task.getNextTasks()).thenReturn(Arrays.asList("task2"));

        when(taskResult2.success()).thenReturn(false);
        when(taskResult2.result()).thenReturn(FAILURE);

        PhaseResult result = target.start();
        assertThat(result.executedTasks()).isEqualTo(2);
        assertThat(result.success()).isFalse();
    }

    @Test
    public void testBothForkedTasksAreExecutedEvenIfSecondFailAndThatPhaseFail() throws Exception {

        when(taskResult.success()).thenReturn(true);
        when(taskResult.result()).thenReturn(SUCESS);

        when(task.getNextTasks()).thenReturn(Arrays.asList("task2", "task3"));

        when(taskResult3.success()).thenReturn(true);
        when(taskResult3.result()).thenReturn(SUCESS);

        PhaseResult result = target.start();
        assertThat(result.executedTasks()).isEqualTo(3);
        assertThat(result.success()).isFalse();
    }

}
