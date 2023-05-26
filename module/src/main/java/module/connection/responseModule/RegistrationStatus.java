package module.connection.responseModule;

public enum RegistrationStatus implements IStatus {
    SUCCESSFUL("Registration successful"),
    SHORT_PASSWORD("Registration failed: password is too short"),
    USER_ALREADY_EXISTS("Registration failed: user is already exists"),
    FAILED("Registration failed");

    final String description;

    RegistrationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
