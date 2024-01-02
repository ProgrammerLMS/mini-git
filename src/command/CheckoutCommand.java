package command;

import engine.Repository;
import view.ViewResponseEntity;

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
    public ViewResponseEntity excute() {
        return null;
    }
}
