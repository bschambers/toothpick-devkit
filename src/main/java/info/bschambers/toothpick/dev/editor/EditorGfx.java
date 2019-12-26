package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.geom.Geom;
import info.bschambers.toothpick.ui.swing.Gfx;
import java.awt.Color;
import java.awt.Graphics;

public final class EditorGfx {

    public static void actorEditor(Graphics g, ActorEditor ae) {
        // rotation indicator
        int x1 = ae.getPosX();
        int y1 = ae.getPosY();
        int x2 = ae.getInertiaPosX();
        int y2 = ae.getInertiaPosY();
        int rScale = 15000;
        int angleTranslationFactor = 58;
        int len = 60;
        int rx = x1 - len / 2;
        int ry = y1 - len / 2;
        int startAngle = (int) -(Geom.angle(x1, y1, x2, y2) * angleTranslationFactor);
        int arcAngle = (int) -(ae.getActor().angleInertia * rScale);
        g.setColor(Color.CYAN);
        Gfx.arc(g, rx, ry, len, startAngle, arcAngle);
        // control handles
        g.setColor(Color.GRAY);
        g.drawLine(ae.getPosX(), ae.getPosY(), ae.getInertiaPosX(), ae.getInertiaPosY());
        g.setColor(Color.GREEN);
        Gfx.rectangle(g, ae.getPositionHandle());
        g.setColor(Color.PINK);
        Gfx.rectangle(g, ae.getInertiaHandle());
        if (ae.isSelected()) {
            g.setColor(Color.RED);
            Gfx.centeredSquare(g, ae.getPosX(), ae.getPosY(), 40);
        }
    }

}
