package view;

import java.awt.*;

public class ViewResponseEntity {
    private String text;

    private Color color;

    public static Color WARNING_COLOR = new Color(153, 140, 0);

    public static Color INFO_COLOR = new Color(58,42,106);

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(Color color) {
        this.color = color;
    }


    public static ViewResponseEntity response(String text, Color color) {
        ViewResponseEntity viewResponseEntity = new ViewResponseEntity();
        viewResponseEntity.setText(text);
        viewResponseEntity.setColor(color);
        return viewResponseEntity;
    }

    public static ViewResponseEntity response(ViewResponseEnum viewResponseEnum) {
        ViewResponseEntity viewResponseEntity = new ViewResponseEntity();
        viewResponseEntity.setText(viewResponseEnum.getText());
        viewResponseEntity.setColor(viewResponseEnum.getColor());
        return viewResponseEntity;
    }
}
