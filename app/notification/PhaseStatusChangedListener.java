package notification;

import models.message.PhaseStatus;

public interface PhaseStatusChangedListener {

    void recieveStatusChanged(PhaseStatus status);

}
