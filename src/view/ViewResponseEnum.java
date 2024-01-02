package view;

import java.awt.*;

import static view.ViewResponseEntity.WARNING_COLOR;

public enum ViewResponseEnum {

    NONE_MESSAGE("", Color.BLACK),

    UNKNOWN_COMMAND("Unknown command! See readme.md for help.\n", Color.RED),

    NOT_INIT("Error! Not in an initialized mini-git directory.\n", Color.RED),

    ALREADY_INIT("A git version-control system " +
                         "already exists in the current directory.\n", WARNING_COLOR),

    NO_SUCH_FILE_EXIST("Error! No such file or directory in your working area.\n",Color.RED),

    NO_CHANGE_TO_COMMIT("No changes added to the commit.\n", WARNING_COLOR);

    private final String text;

    private final Color color;

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    ViewResponseEnum(String text, Color color) {
        this.text = text;
        this.color = color;
    }
}
