package view;
import engine.Engine;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

public class CommandPromptGUI extends JFrame {
    private final JTextPane outputTextPane;
    private final JTextField inputTextField;
    private String currentWorkDirectory;
    private final Engine engine;
    private final List<String> commandHistory;
    private int historyIndex;

    public CommandPromptGUI(String currentWorkDirectory) {
        super("mini-git@ProgrammerLMS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        engine = new Engine(currentWorkDirectory);
        this.currentWorkDirectory = engine.refreshBranch();

        Font font = new Font("Consolas", Font.BOLD, 20);
        outputTextPane = new JTextPane();
        outputTextPane.setEditable(false);
        outputTextPane.setFont(font);
        outputTextPane.setBackground(new Color(199, 237, 204));

        JScrollPane scrollPane = new JScrollPane(outputTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 初始化指令历史记录
        commandHistory = new ArrayList<>();
        historyIndex = 0;

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        int frameWidth = 1000;
        int frameHeight = 600;
        setSize(frameWidth, frameHeight);
        int windowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int widowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation((windowWidth - frameWidth)/2, (widowHeight - frameHeight)/2);
        setVisible(true);

        outputTextPane.setText(" _        _    _        _            _____   _    _____    _ \n" +
                "/ \\__/|  / \\  / \\  /|  / \\          /  __/  / \\  /__ __\\  / \\\n" +
                "| |\\/||  | |  | |\\ ||  | |  _____   | |  _  | |    / \\    | |\n" +
                "| |  ||  | |  | | \\||  | |  \\____\\  | |_//  | |    | |    \\_/\n" +
                "\\_/  \\|  \\_/  \\_/  \\|  \\_/          \\____\\  \\_/    \\_/    (_)\n\n");
        appendColoredText(this.currentWorkDirectory + " > ", Color.BLUE);

        inputTextField = new JTextField("Please enter command here.");
        inputTextField.setFont(new Font("Consolas", Font.BOLD, 24));
        inputTextField.setBackground(Color.LIGHT_GRAY);
        inputTextField.addActionListener(actionEvent -> {
            String command = inputTextField.getText();
            processCommand(command);
            inputTextField.setText("");

            // 将指令添加到历史记录
            addToCommandHistory(command);

            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            SwingUtilities.invokeLater(() ->
                    verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
        });

        // 注册键盘事件监听器
        inputTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    // 按下向上箭头键，回溯到上一条指令
                    showPreviousCommand();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // 按下向下箭头键，进入下一条指令
                    showNextCommand();
                }
            }
        });

        inputTextField.setFocusable(true);
        inputTextField.requestFocusInWindow();

        add(inputTextField, BorderLayout.SOUTH);
    }

    private void processCommand(String command) {
        appendText(command + "\n");
        ViewResponseEntity viewResponseEntity = engine.commandResponse(command);
        currentWorkDirectory = engine.refreshBranch();
        appendColoredText(viewResponseEntity.getText(), viewResponseEntity.getColor());
        appendColoredText("\n" + currentWorkDirectory + " > ", Color.BLUE);
    }

    private void appendText(String text) {
        try {
            outputTextPane.getDocument().insertString(
                    outputTextPane.getDocument().getLength(), text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendColoredText(String text, Color color) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        try {
            outputTextPane.getDocument().insertString(
                    outputTextPane.getDocument().getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void addToCommandHistory(String command) {
        // 将指令添加到历史记录
        commandHistory.add(command);
        // 限制历史记录的长度为10
        if (commandHistory.size() > 10) {
            commandHistory.remove(0);
        }
        historyIndex = commandHistory.size();
    }

    private void showPreviousCommand() {
        if (historyIndex >= 1) {
            historyIndex --;
            String previousCommand = commandHistory.get(historyIndex);
            inputTextField.setText(previousCommand);
        }
    }

    private void showNextCommand() {
        if (historyIndex < commandHistory.size() - 1) {
            historyIndex ++;
            String nextCommand = commandHistory.get(historyIndex);
            inputTextField.setText(nextCommand);
        } else if (historyIndex == commandHistory.size()-1){
            // 清空输入框
            historyIndex ++;
            inputTextField.setText("");
        }
    }

}
