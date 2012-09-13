package orchestration;

import java.util.Comparator;

/**
 * Encapsulates the pipe version with the version as a string with an injectable
 * {@link Comparator} if needed. By default uses {@link String} comparison.
 * 
 * @author marcus
 */
public class PipeStringVersion implements PipeVersion<String> {

    private final String version;
    private final String pipeName;
    private Comparator<PipeVersion<String>> comparator;

    PipeStringVersion(String version, String pipeName) {
        this.version = version;
        this.pipeName = pipeName;
    }

    PipeStringVersion(String version, String pipeName, Comparator<PipeVersion<String>> comparator) {
        this(version, pipeName);
        this.comparator = comparator;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getPipeName() {
        return pipeName;
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

}
