package models.config;

/**
 * The config for an Environment.
 * 
 * @author marcus
 */
public class EnvironmentConfig {
    private String name;
    
    /** No-args constructor to be able to build with tool */
    public EnvironmentConfig() {
    }

    public EnvironmentConfig(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void validate() throws PipeValidationException {
        if (getName() == null || getName().isEmpty()) {
            throw new PipeValidationException("Invalid: " + this);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EnvironmentConfig [name=");
        builder.append(name);
        builder.append("]");
        return builder.toString();
    }

}
