package command;

import engine.Repository;
import object.Commit;
import object.Stage;
import utils.FileTreeUtils;
import utils.PersistenceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/* Staging an already-staged file overwrites
 * the previous entry in the staging area with the new contents.
 * If the current working version of the file is identical to the version in the current commit,
 * do not stage it to be added, and remove it from the staging area if it is already there.
 * as can happen when a file is changed, added, and then changed back to it’s original version.
 * The file will no longer be staged for removal,
 * see git rm, if it was at the time of the command.
 * @author: LMS
 * */
public class AddCommand implements ICommand{

    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(AddCommand.class.getName());

    public AddCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity excute() {
        String[] commandSplits = repository.commandParseSplit(command);
        if (commandSplits.length < 3) {
            return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
        }
        if (commandSplits[2].equals(".")) {
            addDirectory("",repository.CWD);
            return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
        }
        for (int i = 2; i < commandSplits.length; i ++) {
            if (!addSingleFileOrDirecttory(commandSplits[i])) {
                return ViewResponseEntity.response(ViewResponseEnum.NO_SUCH_FILE_EXIST);
            }
        }
        return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
    }

    private boolean addSingleFileOrDirecttory(String fileName) {
        File file = FileTreeUtils.getFileFromTreePath(repository.CWD, fileName);
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            addDirectory(fileName, file);
        } else if (file.isFile()) {
            addSingleFile(fileName, file);
        }
        return true;
    }

    /* Staging an already-staged file overwrites
       the previous entry in the staging area with the new contents.
       If the current working version of the file is identical to the version in the current commit,
       do not stage it to be added, and remove it from the staging area if it is already there.
       as can happen when a file is changed, added, and then changed back to it’s original version.
       The file will no longer be staged for removal,
       see git rm, if it was at the time of the command. */
    private void addSingleFile(String fileName, File file) {
        logger.info("add file: " + fileName + " " + file.getAbsolutePath());
        String content, blobId;
        Stage stage = repository.getStageFromIndexFile();
        content = PersistenceUtils.readContentsAsString(file);
        blobId = repository.checkBlobExist(fileName, content);
        // check this file version in current branch
        Commit currentCommit = repository.getCurrentLocalBranchHead();
        Map<String, String> commitedFiles = currentCommit.getCommitedFiles();
        /* read blob data and check file content;
         * if the content change, add it to the stage area */
        if (blobId.equals("")) {
            blobId = repository.writeBlobIntoObjects(fileName, content);
        }
        if (stage.getRemovedFiles().containsKey(fileName)) {
            if (stage.getRemovedFiles().get(fileName).equals(blobId)) {
                stage.removeFileOutOfRemoval(fileName);
                // roll back to commit
                currentCommit.addFileToCommit(fileName, blobId);
                currentCommit.writeCommitIntoObjects(repository.COMMIT_DIR);
            } else {
                stage.removeFileOutOfRemoval(fileName);
                stage.addFileToStage(fileName, blobId);
                // do not roll back, you need a new commit
            }
        } else {
            stage.addFileToStage(fileName, blobId);
        }
        // I made a change here
        if (commitedFiles.getOrDefault(fileName, "").equals(blobId)) {
            // if this unchanged file exist in stage, remove it from stage
            // and not stage it for removal!
            stage.removeFileOutOfStage(fileName);
        }
        PersistenceUtils.writeObject(repository.STAGE_FILE, stage);
    }

    private void addDirectory(String dirName, File dir) {
        logger.info("add dir: " + dirName);
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null) {
            subDirs = new File[0];
        }
        List<File> subDirList = new ArrayList<>(subDirs.length);
        Collections.addAll(subDirList, subDirs);
        if (dirName.equals("")) {
            subDirList.remove(repository.GIT_DIR);
        }
        List<String> allPlainFileNames = FileTreeUtils.plainFilenamesIn(dir);
        String tempStoreDirName = dirName;
        for (File subDir : subDirList) {
            if (!dirName.equals("")) {
                dirName += "/" + subDir.getName();
            } else {
                dirName = subDir.getName();
            }
            addDirectory(dirName, subDir);
            // go back
            dirName = tempStoreDirName;
        }
        if (allPlainFileNames != null) {
            for (String fileName : allPlainFileNames) {
                File file = FileTreeUtils.join(dir, fileName);
                if (!dirName.equals("")) {
                    fileName = dirName + "/" + fileName;
                }
                addSingleFile(fileName, file);
            }
        }
    }
}
