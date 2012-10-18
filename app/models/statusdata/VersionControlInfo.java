package models.statusdata;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import play.data.validation.Constraints;
import play.db.ebean.Model;

/**
 * 
 * @author danielgronberg
 * 
 */
@Entity
public class VersionControlInfo extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long vcId;

    @Constraints.Required
    public String versionControlId;

    @Constraints.Required
    public String versionControlText;

    @OneToOne
    @JoinColumn(name = "pipe_id", nullable = false)
    public Pipe pipe;

    public static Finder<Long, VersionControlInfo> find = new Finder<Long, VersionControlInfo>(
            Long.class, VersionControlInfo.class);

    public VersionControlInfo(String versionControlId, String versionControlText) {
        super();
        this.versionControlId = versionControlId;
        this.versionControlText = versionControlText;
    }

    public static VersionControlInfo createVCInfoNotAvailable() {
        return new VersionControlInfo("NA", "NA");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionControlInfo [versionControlId=");
        builder.append(versionControlId);
        builder.append(", versionControlText=");
        builder.append(versionControlText + "]");
        return builder.toString();
    }
}
