package notification;

import models.PipeVersion;
import models.statusdata.VersionControlInfo;

public interface PipeStatusChangedListener {
    /**
     * Called when a new PipeVersion has been created.
     * 
     * @param version
     */
    public void receiveNewVersion(PipeVersion version, VersionControlInfo vcInfo);

}
