package module.connection.requestModule;

public class RequestFactory {
    public static Request createRequest(String commandName, Object[] argumentsToCommand, TypeOfRequest typeOfRequest) {
        return new Request(commandName, argumentsToCommand, typeOfRequest);
    }

    public static Request createRequest(TypeOfRequest typeOfRequest) {
        return new Request(null, null, typeOfRequest);
    }

    public static Request nullRequest() {
        return new Request(null, null, null);
    }
}
