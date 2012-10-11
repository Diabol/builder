package models.config;

import java.util.Collections;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * @author danielgronberg, marcus
 */
public class TaskConfigTest {
    private final TaskConfig taskConfNoName = new TaskConfig();
    private final TaskConfig taskConfNoCommand = new TaskConfig();
    private final TaskConfig taskConfValid = new TaskConfig();
    private final TaskConfig taskConfNonBlockingWithTriggersTasksSet = new TaskConfig();

    @Before
    public void prepare() throws Exception {

        taskConfNoName.setCommand("ls");

        taskConfNoCommand.setName("name and no command");

        taskConfValid.setName("Valid");
        taskConfValid.setName("task1");
        taskConfValid.setCommand("ls");
        taskConfValid.setTriggersTasks(Collections.singletonList("next"));

        taskConfNonBlockingWithTriggersTasksSet.setName("Nonblocking with triggerstasks");
        taskConfNonBlockingWithTriggersTasksSet.setIsBlocking(false);
        taskConfNonBlockingWithTriggersTasksSet.setTriggersTasks(Collections.singletonList("task"));

    }

    @Test
    public void testValidateValidTaskConfig() {
        try {
            taskConfValid.validate();
        } catch (PipeValidationException ex) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testDefaultValues() {
        Assert.assertTrue(taskConfNoName.isAutomatic());
        Assert.assertTrue(taskConfNoName.isBlocking());
    }

    @Test
    public void testValidateTaskConfigWithoutNameThrowsException() {
        try {
            taskConfNoName.validate();
            Assert.assertTrue(false);
        } catch (PipeValidationException ex) {
            Assert.assertNotNull(ex);
        }
    }

    @Test
    public void testValidateTaskConfigWithoutCommandThrowsException() {
        try {
            taskConfNoCommand.validate();
            Assert.assertTrue(false);
        } catch (PipeValidationException ex) {
            Assert.assertNotNull(ex);
        }
    }

    @Test
    public void testValidateNonBlockingTaskConfigWithTriggersTasksThrowsException() {
        try {
            taskConfNonBlockingWithTriggersTasksSet.validate();
            Assert.assertTrue(false);
        } catch (PipeValidationException ex) {
            Assert.assertNotNull(ex);
        }
    }
}
