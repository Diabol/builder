package notification;

import models.message.TaskStatus;

public interface TaskStatusChangedListener {

    void recieveStatusChanged(TaskStatus status);

}
