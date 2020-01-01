package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPGeometry;
import info.bschambers.toothpick.actor.TPActor;
import java.awt.Rectangle;

public class ActorEditor {

    private TPActor actor;
    private int posX = 0;
    private int posY = 0;
    private int inerPosX = 0;
    private int inerPosY = 0;
    private int posHandleSize = 20;
    private int inerHandleSize = 10;
    private int inertiaScale = 80;
    private boolean selected = false;

    public ActorEditor(TPActor a) {
        actor = a;
        update();
    }

    public TPActor getActor() { return actor; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean val) { selected = val; }

    public int getPosX() { return posX; }
    public int getPosY() { return posY; }

    public int getInertiaPosX() { return inerPosX; }
    public int getInertiaPosY() { return inerPosY; }

    public Rectangle getPositionHandle(TPGeometry geom) {
        int x = (int) geom.xToScreen(posX);
        int y = (int) geom.yToScreen(posY);
        return makeCenteredSquare(x, y, posHandleSize);
    }

    public Rectangle getInertiaHandle(TPGeometry geom) {
        int x = (int) geom.xToScreen(inerPosX);
        int y = (int) geom.yToScreen(inerPosY);
        return makeCenteredSquare(x, y, inerHandleSize);
    }

    private Rectangle makeCenteredSquare(int xCenter, int yCenter, int size) {
        int x = xCenter - (size / 2);
        int y = yCenter - (size / 2);
        return new Rectangle(x, y, size, size);

    }

    public int getInertiaScale() {
        return inertiaScale;
    }

    public void update() {
        posX = (int) getActor().x;
        posY = (int) getActor().y;
        int x = posX - (posHandleSize / 2);
        int y = posY - (posHandleSize / 2);
        inerPosX = posX + (int) (getActor().xInertia * inertiaScale);
        inerPosY = posY + (int) (getActor().yInertia * inertiaScale);
    }

}
