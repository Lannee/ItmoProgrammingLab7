package src.logic.callers;

import module.commands.CommandArgument;
import module.logic.exceptions.CannotCreateObjectException;
import src.utils.ObjectUtils;
import src.utils.StringConverter;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class BaseCaller implements Callable {
    private String[] arguments;
    private String command;
    private static final Pattern ARG_PAT = Pattern.compile("\"[^\"]+\"|\\S+");


    @Override
    public CallStatus handleCommand(String line) {
        String[] words = line.split(" ", 2);

        command = words[0].toLowerCase();

        if(words.length == 1)
            arguments = new String[0];
        else
            arguments = parseArgs(words[1]);
        return CallStatus.SUCCESSFULLY;
    }

    @Override
    public Object getObjectArgument(Class<?> tClass) throws CannotCreateObjectException {
        return ObjectUtils.createObjectInteractively(tClass);
    }

    private String[] parseArgs(String line) {
        return ARG_PAT.matcher(line)
                .results()
                .map(MatchResult::group)
                .map(e -> e.replaceAll("\"", ""))
                .toArray(String[]::new);
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getCommand() {
        return command;
    }
}
