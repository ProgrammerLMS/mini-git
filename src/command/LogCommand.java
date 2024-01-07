package command;

import engine.Repository;
import object.Commit;
import utils.PersistanceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class LogCommand implements ICommand{

    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(AddCommand.class.getName());

    public LogCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity execute() {
        String[] commandSplits = command.split(" ");
        if (commandSplits.length == 2) {
            return showLogInfo(null, null);
        }
        if (commandSplits.length == 3) {
            String subCommand = commandSplits[2];
            if (Pattern.matches("-(\\d+)$", subCommand)) {
                int number = Integer.parseInt(subCommand.substring(1));
                logger.info("log number: "+number);
                return showLogInfo(number, null);
            } else if (Pattern.matches("--grep=\"(.*)\"$", subCommand)) {
                String message = subCommand.substring(8, subCommand.length() - 1);
                logger.info("log message: "+message);
                return showLogInfo(null, message);
            }
        }
        return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
    }

    private ViewResponseEntity showLogInfo(Integer number, String message) {
        StringBuilder logBuilder = new StringBuilder();
        List<Commit> commitList = showLogInfoHelper();
        for (Commit commit : commitList) {
            if (number != null) {
                if (number == 0) {
                    break;
                }
                number -= 1;
            }
            if (message != null) {
                if (!commit.getMessage().contains(message)) {
                    continue;
                }
            }
            logBuilder.append(showSingleCommitLogInfo(commit));
        }
        return ViewResponseEntity.response(logBuilder.toString(), ViewResponseEntity.INFO_COLOR);
    }

    private List<Commit> showLogInfoHelper() {
        List<Commit> commitList = new ArrayList<>();
        Comparator<Commit> commitComparator = Comparator.comparing(Commit::getTimestamp).reversed();
        Queue<Commit> commitQueue = new PriorityQueue<>(commitComparator);
        Set<String> checkedCommitIds = new HashSet<>();
        File[] branchHeadFiles = repository.LOCAL_BRANCH_DIR.listFiles();
        if (branchHeadFiles != null) {
            Arrays.sort(branchHeadFiles, Comparator.comparing(File::getName));
            for (File branchHeadFile : branchHeadFiles) {
                String branchHeadCommitId = PersistanceUtils.readContentsAsString(branchHeadFile);
                if (checkedCommitIds.contains(branchHeadCommitId)) {
                    continue;
                }
                checkedCommitIds.add(branchHeadCommitId);
                Commit branchHeadCommit = repository.getCommitById(branchHeadCommitId);
                commitQueue.add(branchHeadCommit);
            }

            while (true) {
                Commit nextCommit = commitQueue.poll();
                if (nextCommit != null) {
                    commitList.add(nextCommit);
                    List<String> parentCommitIds = new ArrayList<>();
                    if (!nextCommit.getParentCommitId().equals("")) {
                        parentCommitIds.add(nextCommit.getParentCommitId());
                    }
                    if (!nextCommit.getSecondParentCommitId().equals("")) {
                        parentCommitIds.add(nextCommit.getSecondParentCommitId());
                    }
                    if (parentCommitIds.size() == 0) {
                        break;
                    }
                    for (String parentCommitId : parentCommitIds) {
                        if (checkedCommitIds.contains(parentCommitId)) {
                            continue;
                        }
                        checkedCommitIds.add(parentCommitId);
                        Commit parentCommit = repository.getCommitById(parentCommitId);
                        commitQueue.add(parentCommit);
                    }
                }
            }
        }
        return commitList;
    }

    private String showSingleCommitLogInfo(Commit commit) {
        StringBuilder sb = new StringBuilder();
        sb.append("===\n");
        // SHA1
        sb.append("commit ").append(commit.getCommitId()).append("\n");
        // Merge
        if (!commit.getSecondParentCommitId().equals("")) {
            sb.append("Merge: ").append(commit.getParentCommitId(), 0, 7).append(" ");
            sb.append(commit.getSecondParentCommitId(), 0, 7).append("\n");
        }
        // TimeStamp
        sb.append("Date: ");
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        sb.append(format.format(commit.getTimestamp())).append("\n");
        // Message
        sb.append(commit.getMessage()).append("\n");
        return sb.toString();
    }
}
