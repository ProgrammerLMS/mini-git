package engine;

import command.*;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

public class Engine {
    private final String currentWorkDirectory;
    private final Repository repository;

    public Engine(String currentWorkDirectory) {
        this.currentWorkDirectory = currentWorkDirectory;
        repository = new Repository(currentWorkDirectory);
        repository.initBranch();
    }

    public ViewResponseEntity commandResponse(String command) {
        if (command.length() == 0) {
            return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
        }
        String[] commandSplits = command.split(" ");
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

    private ViewResponseEntity init(String command) {
        if (repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.ALREADY_INIT);
        }
        InitCommand initCommand = new InitCommand(repository, command);
        return initCommand.excute();
    }

    private ViewResponseEntity add(String command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        AddCommand addCommand = new AddCommand(repository, command);
        return addCommand.excute();
    }

    private ViewResponseEntity commit(String command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        CommitCommand commitCommand = new CommitCommand(repository, command);
        return commitCommand.excute();
    }

    private ViewResponseEntity status(String command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        StatusCommand statusCommand = new StatusCommand(repository, command);
        return statusCommand.excute();
    }

    private ViewResponseEntity rm(String command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        RmCommand rmCommand = new RmCommand(repository, command);
        return rmCommand.excute();
    }

    public ViewResponseEntity log(String command) {
        if (!repository.checkRepositoryExist()) {
            return ViewResponseEntity.response(ViewResponseEnum.NOT_INIT);
        }
        LogCommand logCommand = new LogCommand(repository, command);
        return logCommand.excute();
    }
}
