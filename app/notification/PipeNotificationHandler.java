package notification;

import models.message.PhaseStatus;
import models.message.TaskStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton that keeps track of listeners and notifies them of relevant events.
 * 
 * Listeners can register themselves and event creators/manager can notify it.
 * 
 * @author marcus
 */
public class PipeNotificationHandler {

    private static final PipeNotificationHandler INSTANCE = new PipeNotificationHandler();

    private final Set<PhaseStatusChangedListener> phaseListeners = new HashSet<PhaseStatusChangedListener>();
    private final Set<TaskStatusChangedListener> taskListeners = new HashSet<TaskStatusChangedListener>();

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

    public void notifyPhaseStatusListeners(PhaseStatus status) {
        synchronized (phaseListeners) {
            for (PhaseStatusChangedListener listener : phaseListeners) {
                listener.recieveStatusChanged(status);
            }
        }
    }

    public void notifyTaskStatusListeners(TaskStatus status) {
        synchronized (taskListeners) {
            for (TaskStatusChangedListener listener : taskListeners) {
                listener.recieveStatusChanged(status);
            }
        }
    }

    public int getNumberOfTaskStatusListeners() {
        synchronized(taskListeners) {
            return taskListeners.size();
        }
    }

    public int getNumberOfPhaseStatusListeners() {
        synchronized (phaseListeners) {
            return phaseListeners.size();
        }
    }

    public void removeAllTaskListeners() {
        synchronized (taskListeners) {
            taskListeners.clear();
        }
    }

    public void removeAllPhaseListeners() {
        synchronized (phaseListeners) {
            phaseListeners.clear();
        }
    }
}
