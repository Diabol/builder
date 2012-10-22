package notification;

import java.util.HashSet;
import java.util.Set;

import models.PipeVersion;
import models.message.PhaseStatus;
import models.message.TaskStatus;
import models.statusdata.VersionControlInfo;

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
    private final Set<PipeStatusChangedListener> pipeListeners = new HashSet<PipeStatusChangedListener>();

    public static PipeNotificationHandler getInstance() {
        return INSTANCE;
    }

    public void addPipeStatusChangedListener(PipeStatusChangedListener pipeListener) {
        synchronized (pipeListeners) {
            pipeListeners.add(pipeListener);
        }
    }

    public void removePipeStatusChangedListener(PipeStatusChangedListener pipeListener) {
        synchronized (pipeListeners) {
            pipeListeners.remove(pipeListener);
        }
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
        synchronized (taskListeners) {
            return taskListeners.size();
        }
    }

    public int getNumberOfPhaseStatusListeners() {
        synchronized (phaseListeners) {
            return phaseListeners.size();
        }
    }

    public int getNumberOfPipeListeners() {
        synchronized (pipeListeners) {
            return pipeListeners.size();
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

    public void removeAllPipeListeners() {
        synchronized (pipeListeners) {
            pipeListeners.clear();
        }
    }

    public void notifyNewVersionOfPipe(PipeVersion version, VersionControlInfo vcInfo) {
        synchronized (pipeListeners) {
            for (PipeStatusChangedListener pipeListener : pipeListeners) {
                pipeListener.receiveNewVersion(version, vcInfo);
            }
        }
    }
}
