package command;

import engine.Repository;
import object.Commit;
import object.Stage;
import utils.FileTreeUtils;
import utils.PersistenceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static view.ViewResponseEntity.WARNING_COLOR;

public class RmCommand implements ICommand{
    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(AddCommand.class.getName());
    private final static ViewResponseEntity common_response
            = ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);

    private boolean forced, cached;

    public RmCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity excute() {
        String[] commandSplits = repository.commandParseSplit(command);
        if (commandSplits.length < 3) {
            return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
        }
        List<String> rmFileNames;
        cached = false; forced = false;
        /* see more detail in https://blog.csdn.net/qq_30614345/article/details/130790346 */
        if (commandSplits[2].equals("--cached")) {
            cached = true;
            rmFileNames = new ArrayList<>(Arrays.asList(commandSplits).subList(3, commandSplits.length));
        } else if (commandSplits[2].equals("-f")) {
            forced = true;
            rmFileNames = new ArrayList<>(Arrays.asList(commandSplits).subList(3, commandSplits.length));
        } else {
            rmFileNames = new ArrayList<>(Arrays.asList(commandSplits).subList(2, commandSplits.length));
        }
        for (String rmFileName : rmFileNames) {
            File file = checkSingleFileOrDirecttoryExist(rmFileName);
            if (file != null) {
                if (file.isFile()) {
                    ViewResponseEntity response = removeSingleFileFromStageAndCWD(rmFileName);
                    if (response != common_response) {
                        return response;
                    }
                } else if (file.isDirectory()) {
                    ViewResponseEntity response = removeDirectory(rmFileName, file);
                    if (response != common_response) {
                        return response;
                    }
                }
            } else return ViewResponseEntity.response(ViewResponseEnum.NO_SUCH_FILE_EXIST);
        }
        return common_response;
    }

    private File checkSingleFileOrDirecttoryExist(String fileName) {
        File file = FileTreeUtils.getFileFromTreePath(repository.CWD, fileName);
        if (!file.exists()) {
            return null;
        }
        return file;
    }


    private ViewResponseEntity removeDirectory(String dirName, File dir) {
        logger.info("remove dir: " + dirName);
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null) {
            subDirs = new File[0];
        }
        List<File> subDirList = new ArrayList<>(subDirs.length);
        Collections.addAll(subDirList, subDirs);
        List<String> allPlainFileNames = FileTreeUtils.plainFilenamesIn(dir);
        String tempStoreDirName = dirName;
        for (File subDir : subDirList) {
            if (!dirName.equals("")) {
                dirName += "/" + subDir.getName();
            } else {
                dirName = subDir.getName();
            }
            ViewResponseEntity response = removeDirectory(dirName, subDir);
            if (response != common_response) {
                return response;
            }
            // go back
            dirName = tempStoreDirName;
        }
        if (allPlainFileNames != null) {
            for (String fileName : allPlainFileNames) {
                if (!dirName.equals("")) {
                    fileName = dirName + "/" + fileName;
                }
                ViewResponseEntity response = removeSingleFileFromStageAndCWD(fileName);
                if (response != common_response) {
                    return response;
                }
            }
        }
        dir.delete();
        return common_response;
    }


    /* If the file is neither staged nor tracked by the head commit. do not remove */
    /* The rm command will remove such files, as well as staging them for removal
       so that they will be untracked after a commit. */
    private ViewResponseEntity removeSingleFileFromStageAndCWD(String rmFileName) {
        logger.info("remove file: " + rmFileName);
        Stage stage = repository.getStageFromIndexFile();
        File rmFile = FileTreeUtils.join(repository.CWD, rmFileName);
        Commit commit = repository.getCurrentLocalBranchHead();
        String rmFileContent = PersistenceUtils.readContentsAsString(rmFile);
        String blobId = PersistenceUtils.sha1(rmFileName + rmFileContent);
        Map<String, String> addedFiles = stage.getAddedFiles();
        Map<String, String> commitedFiles = commit.getCommitedFiles();
        if (!addedFiles.containsKey(rmFileName) && !commitedFiles.containsKey(rmFileName)) {
            return ViewResponseEntity.response("No reason to remove the untracked file. " +
                    "Please add it or commit it first.\n", WARNING_COLOR);
        }
        if (addedFiles.containsKey(rmFileName)) {
            // if the content is unchanged or -f, rm it in stage
            if (addedFiles.get(rmFileName).equals(blobId)
                || forced) {
                stage.removeFileOutOfStage(rmFileName);
            } else {
                return ViewResponseEntity.response(
                        "Error: this file has staged content different " +
                        "from both the file and the HEAD\n" +
                        "(use -f to force removal)\n", Color.RED);
            }
        }
        /* do not remove it in CWD unless
           it is tracked in the current commit or you use -f || --cached*/
        if (commitedFiles.containsKey(rmFileName) || forced || cached) {
            if ((commitedFiles.containsKey(rmFileName)
                    && !commitedFiles.get(rmFileName).equals(blobId))
                    && !cached
                    && !forced) {
                return ViewResponseEntity.response(
                        "Error: the following file has local modifications\n"
                                + "(use --cached to keep the file, or -f to force removal)\n", Color.RED);
            } else {
                if (!commitedFiles.containsKey(rmFileName)) {
                    // A example : when you call "git status" in real git,
                    // you can see "git rm --cached <file>..." to unstage
                    stage.removeFileOutOfStage(rmFileName);
                } else stage.removeFileForRemoval(rmFileName, commitedFiles.get(rmFileName));
                commit.removeFileOutOfCommit(rmFileName);
                commit.writeCommitIntoObjects(repository.COMMIT_DIR);
                if (!cached) {
                    PersistenceUtils.restrictedDelete(rmFile);
                }
            }
        } else {
            return ViewResponseEntity.response(
                    "Error: the following file has changes staged in the index\n"
                            + "(use --cached to keep the file, or -f to force removal)\n", Color.RED);
        }

        PersistenceUtils.writeObject(repository.STAGE_FILE, stage);
        return common_response;
    }
}
