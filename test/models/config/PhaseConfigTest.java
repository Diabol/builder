package models.config;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * @author danielgronberg, marcus
 */
public class PhaseConfigTest {
    
    private final PhaseConfig phaseConfEmpty = new PhaseConfig();
    private final PhaseConfig phaseConfWithName = new PhaseConfig();
    private final PhaseConfig phaseConfWithNameAndEmptyTaskList = new PhaseConfig();
    private final PhaseConfig phaseConfValidWithTwoTasksAndTwoEnvs = new PhaseConfig();
    
    private final TaskConfig task1 = new TaskConfig();


    @Before
    public void prepare() throws Exception {
        phaseConfWithName.setName("name but no tasks");

        phaseConfWithNameAndEmptyTaskList.setName("name and empty task list");
        phaseConfWithNameAndEmptyTaskList.setTasks(new ArrayList<TaskConfig>());

        phaseConfValidWithTwoTasksAndTwoEnvs.setName("Valid");
        task1.setName("task1");
        task1.setCommand("ls");
        TaskConfig task2 = new TaskConfig();
        task2.setName("task2");
        task2.setCommand("echo hej");
        phaseConfValidWithTwoTasksAndTwoEnvs.setTasks(Arrays.asList(task1, task2));
        
        phaseConfValidWithTwoTasksAndTwoEnvs.setEnvironments(Arrays.asList(new EnvironmentConfig("env1"), new EnvironmentConfig("env2")));
    }

    @Test
    public void testValidateEmptyThrowsPipeValidationException() {
        shouldThrowPipeValidationException(phaseConfEmpty);
    }

    @Test
    public void testValidateNoTasksThrowsPipeValidationException() {
        shouldThrowPipeValidationException(phaseConfWithName);
    }

    @Test
    public void testValidateEmptyTaskListThrowsPipeValidationException() {
        shouldThrowPipeValidationException(phaseConfWithNameAndEmptyTaskList);
    }

    @Test
    public void testValidateOK() throws PipeValidationException {
        phaseConfValidWithTwoTasksAndTwoEnvs.validate();
    }

    @Test
    public void testGetInitialTaskReturnsFirstTaskInTaskList() {
        TaskConfig result = phaseConfValidWithTwoTasksAndTwoEnvs.getInitialTask();
        assertEquals(task1, result);
    }

    @Test
    public void testGetInitialTaskThrowsRuntimeExceptionWhenTaskListIsMissing() {
        try {
            phaseConfWithName.getInitialTask();
            fail();
        } catch (RuntimeException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void testGetInitialTaskThrowsExceptionWhenTaskListIsEmpty() {
        try {
            phaseConfWithNameAndEmptyTaskList.getInitialTask();
            fail();
        } catch (RuntimeException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void testGetTaskByNameReturnCorrectTaskWhenMatchFound() throws Exception {
        TaskConfig result = phaseConfValidWithTwoTasksAndTwoEnvs.getTaskByName("task1");
        assertEquals(task1, result);
    }

    @Test
    public void testGetTaskByNameThrowsExceptionWhenNotFound() {
        try {
            phaseConfValidWithTwoTasksAndTwoEnvs.getTaskByName("no match");
        } catch (RuntimeException ex) {
            assertNotNull(ex);
        }
    }
    
    private static void shouldThrowPipeValidationException(PhaseConfig pc) {
        try {
            pc.validate();
            fail();
        } catch (PipeValidationException e) {
            assertThat(e.getMessage()).contains("PhaseConfig");
        }
    }
}
