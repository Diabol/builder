package notification;

import models.PipeVersion;

public interface PipeStatusChangedListener {
    /**
     * Called when a new PipeVersion has been created.
     * 
     * @param version
     */
    public void receiveNewVersion(PipeVersion version);

}
