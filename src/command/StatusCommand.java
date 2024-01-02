package command;

import engine.Repository;
import object.Stage;
import utils.FileTreeUtils;
import utils.PersistenceUtils;
import view.ViewResponseEntity;

import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static view.ViewResponseEntity.INFO_COLOR;
import static view.ViewResponseEntity.WARNING_COLOR;

public class StatusCommand implements ICommand{

    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(Repository.class.getName());


    public StatusCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity excute() {
        // TODO command split
        String status = getStatusInfo();
        return ViewResponseEntity.response(status, INFO_COLOR);
    }

    private String getStatusInfo() {
        StringBuilder statusBuilder = new StringBuilder();
        /* branch */
        statusBuilder.append("=== Branches ===").append("\n");
        List<String> branchNames = FileTreeUtils.plainFilenamesIn(repository.LOCAL_BRANCH_DIR);
        if (branchNames != null) {
            for (String branchName : branchNames) {
                if (branchName.equals(repository.currentBranchName)) {
                    statusBuilder.append("*").append(repository.currentBranchName).append("\n");
                } else {
                    statusBuilder.append(branchName).append("\n");
                }
            }
        }
        statusBuilder.append("\n");
        /* stage */
        statusBuilder.append("=== Staged Files ===").append("\n");
        Stage stage = repository.getStageFromIndexFile();
        for (String addedFile : stage.getAddedFiles().keySet()) {
            statusBuilder.append(addedFile).append("\n");
        }
        statusBuilder.append("\n");
        /* remove */
        statusBuilder.append("=== Removed Files ===").append("\n");
        for (String removedFile : stage.getRemovedFiles().keySet()) {
            statusBuilder.append(removedFile).append("\n");
        }
        statusBuilder.append("\n");
        /* not stage */
        statusBuilder.append("=== Modifications Not Staged For Commit ===").append("\n");
        List<String> modifiedNotStageFiles = new ArrayList<>();
        Set<String> deletedNotStageFiles = new HashSet<>();
        Map<String, String> currentFilesMap = repository.getCurrentFilesToContentMap();
        Map<String, String> trackedFilesMap = repository.getCurrentLocalBranchHead().getCommitedFiles();

        trackedFilesMap.putAll(stage.getAddedFiles());
        for (String filename : stage.getRemovedFiles().keySet()) {
            trackedFilesMap.remove(filename);
        }

        for (Map.Entry<String, String> entry : trackedFilesMap.entrySet()) {
            String filename = entry.getKey();
            String blobId = entry.getValue();
            String currentFileBlobId = currentFilesMap.getOrDefault(filename, "");
            if (!currentFileBlobId.equals("")) {
                if (!currentFileBlobId.equals(blobId)) {
                    /* 1. Tracked in the current commit, changed
                       in the working directory, but not staged; or*/
                    /* 2. Staged for addition, but with different contents
                       than in the working directory.*/
                    modifiedNotStageFiles.add(filename);
                }
                currentFilesMap.remove(filename);
            } else {
                // 3. Staged for addition, but deleted in the working directory; or
                // 4. Not staged for removal, but tracked in the current
                // commit and deleted from the working directory.
                modifiedNotStageFiles.add(filename);
                deletedNotStageFiles.add(filename);
            }
        }

        modifiedNotStageFiles.sort(String::compareTo);

        for (String filename : modifiedNotStageFiles) {
            statusBuilder.append(filename);
            if (deletedNotStageFiles.contains(filename)) {
                statusBuilder.append(" ").append("(deleted)");
            } else {
                statusBuilder.append(" ").append("(modified)");
            }
            statusBuilder.append("\n");
        }
        statusBuilder.append("\n");

        /* untracked files */
        statusBuilder.append("=== Untracked Files ===").append("\n");
        for (String filename : currentFilesMap.keySet()) {
            statusBuilder.append(filename).append("\n");
        }

        return statusBuilder.toString();
    }

}
