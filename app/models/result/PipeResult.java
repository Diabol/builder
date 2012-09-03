package models.result;

import static models.result.Result.ResultLevel.SUCESS;
import models.Pipe;

public class PipeResult implements Result {

    private final Pipe pipe;

    public PipeResult(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public ResultLevel result() {
        // TODO Auto-generated method stub
        return SUCESS;
    }

}
