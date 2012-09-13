package orchestration;

/**
 * A version of a given pipe.
 * @author marcus
 * @param <T> The type of the version, e.g. String.
 */
public interface PipeVersion<T> extends Comparable<PipeVersion<T>> {

    /**
     * @return A version of type T. This must have a toString representation that can be used in a REST URL.
     */
    public T getVersion();

    public String getPipeName();

    @Override
    public int compareTo(PipeVersion<T> other);

}