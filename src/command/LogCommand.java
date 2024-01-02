package command;

import engine.Repository;
import view.ViewResponseEntity;

public class LogCommand implements ICommand{

    private final Repository repository;
    private final String command;

    public LogCommand(Repository repository, String command) {
        this.repository = repository;
        this.command = command;
    }

    @Override
    public ViewResponseEntity excute() {
        return null;
    }
}
