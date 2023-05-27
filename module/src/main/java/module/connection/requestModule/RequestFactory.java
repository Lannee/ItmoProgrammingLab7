package module.connection.requestModule;

public class RequestFactory {
    public static Request createRequest(String commandName, Object[] argumentsToCommand, TypeOfRequest typeOfRequest,  String userName, String userPassword) {
        return new Request(commandName, argumentsToCommand, typeOfRequest, userName, userPassword);
    }

    public static Request createRequest(TypeOfRequest typeOfRequest, String name) {
        return new Request(null, null, typeOfRequest, name, null);
    }

    public static Request createRequest(TypeOfRequest typeOfRequest) {
        return new Request(null, null, typeOfRequest, null, null);
    }

    public static Request nullRequest() {
        return new Request(null, null, null, null, null);
    }
}
