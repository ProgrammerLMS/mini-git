package object;

import utils.FileTreeUtils;
import utils.PersistenceUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*  Represents a mini-git commit object, stored in directory "objects"
 *  In real git, we have three objects, blob, commit and tree
 *  but here, we incorporate trees into commits and not dealing with subdirectories
 *  so there will be one flat directory of plain files for each repository
 *
 *  About Merge:
 *  A commit, therefore, will consist of a log message,
 *  timestamp, a mapping of file names to blob references,
 *  a parent reference, and (for merges) a second parent reference.
 *
 *  About hash
 *  In particular, this involves Including all metadata and references when hashing a commit.
 *  Distinguishing somehow between hashes for commits and hashes for blobs.
 *  A good way of doing this involves a well-thought
 *  out directory structure within the .mini=git directory.
 *  Another way to do so is to hash in an extra word for each object
 *  that has one value for blobs and another for commits.
 *  @author LMS
 */
public class Commit implements Serializable {
    private String commitId;
    /* The message of this Commit. */
    private String message;
    /* the timestamp of this commit */
    private Date timestamp;
    /* point to the last commit node */
    private String parentCommitId;
    /* second parent commitId, for merge */
    private String secondParentCommitId;
    /* <fileName, blobId> */
    private Map<String, String> commitedFiles;

    public Commit(String commitId, String message, Date timestamp,
                  String parentCommitId, String secondParentCommitId) {
        this.commitId = commitId;
        this.message = message;
        this.timestamp = timestamp;
        this.parentCommitId = parentCommitId;
        this.secondParentCommitId = secondParentCommitId;
        commitedFiles = new HashMap<>();
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getParentCommitId() {
        return parentCommitId;
    }

    public void setParentCommitId(String parentCommitId) {
        this.parentCommitId = parentCommitId;
    }

    public String getSecondParentCommitId() {
        return secondParentCommitId;
    }

    public void setSecondParentCommitId(String secondParentCommitId) {
        this.secondParentCommitId = secondParentCommitId;
    }

    public Map<String, String> getCommitedFiles() {
        return commitedFiles;
    }

    public void setCommitedFiles(Map<String, String> commitedFiles) {
        this.commitedFiles = commitedFiles;
    }

    public void addFileToCommit(String fileName, String blobId) {
        this.commitedFiles.put(fileName, blobId);
    }

    public void removeFileOutOfCommit(String fileName) {
        this.commitedFiles.remove(fileName);
    }

    public void writeCommitIntoObjects(File commitDir) {
        File file = FileTreeUtils.join(commitDir, commitId);
        PersistenceUtils.writeObject(file, this);
    }
}
