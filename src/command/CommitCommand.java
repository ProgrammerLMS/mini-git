package command;

import engine.Repository;
import object.Commit;
import object.Stage;
import utils.PersistanceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CommitCommand implements ICommand{
    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(CommitCommand.class.getName());


    public CommitCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    private static final String COMMIT_MESSAGE_FLAG = "-m";
    @Override
    public ViewResponseEntity execute() {
        String[] commandSplits = repository.commandParseSplit(command);
        if (commandSplits.length == 4) {
            if (commandSplits[2].equals(COMMIT_MESSAGE_FLAG)) {
                logger.info("commit message: " + commandSplits[3]);
                return clearStageAndCommit(commandSplits[3], "");
            }
        }
        return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
    }

    /* By default a commit has the same file contents as its parent.
       Files staged for addition and removal are the updates to the commit.
       remember that the staging area is cleared after a commit. */
    /* Any changes made to files after staging
       for addition or removal are ignored by the commit command */
    /* ? Each commit is identified by its SHA-1 id,
       which must include the blob references of its files,
       parent reference, log message, and commit time. ? */
    private ViewResponseEntity clearStageAndCommit(String message, String secondParentId) {
        Date date = new Date();
        String obj = message + date;
        String newCommitId = PersistanceUtils.sha1(obj);
        logger.info("Entering clearStageAndCommit with message: " + message + " and secondParentId: " + secondParentId);
        // how we get the lastest commitId? -> current branch head point at it
        String currentCommitId = repository.getCurrentLocalBranchHeadId();
        Commit currentCommit = repository.getCurrentLocalBranchHead();
        Commit newCommit = new Commit(newCommitId, message, date, currentCommitId, secondParentId);
        /* default commit is same as it parent commit */
        if (currentCommit != null) {
            newCommit.setCommitedFiles(currentCommit.getCommitedFiles());
        }
        Stage stage = repository.getStageFromIndexFile();;
        Map<String, String> addedFiles = stage.getAddedFiles();
        Map<String, String> removedFiles = stage.getRemovedFiles();
        if (addedFiles.size() == 0 && removedFiles.size() == 0) {
            return ViewResponseEntity.response(ViewResponseEnum.NO_CHANGE_TO_COMMIT);
        }
        for (String fileName : addedFiles.keySet()) {
            newCommit.addFileToCommit(fileName, addedFiles.get(fileName));
        }
        /* files tracked in the current commit may be untracked in the new commit
           as a result being staged for removal */
        for (String removeFileName : removedFiles.keySet()) {
            newCommit.removeFileOutOfCommit(removeFileName);
        }
        // 1.update index
        stage.clear();
        PersistanceUtils.writeObject(repository.STAGE_FILE, stage);
        // 2.update refs/heads
        repository.writeCurrentCommitIdIntoCurrentLocalBranch(newCommitId);
        // 3.write new commit into object
        repository.writeCommitIntoObjects(newCommitId, newCommit);
        return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
    }
}
