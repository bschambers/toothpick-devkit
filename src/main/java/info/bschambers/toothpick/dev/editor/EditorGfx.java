package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.ui.swing.Gfx;
import java.awt.Color;
import java.awt.Graphics;

public final class EditorGfx {

    public static void actorEditor(Graphics g, ActorEditor ae) {
        g.setColor(Color.GRAY);
        g.drawLine(ae.getPosX(), ae.getPosY(), ae.getVectorPosX(), ae.getVectorPosY());
        g.setColor(Color.GREEN);
        Gfx.rectangle(g, ae.getPositionHandle());
        g.setColor(Color.PINK);
        Gfx.rectangle(g, ae.getVectorHandle());
        if (ae.isSelected()) {
            g.setColor(Color.RED);
            Gfx.centeredSquare(g, ae.getPosX(), ae.getPosY(), 40);
        }
    }

}
