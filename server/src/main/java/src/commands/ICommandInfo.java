package src.commands;

@FunctionalInterface
public interface ICommandInfo {
    public StringBuilder getInfo(String key, Command value);
}
