package models.state;

import static models.state.ResultLevel.SUCESS;

public abstract class AbstractResult implements Result {

    @Override
    public boolean success() {
        return result().equals(SUCESS);
    }

}