import utils.FileTreeUtils;
import utils.PersistanceUtils;
import view.CommandPromptGUI;

import javax.swing.*;
import java.io.File;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Logger logger = Logger.getLogger(Main.class.getName());
                File cwd = new File(System.getProperty("user.dir"));
                File temp = FileTreeUtils.join(cwd, "dir.tmp");
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (temp.exists()) {
                    String content = PersistanceUtils.readContentsAsString(temp);
                    File recentPath = new File(content);
                    fc.setCurrentDirectory(recentPath);
                }
                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    PersistanceUtils.writeContents(temp, file.getAbsolutePath());
                    CommandPromptGUI commandPromptGUI =
                            new CommandPromptGUI(file.getAbsolutePath());
                    commandPromptGUI.setVisible(true);
                } else {
                    logger.info("Open command cancelled by user.");
                }
            }
        });
    }
}
