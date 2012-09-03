package models.config;

/**
 * @author danielgronberg, marcus
 */
public class TaskConfig {

    private String name;
    private boolean isAutomatic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsAutomatic() {
        return isAutomatic;
    }

    public void setIsAutomatic(Boolean isAutomatic) {
        this.isAutomatic = isAutomatic;
    }

}
