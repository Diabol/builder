package models.result;

public interface Result {

    /**
     * @return Result {@link ResultLevel}.
     */
    ResultLevel result();

    /**
     * @return true if suceess, false otherwise
     */
    boolean success();
}
