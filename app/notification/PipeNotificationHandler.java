package notification;

import java.util.ArrayList;
import java.util.List;

import models.message.PhaseStatus;
import models.message.TaskStatus;

/**
 * Singleton that keeps track of listeners and notifies them of relevant events.
 * 
 * Listeners can register themselves and event creators/manager can notify it.
 * 
 * @author marcus
 */
public class PipeNotificationHandler {

    private static final PipeNotificationHandler INSTANCE = new PipeNotificationHandler();

    private final List<PhaseStatusChangedListener> phaseListeners = new ArrayList<PhaseStatusChangedListener>();
    private final List<TaskStatusChangedListener> taskListeners = new ArrayList<TaskStatusChangedListener>();

    public static PipeNotificationHandler getInstance() {
        return INSTANCE;
    }

    public void addPhaseStatusChangedListener(PhaseStatusChangedListener phaseListener) {
        synchronized (phaseListeners) {
            phaseListeners.add(phaseListener);
        }
    }

    public void removePhaseStatusChangedListener(PhaseStatusChangedListener phaseListener) {
        synchronized (phaseListeners) {
            phaseListeners.remove(phaseListener);
        }
    }

    public void addTaskStatusChangedListener(TaskStatusChangedListener taskListener) {
        synchronized (taskListeners) {
            taskListeners.add(taskListener);
        }
    }

    public void removeTaskStatusChangedListener(TaskStatusChangedListener taskListener) {
        synchronized (taskListeners) {
            taskListeners.remove(taskListener);
        }
    }

    public void recievePhaseStatusChanged(PhaseStatus status) {
        synchronized (phaseListeners) {
            for (PhaseStatusChangedListener listener : phaseListeners) {
                listener.recieveStatusChanged(status);
            }
        }
    }

    public void recieveTaskStatusChanged(TaskStatus status) {
        synchronized (taskListeners) {
            for (TaskStatusChangedListener listener : taskListeners) {
                listener.recieveStatusChanged(status);
            }
        }
    }
}
