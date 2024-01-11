package command;

import engine.Repository;
import utils.PersistanceUtils;
import view.ViewResponseEntity;
import view.ViewResponseEnum;

import java.util.logging.Logger;

public class CheckoutCommand implements ICommand{

    private final Repository repository;
    private final String command;
    private final Logger logger = Logger.getLogger(AddCommand.class.getName());

    public CheckoutCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity execute() {
        String[] commdSpilts = repository.commandParseSplit(command);
        if (commdSpilts.length == 3) {
            String branchName = commdSpilts[2];
            return checkoutBranch(branchName, false);
        }
        if (commdSpilts[2].equals("-b")) {
            String branchName = commdSpilts[3];
            return checkoutBranch(branchName, true);
        }
        if (commdSpilts[2].equals("--")){

            return null;
        }
        if (commdSpilts[2].length() == PersistanceUtils.SHORT_UID_LENGTH
                || commdSpilts[2].length() == PersistanceUtils.UID_LENGTH) {

            return null;
        }
        return null;
    }

    private ViewResponseEntity checkoutBranch(String branchName, boolean create) {
        logger.info("branch name " + branchName);
        if (create) {
            String createCommand = "git branch " + branchName;
            BranchCommand branchCommand = new BranchCommand(repository, createCommand);
            ViewResponseEntity response = branchCommand.execute();
            if (response != ViewResponseEntity.response(ViewResponseEnum.NONE_MESSAGE)) {
                return response;
            }
        }
        return null;
    }


}
