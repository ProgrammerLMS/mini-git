package command;

import engine.Repository;
import utils.FileTreeUtils;
import utils.PersistanceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

public class BranchCommand implements ICommand{

    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(AddCommand.class.getName());

    public BranchCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity execute() {
        String[] commandSplits = repository.commandParseSplit(command);
        if (commandSplits.length == 2) {
            return showAllLocalBranchInfo();
        }
        if (commandSplits.length == 3) {
            String branchName = commandSplits[2];
            if (FileTreeUtils.isValidFileName(branchName)) {
                return createNewBranch(branchName);
            }
        }
        if (commandSplits.length == 4) {
            if (commandSplits[2].equals("-d")) {
                String branchName = commandSplits[3];
                return deleteGivenBranch(branchName);
            }
        }
        return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
    }

    private ViewResponseEntity showAllLocalBranchInfo() {
        StringBuilder branchBuilder = new StringBuilder();
        File[] branchFiles = repository.LOCAL_BRANCH_DIR.listFiles();
        if (branchFiles != null) {
            Arrays.sort(branchFiles, Comparator.comparing(File::getName));
            for (File file : branchFiles) {
                if (file.getName().equals(repository.currentBranchName)) {
                    branchBuilder.append("*").append(file.getName()).append("\n");
                } else {
                    branchBuilder.append(file.getName()).append("\n");
                }
            }
        }
        return ViewResponseEntity.response(branchBuilder.toString(), ViewResponseEntity.INFO_COLOR);
    }

    /* Creates a new branch with the given name,
       and points it at the current head commit.
       A branch is nothing more than a name for a reference to a commit node.
       This command does NOT immediately switch to the newly created branch
       Before you ever call branch,
       your code should be running with a default branch called master*/
    private ViewResponseEntity createNewBranch(String newBranchName) {
        logger.info("new branch name: " + newBranchName);
        File file = FileTreeUtils.join(repository.LOCAL_BRANCH_DIR, newBranchName);
        if (file.exists()) {
            return ViewResponseEntity.response("A branch with that name already exists.\n",
                    ViewResponseEntity.WARNING_COLOR);
        }
        String commitId = repository.getCurrentLocalBranchHeadId();
        PersistanceUtils.writeContents(file, commitId);
        return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
    }

    private ViewResponseEntity deleteGivenBranch(String branchName) {
        return null;
    }
}
