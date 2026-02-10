package javax.microedition.lcdui;

public class Alert extends Displayable {

    private String title;
    private String text;
    private Image image;
    private AlertType type;

    public Alert(String title, String text, Image image, AlertType type) {
        this.title = title;
        this.text = text;
        this.image = image;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public AlertType getType() {
        return type;
    }
}
