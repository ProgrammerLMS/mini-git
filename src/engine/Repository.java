package engine;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import object.Blob;
import object.Commit;
import object.Stage;
import utils.FileTreeUtils;
import utils.PersistanceUtils;

public class Repository {

    /* The current working directory. */
    public final File CWD;
    /* The mini-git directory. */
    public final File GIT_DIR;
    /* Blob and Commit and Tree data directory */
    public final File OBJECT_DIR;
    /* Stage data file */
    public final File STAGE_FILE;
    /* Branch file directory */
    public final File BRANCH_DIR;
    /* local branch directory*/
    public final File LOCAL_BRANCH_DIR;
    /* HEAD file */
    /* Note that in mini-git, there is no way to be in a detached head state
       since there is no [checkout] command that will move the HEAD pointer to a specific commit.
       The [reset] command will do that, though it also moves the branch pointer.
       Thus, in mini-git, you will never be in a detached HEAD state. */
    public final File HEAD_FILE;

    public String currentBranchName;

    private final Logger logger = Logger.getLogger(Repository.class.getName());

    public Repository(String currentWorkDirectory) {
        CWD = new File(currentWorkDirectory);
        GIT_DIR = FileTreeUtils.join(CWD, ".mini-git");
        OBJECT_DIR = FileTreeUtils.join(GIT_DIR, "objects");
        STAGE_FILE = FileTreeUtils.join(GIT_DIR, "index");
        BRANCH_DIR = FileTreeUtils.join(GIT_DIR, "refs");
        LOCAL_BRANCH_DIR = FileTreeUtils.join(BRANCH_DIR, "heads");
        HEAD_FILE = FileTreeUtils.join(GIT_DIR, "HEAD");
        currentBranchName = "";
    }

    /* check directory exsit */
    public boolean checkRepositoryExist() {
        return GIT_DIR.exists() && OBJECT_DIR.exists()
                && BRANCH_DIR.exists() && LOCAL_BRANCH_DIR.exists()
                && GIT_DIR.isDirectory() && OBJECT_DIR.isDirectory()
                && BRANCH_DIR.isDirectory() && LOCAL_BRANCH_DIR.isDirectory();
    }

    public void initBranch() {
        if (checkRepositoryExist()) {
            if (HEAD_FILE.exists()) {
                String currentLocalBranchInfo = PersistanceUtils.readContentsAsString(HEAD_FILE);
                currentBranchName = currentLocalBranchInfo.split(" ")[1].split("/")[2];
            } else {
                currentBranchName = "master";
            }
        }
    }

    /* write current commitId into refs/heads/branchName */
    public void writeCurrentCommitIdIntoCurrentLocalBranch(String commitId) {
        File file = FileTreeUtils.join(LOCAL_BRANCH_DIR, currentBranchName);
        PersistanceUtils.writeContents(file, commitId);
    }

    /* write current branchInfo into HEAD_FILE */
    private static final String REF_PREFIX = "ref: ";
    private static final String BRANCH_SEPARATOR = "/";
    public void writeCurrentLocalBranchIntoHead() {
        String content = REF_PREFIX + BRANCH_DIR.getName() + BRANCH_SEPARATOR +
                LOCAL_BRANCH_DIR.getName() + BRANCH_SEPARATOR + currentBranchName;
        PersistanceUtils.writeContents(HEAD_FILE, content);
    }

    /* id stores in refs/heads/branchName */
    public String getCurrentLocalBranchHeadId() {
        File file = FileTreeUtils.join(LOCAL_BRANCH_DIR, currentBranchName);
        if (file.exists()) {
            return PersistanceUtils.readContentsAsString(file);
        } else {
            return "";
        }
    }

    public Stage getStageFromIndexFile() {
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = PersistanceUtils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        return stage;
    }

    public Commit getCurrentLocalBranchHead() {
        String commitId = getCurrentLocalBranchHeadId();
        File commitFile = FileTreeUtils.join(OBJECT_DIR, commitId);
        if (commitFile.exists()) {
            return PersistanceUtils.readObject(commitFile, Commit.class);
        } else {
            return null;
        }
    }

    public String[] commandParseSplit(String command) {
        if (!command.contains("\"")) {
            // 字符串不包含双引号，只包含空格
            return command.split(" ");
        } else {
            // 字符串包含双引号和空格
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

    /* using filename+filecontent as key */
    public String checkBlobExist(String fileName, String content) {
        String obj = fileName + content;
        String blobId = PersistanceUtils.sha1(obj);
        File blobFile = FileTreeUtils.join(OBJECT_DIR, blobId);
        if (blobFile.exists()) {
            return blobId;
        } else {
            return "";
        }
    }

    public String writeBlobIntoObjects(String fileName, String content) {

        String obj = fileName + content;
        String blobId = PersistanceUtils.sha1(obj);
        Blob blob = new Blob(blobId, content);
        File file = FileTreeUtils.join(OBJECT_DIR, blobId);
        PersistanceUtils.writeObject(file, blob);
        return blobId;
    }

    /* write commit into objects */
    public void writeCommitIntoObjects(String commitId, Commit commit) {
        File file = FileTreeUtils.join(OBJECT_DIR, commitId);
        PersistanceUtils.writeObject(file, commit);
    }

    public Commit getCommitById(String commitId) {
        File file = FileTreeUtils.join(OBJECT_DIR, commitId);
        return PersistanceUtils.readObject(file, Commit.class);
    }

    public Map<String, String> getCurrentFilesToContentMap() {
        Map<String, String> filesMap = new HashMap<>();
        getCurrentFilesToContentMapHelper(filesMap,"", CWD);
        return filesMap;
    }

    private void getCurrentFilesToContentMapHelper(Map<String, String> filesMap, String dirName, File dir) {
        logger.info("current dir: " + dirName);
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null) {
            subDirs = new File[0];
        }
        List<File> subDirList = new ArrayList<>(subDirs.length);
        Collections.addAll(subDirList, subDirs);
        if (dirName.equals("")) {
            subDirList.remove(GIT_DIR);
        }
        for (File subDir : subDirList) {
            String newDirName = appendDirName(dirName, subDir.getName());
            getCurrentFilesToContentMapHelper(filesMap, newDirName, subDir);
        }
        List<String> allPlainFileNames = FileTreeUtils.plainFilenamesIn(dir);
        if (allPlainFileNames != null) {
            for (String fileName : allPlainFileNames) {
                File file = FileTreeUtils.join(dir, fileName);
                String content = PersistanceUtils.readContentsAsString(file);
                String fullFileName = appendDirName(dirName, fileName);
                String blobId = PersistanceUtils.sha1(fullFileName + content);
                filesMap.put(fullFileName, blobId);
            }
        }
    }
    private String appendDirName(String dirName, String name) {
        return dirName.equals("") ? name : dirName + "/" + name;
    }

}
