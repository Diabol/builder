package common;

/**
 * Pojo representing the json of a VersionControlInfo in PipeIt API.
 * 
 * @author danielgronberg
 * 
 */
public class VersionControlInfo {
    private String versionControlId;
    private String versionControlText;
    private Committer committer;

    public String getVersionControlId() {
        return versionControlId;
    }

    public void setVersionControlId(String versionControlId) {
        this.versionControlId = versionControlId;
    }

    public String getVersionControlText() {
        return versionControlText;
    }

    public void setVersionControlText(String versionControlText) {
        this.versionControlText = versionControlText;
    }

    public Committer getCommitter() {
        return committer;
    }

    public void setCommitter(Committer commiter) {
        this.committer = commiter;
    }

}