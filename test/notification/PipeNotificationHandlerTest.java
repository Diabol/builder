package notification;

import models.message.PhaseStatus;
import models.message.TaskStatus;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import test.MockitoTestBase;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-09-14
 * Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class PipeNotificationHandlerTest extends MockitoTestBase {

    PipeNotificationHandler handler = PipeNotificationHandler.getInstance();
    @Mock
    private PhaseStatusChangedListener phaseListener1;
    @Mock
    private PhaseStatusChangedListener phaseListener2;
    @Mock
    private TaskStatusChangedListener taskListener1;
    @Mock
    private TaskStatusChangedListener taskListener2;
    @Mock
    private TaskStatus taskStatus;
    @Mock
    private PhaseStatus phaseStatus;

    @After
    public void after() {
        handler.removeAllPhaseListeners();
        handler.removeAllTaskListeners();
    }

    @Test
    public void testThatTaskListenersAreNotifiedOnReceiveTaskStatusChange() {
        handler.addTaskStatusChangedListener(taskListener1);
        handler.addTaskStatusChangedListener(taskListener2);

        handler.notifyTaskStatusListeners(taskStatus);
        Mockito.verify(taskListener1).recieveStatusChanged(taskStatus);
        Mockito.verify(taskListener2).recieveStatusChanged(taskStatus);
    }

    @Test
    public void testThatPhaseListenersAreNotifiedOnReceivePhaseStatusChange(){
        handler.addPhaseStatusChangedListener(phaseListener1);
        handler.addPhaseStatusChangedListener(phaseListener2);

        handler.notifyPhaseStatusListeners(phaseStatus);
        Mockito.verify(phaseListener1).recieveStatusChanged(phaseStatus);
        Mockito.verify(phaseListener2).recieveStatusChanged(phaseStatus);
    }

    @Test
    public void testThatTheNumberOfListenersIncreaseAfterAddingTaskListener() {
        handler.addTaskStatusChangedListener(taskListener1);
        assertTrue(handler.getNumberOfTaskStatusListeners() == 1);
    }

    @Test
    public void testThatTheNumberOfListenersIncreaseAfterAddingPhaseListener() {
        handler.addPhaseStatusChangedListener(phaseListener1);
        assertTrue(handler.getNumberOfPhaseStatusListeners() == 1);
    }


    @Test
    public void testThatTheNumberOfListenersDecreaseAfterRemovingTaskListener() {
        handler.addPhaseStatusChangedListener(phaseListener1);
        handler.removePhaseStatusChangedListener(phaseListener1);
        assertTrue(handler.getNumberOfPhaseStatusListeners() == 0);
    }

    @Test
    public void testThatTheNumberOfListenersDecreaseAfterRemovingPhaseListener() {
        handler.addTaskStatusChangedListener(taskListener1);
        handler.removeTaskStatusChangedListener(taskListener1);
        assertTrue(handler.getNumberOfTaskStatusListeners() == 0);
    }

}
