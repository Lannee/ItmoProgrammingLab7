package src.commands;

/**
 * Command interface, sets the behavior for each team in the project
 */
public interface Command {
    void execute(String[] args);

    String getDescription();

    String[] args();

    /**
     * Checks arguments matching
     * @param args1
     * @param args2
     */
    default void checkArgsConformity(String[] args) {
        if(args.length != args().length) throw new IllegalArgumentException("Invalid number of arguments");
    }

    static void checkArgsConformity(String[] args1, String[] args2) {
        if(args1.length != args2.length) throw new IllegalArgumentException("Invalid number of arguments");
    }
}
