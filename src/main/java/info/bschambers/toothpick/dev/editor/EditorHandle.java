package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPGeometry;
import java.awt.Color;
import java.awt.Rectangle;

/**
 * <p>A click-and-drag handle for interactive editing.</p>
 */
public class EditorHandle {

    protected int x = 0;
    protected int y = 0;
    private int size = 10;
    private boolean selected = false;
    private Color color = Color.YELLOW;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean val) {
        selected = val;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getHandle() {
        return makeCenteredSquare(x, y, size);
    }

    public Color getColor() {
        return color;
    }

    private Rectangle makeCenteredSquare(int xCenter, int yCenter, int size) {
        int x = xCenter - (size / 2);
        int y = yCenter - (size / 2);
        return new Rectangle(x, y, size, size);
    }

    /**
     * <p>Does nothing - child classes should use this to update their state after the
     * handle has been moved.</p>
     */
    public void update() {}

}
