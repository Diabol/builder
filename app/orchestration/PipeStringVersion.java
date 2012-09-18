package orchestration;

import java.util.Comparator;

import models.config.PipeConfig;

/**
 * Encapsulates the pipe version with the version as a string with an injectable
 * {@link Comparator} if needed. By default uses {@link String} comparison.
 * 
 * @author marcus
 */
public class PipeStringVersion implements PipeVersion<String> {

    private final String version;
    private final PipeConfig pipe;
    private Comparator<PipeVersion<String>> comparator;

    PipeStringVersion(String version, PipeConfig pipe) throws PipeVersionValidationException {
        this.version = version;
        this.pipe = pipe;
        validate();
    }

    PipeStringVersion(String version, PipeConfig pipe, Comparator<PipeVersion<String>> comparator)
            throws PipeVersionValidationException {
        this(version, pipe);
        this.comparator = comparator;
    }

    private void validate() throws PipeVersionValidationException {
        // TODO More validation
        if (version == null || version.isEmpty() || pipe == null || pipe.getName().isEmpty()) {
            throw new PipeVersionValidationException("Could not create pipe version from: '" + version + "'.");
        }
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getPipeName() {
        return pipe.getName();
    }

    @Override
    public int compareTo(PipeVersion<String> other) {
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
        if (!(obj instanceof PipeStringVersion)) {
            return false;
        }

        PipeStringVersion other = (PipeStringVersion) obj;
        if (!getPipeName().equals(other.getPipeName())) {
            return false;
        }
        if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
