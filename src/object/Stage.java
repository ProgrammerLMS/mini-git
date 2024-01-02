package object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * staging area
 * all stage data in file "index"
 * @author: LMS
 * */
public class Stage implements Serializable {
    /* <K, V> --> <fileName, blobId> */
    private Map<String, String> addedFiles;
    /* fileNames */
    private Map<String, String> removedFiles;

    public Stage() {
        addedFiles = new HashMap<>();
        removedFiles = new HashMap<>();
    }

    public Map<String, String> getAddedFiles() {
        return addedFiles;
    }

    public Map<String, String> getRemovedFiles() {
        return removedFiles;
    }

    public void clear() {
        this.addedFiles = new HashMap<>();
        this.removedFiles = new HashMap<>();
    }

    public void addFileToStage(String fileName, String blobId) {
        addedFiles.put(fileName, blobId);
    }

    public void removeFileOutOfStage(String fileName) {
        addedFiles.remove(fileName);
    }

    public void removeFileForRemoval(String fileName, String blobId) {
        removedFiles.put(fileName, blobId);
    }

    public void removeFileOutOfRemoval(String fileName) {
        removedFiles.remove(fileName);
    }
}
