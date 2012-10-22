package models;

import java.util.Comparator;

import models.config.PipeConfig;
import models.statusdata.VersionControlInfo;

/**
 * Encapsulates the pipe version with the version as a string with an injectable
 * {@link Comparator} if needed. By default uses {@link String} comparison.
 * 
 * @author marcus
 */
public class PipeVersion implements Comparable<PipeVersion> {

    private final String version;
    private final PipeConfig pipe;
    private final VersionControlInfo versionControlInfo;
    private Comparator<PipeVersion> comparator;

    public static PipeVersion fromString(String pipeVersion, VersionControlInfo vcInfo,
            PipeConfig pipe) {
        return new PipeVersion(pipeVersion, vcInfo, pipe);
    }

    PipeVersion(String version, VersionControlInfo vcInfo, PipeConfig pipe)
            throws PipeVersionValidationException {
        this.version = version;
        this.pipe = pipe;
        this.versionControlInfo = vcInfo;
        validate();
    }

    PipeVersion(String version, VersionControlInfo vcInfo, PipeConfig pipe,
            Comparator<PipeVersion> comparator) throws PipeVersionValidationException {
        this(version, vcInfo, pipe);
        this.comparator = comparator;
    }

    private void validate() throws PipeVersionValidationException {
        // TODO More validation
        if (version == null || version.isEmpty() || pipe == null || pipe.getName().isEmpty()
                || versionControlInfo == null) {
            throw new PipeVersionValidationException("Could not create pipe version from: '"
                    + version + "'.");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getPipeName() {
        return pipe.getName();
    }

    public VersionControlInfo getVersionControlInfo() {
        return versionControlInfo;
    }

    @Override
    public int compareTo(PipeVersion other) {
        if (comparator != null) {
            return comparator.compare(this, other);
        }
        return this.getVersion().compareTo(other.getVersion());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PipeStringVersion [version=");
        builder.append(getVersion());
        builder.append(", versionControlInfo=" + versionControlInfo.toString());
        builder.append(", pipeName=");
        builder.append(getPipeName());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPipeName() == null) ? 0 : getPipeName().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PipeVersion)) {
            return false;
        }

        PipeVersion other = (PipeVersion) obj;
        if (!getPipeName().equals(other.getPipeName())) {
            return false;
        }
        if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
