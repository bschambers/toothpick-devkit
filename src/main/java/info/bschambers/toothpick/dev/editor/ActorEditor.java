package info.bschambers.toothpick.dev.editor;

import java.awt.Rectangle;
import info.bschambers.toothpick.actor.TPActor;

public class ActorEditor {

    private TPActor actor;
    private int posX = 0;
    private int posY = 0;
    private int inerPosX = 0;
    private int inerPosY = 0;
    private int posHandleSize = 20;
    private int inerHandleSize = 10;
    private int inertiaScale = 80;
    private Rectangle positionHandle;
    private Rectangle inertiaHandle;
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

    public Rectangle getPositionHandle() {
        return positionHandle;
    }

    public Rectangle getInertiaHandle() {
        return inertiaHandle;
    }

    public int getInertiaScale() {
        return inertiaScale;
    }

    public void update() {
        posX = (int) getActor().x;
        posY = (int) getActor().y;
        int x = posX - (posHandleSize / 2);
        int y = posY - (posHandleSize / 2);
        positionHandle = new Rectangle(x, y, posHandleSize, posHandleSize);
        inerPosX = posX + (int) (getActor().xInertia * inertiaScale);
        inerPosY = posY + (int) (getActor().yInertia * inertiaScale);
        x = inerPosX - (inerHandleSize / 2);
        y = inerPosY - (inerHandleSize / 2);
        inertiaHandle = new Rectangle(x, y, inerHandleSize, inerHandleSize);
    }

}
