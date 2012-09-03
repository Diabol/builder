package models.result;

import static models.result.ResultLevel.SUCESS;

public abstract class AbstractResult implements Result {

    @Override
    public boolean success() {
        return result().equals(SUCESS);
    }

}