package javax.microedition.lcdui;

/**
 * Minimal stub for javax.microedition.lcdui.List.
 * The game imports this class but does not actually use it as a superclass
 * (the List superclass is commented out in MenuScreen in favor of FullScreenInfo).
 * This stub exists solely to satisfy the import statement.
 */
public class List extends Displayable {

    public static final int EXCLUSIVE = 1;
    public static final int IMPLICIT = 3;
    public static final int MULTIPLE = 2;

    public List(String title, int listType) {
    }

    public List(String title, int listType, String[] stringElements, Image[] imageElements) {
    }

    public int append(String stringPart, Image imagePart) {
        return 0;
    }

    public void delete(int elementNum) {
    }

    public int getSelectedIndex() {
        return 0;
    }

    public String getString(int elementNum) {
        return "";
    }

    public int size() {
        return 0;
    }
}
