package src.authorization;

public enum LoginStatus {
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
