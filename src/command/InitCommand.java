package command;

import engine.Repository;
import object.Commit;
import utils.PersistanceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.util.Date;

public class InitCommand implements ICommand {

    private final Repository repository;
    private final String command;

    public InitCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity execute() {
        String[] commandSplits = command.split(" ");
        if (commandSplits.length > 2) {
            return ViewResponseEntity.response(ViewResponseEnum.UNKNOWN_COMMAND);
        }
        if (!repository.GIT_DIR.exists()) {
            repository.GIT_DIR.mkdir();
        }
        if (!repository.OBJECT_DIR.exists()) {
            repository.OBJECT_DIR.mkdir();
        }
        if (!repository.BRANCH_DIR.exists()) {
            repository.BRANCH_DIR.mkdir();
        }
        if (!repository.LOCAL_BRANCH_DIR.exists()) {
            repository.LOCAL_BRANCH_DIR.mkdir();
        }
        repository.initBranch();
        /* do not forget every time you init, there will be a new Commit which point nothing;*/
        Date initDate = new Date(0);
        String initMessage = "initial commit";
        /* Since the initial commit in all repositories
           created by Gitlet will have exactly the same content,
           it follows that all repositories will automatically share this commit
           they will all have the same UID and
           all commits in all repositories will trace back to it. */
        String obj = initMessage + initDate;
        String commitId = PersistanceUtils.sha1(obj);
        // init commit
        Commit commit = new Commit(commitId, initMessage, initDate, "", "");
        commit.writeCommitIntoObjects(repository.OBJECT_DIR);
        // local"master" branch head and HEAD file both point at the init commit
        repository.writeCurrentCommitIdIntoCurrentLocalBranch(commitId);
        // write branchInfo into HEAD
        repository.writeCurrentLocalBranchIntoHead();
        return ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE);
    }
}
