package models.statusdata;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.data.validation.Constraints;
import play.db.ebean.Model.Finder;
import play.libs.Json;

@Entity
public class Committer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long cmId;
    @Constraints.Required
    public String name;
    @Constraints.Required
    public String email;

    @OneToOne
    @JoinColumn(name = "vc_id", nullable = false)
    public VersionControlInfo vcInfo;

    public static Finder<Long, Committer> find = new Finder<Long, Committer>(Long.class,
            Committer.class);

    public Committer(String name, String email) {
        super();
        this.name = name;
        this.email = email;
    }

    public static Committer createCommitterNotAvailable() {
        return new Committer("NA", "NA");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Committer [name=");
        builder.append(name);
        builder.append(", email=");
        builder.append(email + "]");
        return builder.toString();
    }

    public JsonNode toObjectNode() {
        ObjectNode result = Json.newObject();
        result.put("name", name);
        result.put("email", email);
        return result;
    }
}
