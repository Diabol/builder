package models;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:12
 * To change this template use File | Settings | File Templates.
 */
public class Pipe {
    private String name;
    private Phase initialPhase;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Phase getInitialPhase() {
        return initialPhase;
    }

    public void setInitialPhase(Phase initialPhase) {
        this.initialPhase = initialPhase;
    }
}
