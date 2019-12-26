package info.bschambers.toothpick.dev.editor;

import java.awt.Rectangle;
import info.bschambers.toothpick.actor.TPActor;

public class ActorEditor {

    private TPActor actor;
    private int posX = 0;
    private int posY = 0;
    private int vecPosX = 0;
    private int vecPosY = 0;
    private int posHandleSize = 20;
    private int vecHandleSize = 10;
    private int vectorScale = 80;
    private Rectangle positionHandle;
    private Rectangle vectorHandle;
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

    public int getVectorPosX() { return vecPosX; }
    public int getVectorPosY() { return vecPosY; }

    public Rectangle getPositionHandle() {
        return positionHandle;
    }

    public Rectangle getVectorHandle() {
        return vectorHandle;
    }

    public void update() {
        posX = (int) getActor().x;
        posY = (int) getActor().y;
        int x = posX - (posHandleSize / 2);
        int y = posY - (posHandleSize / 2);
        positionHandle = new Rectangle(x, y, posHandleSize, posHandleSize);

        vecPosX = posX + (int) (getActor().xInertia * vectorScale);
        vecPosY = posY + (int) (getActor().yInertia * vectorScale);

        x = vecPosX - (vecHandleSize / 2);
        y = vecPosY - (vecHandleSize / 2);
        vectorHandle = new Rectangle(x, y, vecHandleSize, vecHandleSize);
    }

}
