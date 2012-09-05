package models.config;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-09-05
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public class PhaseConfigTest {
    private PhaseConfig phConf = new PhaseConfig();

    @Before
    public void prepare() throws Exception {
        phConf.setName("name");
    }

    @Test
    public void testGetInitialTaskReturnsFirstTaskInTaskList() throws Exception {
        TaskConfig initialTask = new TaskConfig();
        TaskConfig nextTask = new TaskConfig();
        phConf.setTasks(Arrays.asList(initialTask, nextTask));
        TaskConfig result = phConf.getInitialTask();
        assertEquals(initialTask, result);
    }

    @Test
    public void testGetInitialTaskThrowsExceptionWhenTaskListIsMissing() {
        phConf.setTasks(null);
        try {
            phConf.getInitialTask();
        } catch (PipeValidationException ex) {
             assertNotNull(ex);
        }
    }
    @Test
    public void testGetInitialTaskThrowsExceptionWhenTaskListIsEmpty() {
        phConf.setTasks(new ArrayList<TaskConfig>());
        try {
            phConf.getInitialTask();
        } catch (PipeValidationException ex) {
            assertNotNull(ex);
        }
    }

    @Test
    public void testGetTaskByNameReturnCorrectTaskWhenMatchFound() throws Exception{
        TaskConfig task1 = new TaskConfig();
        task1.setName("task1");
        TaskConfig task2 = new TaskConfig();
        task2.setName("task2");
        phConf.setTasks(Arrays.asList(task1, task2));
        TaskConfig result = phConf.getTaskByName("task1");
        assertEquals(task1, result);
    }

    @Test
    public void testGetTaskByNameThrowsExceptionWhenNotFound() throws Exception {
        TaskConfig task1 = new TaskConfig();
        task1.setName("task1");
        TaskConfig task2 = new TaskConfig();
        task2.setName("task2");
        phConf.setTasks(Arrays.asList(task1, task2));
        try {
            TaskConfig result = phConf.getTaskByName("no match");
        } catch(PipeValidationException ex ){
            assertNotNull(ex);
        }
    }
}
