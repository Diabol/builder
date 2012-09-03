package models.result;

public interface Result {

    public enum ResultLevel {
        /** Activity succeeded and all is OK */
        SUCESS,
        /** Activity finished without errors, but there are warnings */
        WARNING,
        /** Activity failed and potentially did not finish */
        FAILURE};

        /**
         * @return Result {@link ResultLevel}.
         */
        ResultLevel result();
}
