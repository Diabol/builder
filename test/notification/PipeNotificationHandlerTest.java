package notification;

import static org.junit.Assert.assertTrue;
import models.PipeVersion;
import models.message.PhaseStatus;
import models.message.PipeStatus;
import models.message.TaskStatus;
import models.statusdata.VersionControlInfo;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import test.MockitoTestBase;

/**
 * Created with IntelliJ IDEA. User: danielgronberg Date: 2012-09-14 Time: 14:26
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
    @Mock
    private PipeStatus pipeStatus;
    @Mock
    private PipeStatusChangedListener pipeListener1;
    @Mock
    private PipeStatusChangedListener pipeListener2;
    @Mock
    private PipeVersion pipeVersion;
    @Mock
    private VersionControlInfo vcInfo;

    @After
    public void after() {
        handler.removeAllPhaseListeners();
        handler.removeAllTaskListeners();
        handler.removeAllPipeListeners();
        assertTrue(handler.getNumberOfPhaseStatusListeners() == 0);
        assertTrue(handler.getNumberOfTaskStatusListeners() == 0);
        assertTrue(handler.getNumberOfPipeListeners() == 0);
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
    public void testThatPhaseListenersAreNotifiedOnReceivePhaseStatusChange() {
        handler.addPhaseStatusChangedListener(phaseListener1);
        handler.addPhaseStatusChangedListener(phaseListener2);

        handler.notifyPhaseStatusListeners(phaseStatus);
        Mockito.verify(phaseListener1).recieveStatusChanged(phaseStatus);
        Mockito.verify(phaseListener2).recieveStatusChanged(phaseStatus);
    }

    @Test
    public void testThatPipeListenersAreNotifiedOnReceiveNewPipeVersion() {
        handler.addPipeStatusChangedListener(pipeListener1);
        handler.addPipeStatusChangedListener(pipeListener2);

        handler.notifyNewVersionOfPipe(pipeVersion, vcInfo);
        Mockito.verify(pipeListener1).receiveNewVersion(pipeVersion, vcInfo);
        Mockito.verify(pipeListener2).receiveNewVersion(pipeVersion, vcInfo);
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
    public void testThatTheNumberOfListenersIncreaseAfterAddingPipeListener() {
        handler.addPipeStatusChangedListener(pipeListener1);
        assertTrue(handler.getNumberOfPipeListeners() == 1);
    }

    @Test
    public void testThatTheNumberOfListenersDecreaseAfterRemovingPhaseListener() {
        handler.addPhaseStatusChangedListener(phaseListener1);
        handler.removePhaseStatusChangedListener(phaseListener1);
        assertTrue(handler.getNumberOfPhaseStatusListeners() == 0);
    }

    @Test
    public void testThatTheNumberOfListenersDecreaseAfterRemovingTaskListener() {
        handler.addTaskStatusChangedListener(taskListener1);
        handler.removeTaskStatusChangedListener(taskListener1);
        assertTrue(handler.getNumberOfTaskStatusListeners() == 0);
    }

    @Test
    public void testThatTheNumberOfListenersDecreaseAfterRemovingPipeListener() {
        handler.addPipeStatusChangedListener(pipeListener1);
        handler.removePipeStatusChangedListener(pipeListener1);
        assertTrue(handler.getNumberOfTaskStatusListeners() == 0);
    }

}
