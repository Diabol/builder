package models;

/**
 * Created with IntelliJ IDEA.
 * User: danielgronberg
 * Date: 2012-08-27
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class Task {

    public enum Progress {NOT_STARTED, IN_PROGRESS, SUCCESS, FAILURE}


    private String name;
    private Progress progress = Progress.NOT_STARTED;
    private boolean isAutomatic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        isAutomatic = isAutomatic;
    }

}
