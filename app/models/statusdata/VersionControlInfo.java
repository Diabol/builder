package models.statusdata;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.codehaus.jackson.node.ObjectNode;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Json;

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

    @OneToOne(mappedBy = "vcInfo", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Committer committer;

    @Constraints.Required
    public String versionControlId;

    @Constraints.Required
    public String versionControlText;

    @OneToOne
    @JoinColumn(name = "pipe_id", nullable = false)
    public Pipe pipe;

    public static Finder<Long, VersionControlInfo> find = new Finder<Long, VersionControlInfo>(
            Long.class, VersionControlInfo.class);

    public VersionControlInfo(String versionControlId, String versionControlText,
            Committer committer) {
        super();
        this.versionControlId = versionControlId;
        this.versionControlText = versionControlText;
        this.committer = committer;
    }

    public static VersionControlInfo createVCInfoNotAvailable() {
        return new VersionControlInfo("N/A", "N/A", Committer.createCommitterNotAvailable());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VersionControlInfo [versionControlId=");
        builder.append(versionControlId);
        builder.append(", versionControlText=");
        builder.append(versionControlText);
        builder.append(", committer=");
        builder.append(committer.toString() + "]");
        return builder.toString();
    }

    public ObjectNode toObjectNode() {
        ObjectNode result = Json.newObject();
        result.put("versionControlId", versionControlId);
        result.put("versionControlText", versionControlText);
        result.put("committer", committer.toObjectNode());
        return result;
    }
}
