package models.result;

public enum ResultLevel {
    /** Activity succeeded and all is OK */
    SUCESS,
    // Maybe we add this level later
//    /** Activity finished without errors, but there are warnings */
//    WARNING,
    /** Activity failed and potentially did not finish */
    FAILURE
}