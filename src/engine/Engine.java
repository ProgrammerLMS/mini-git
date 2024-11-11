package engine;

import command.*;
import view.ViewResponseEntity;
import view.ViewResponseEnum;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    private final String currentWorkDirectory;
    private final Repository repository;

    public Engine(String currentWorkDirectory) {
        this.currentWorkDirectory = currentWorkDirectory;
        repository = new Repository(currentWorkDirectory);
        repository.initBranch();
    }

    private String[] parseCommand(String command) {
        if (!command.contains("\"")) {
            return command.split(" ");
        } else {
            boolean insideQuotes = false;
            StringBuilder sb = new StringBuilder();
            List<String> result = new ArrayList<>();
            for (char c : command.toCharArray()) {
                if (c == '\"') {
                    insideQuotes = !insideQuotes;
                } else if (c == ' ' && !insideQuotes) {
                    result.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            result.add(sb.toString());
            return result.toArray(new String[0]);
        }
    }
    public ViewResponseEntity commandResponse(String command) {
        if (command.length() == 0) {
            return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
        }
        String[] commandSplits = parseCommand(command);
        if (commandSplits.length == 1 || !commandSplits[0].equals("git")) {
            return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
        }
        String commandHead = commandSplits[1];
        switch (commandHead) {
            case "init":
                return init(command);
            case "add":
                return add(command);
            case "commit":
                return commit(command);
            case "status":
                return status(command);
            case "rm":
                return rm(command);
            case "log":
                return log(command);
            case "branch":
                return branch(command);
            case "checkout":

            default:
                return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
        }
    }

    public String refreshBranch() {
        if (repository.currentBranchName.equals("")) {
            return currentWorkDirectory;
        } else {
            return currentWorkDirectory + "("
                    + repository.currentBranchName + ")";
        }
    }

    private ViewResponseEntity executeCommand(ICommand command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        return command.execute();
    }

    private ViewResponseEntity init(String command) {
        if (repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.ALREADY_INIT);
        }
        return executeCommand(new InitCommand(repository, command));
    }
    private ViewResponseEntity add(String command) {
        return executeCommand(new AddCommand(repository, command));
    }
    private ViewResponseEntity commit(String command) {
        return executeCommand(new CommitCommand(repository, command));
    }
    private ViewResponseEntity status(String command) {
        return executeCommand(new StatusCommand(repository, command));
    }
    private ViewResponseEntity rm(String command) {
        return executeCommand(new RmCommand(repository, command));
    }
    public ViewResponseEntity log(String command) {
        return executeCommand(new LogCommand(repository, command));
    }
    public ViewResponseEntity branch(String command) {
        return executeCommand(new BranchCommand(repository, command));
    }
    public ViewResponseEntity checkout(String command) {
        return executeCommand(new CheckoutCommand(repository, command));
    }
}
