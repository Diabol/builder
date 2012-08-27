package models;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class PipeConfDocument{
    private List<Pipe> pipes;

    public List<Pipe> getPipes() {
        return pipes;
    }

    public void setPipes(List<Pipe> pipes) {
        this.pipes = pipes;
    }

}
