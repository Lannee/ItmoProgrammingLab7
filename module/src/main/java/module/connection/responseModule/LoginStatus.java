package module.connection.responseModule;

public enum LoginStatus implements IStatus { 
    SUCCESSFUL("Login successful"),
    INVALID_PASSWORD("Login failed: invalid password"),
    FAILED("Login failed");

    final String description;

    LoginStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
