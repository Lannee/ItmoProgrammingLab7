package src.logic.callers;

import module.logic.exceptions.CannotCreateObjectException;

public interface Callable {
    public CallStatus handleCommand(String line);
    public Object getObjectArgument(Class<?> tClass) throws CannotCreateObjectException;
    public String getCommand();
    public String[] getArguments();
}
