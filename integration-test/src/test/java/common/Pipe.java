package common;

import java.util.List;

/**
 * Pojo representing the json of a Pipe in PipeIt API.
 * 
 * @author danielgronberg
 * 
 */
public class Pipe extends CDEntity {
    private List<Phase> phases;
    private String version;
    private VersionControlInfo versionControlInfo;

    public List<Phase> getPhases() {
        return phases;
    }

    public void setPhases(List<Phase> phases) {
        this.phases = phases;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VersionControlInfo getVersionControlInfo() {
        return versionControlInfo;
    }

    public void setVersionControlInfo(VersionControlInfo versionControlInfo) {
        this.versionControlInfo = versionControlInfo;
    }

}