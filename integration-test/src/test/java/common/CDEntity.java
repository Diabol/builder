package common;

public abstract class CDEntity {

    private String state;
    private String name;
    private long started;
    private String startedAsString;
    private long finished;
    private String executionTime;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public String getStartedAsString() {
        return startedAsString;
    }

    public void setStartedAsString(String startedAsString) {
        this.startedAsString = startedAsString;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

}
