package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPGeometry;
import info.bschambers.toothpick.actor.TPExplosion;
import info.bschambers.toothpick.actor.TPForm;
import info.bschambers.toothpick.actor.TPImage;
import info.bschambers.toothpick.actor.TPLine;
import info.bschambers.toothpick.actor.TPPart;
import info.bschambers.toothpick.actor.TPText;
import info.bschambers.toothpick.geom.Geom;
import info.bschambers.toothpick.ui.swing.Gfx;
import java.awt.Color;
import java.awt.Graphics;

public final class EditorGfx {

    public static void actorEditor(Graphics g, TPGeometry geom, ActorEditor ae) {
        // rotation indicator
        int x1 = (int) geom.xToScreen(ae.getPosX());
        int y1 = (int) geom.yToScreen(ae.getPosY());
        int x2 = (int) geom.xToScreen(ae.getInertiaPosX());
        int y2 = (int) geom.yToScreen(ae.getInertiaPosY());
        int rScale = 15000;
        int angleTranslationFactor = 58;
        int len = 60;
        int rx = x1 - len / 2;
        int ry = y1 - len / 2;
        int startAngle = (int) -(Geom.angle(x1, y1, x2, y2) * angleTranslationFactor);
        int arcAngle = (int) -(ae.getActor().angleInertia * rScale);
        g.setColor(Color.CYAN);
        Gfx.arc(g, rx, ry, len, startAngle, arcAngle);
        // inertia vector
        g.setColor(Color.GRAY);
        Gfx.line(g, geom, Gfx.STROKE_1, ae.getPosX(), ae.getPosY(),
                 ae.getInertiaPosX(), ae.getInertiaPosY());
        // position control handle
        g.setColor(Color.GREEN);
        Gfx.rectangle(g, ae.getPositionHandle(geom));
        if (ae.isSelected()) {
            int x = (int) geom.xToScreen(ae.getPosX());
            int y = (int) geom.yToScreen(ae.getPosY());
            g.setColor(Color.RED);
            Gfx.centeredSquare(g, x, y, 40);
        }
        // inertia control handle
        g.setColor(Color.PINK);
        Gfx.rectangle(g, ae.getInertiaHandle(geom));
    }

    public static void formArchetype(Graphics g, TPGeometry geom, TPForm form) {
        for (int i = 0; i < form.numParts(); i++) {
            TPPart part = form.getPart(i);
            if (part instanceof TPLine) {
                tpLineArchetype(g, geom, (TPLine) part);
            } else if (part instanceof TPExplosion) {
                Gfx.explosion(g, geom, (TPExplosion) part);
            } else if (part instanceof TPText) {
                Gfx.text(g, geom, (TPText) part);
            } else if (part instanceof TPImage) {
                Gfx.image(g, geom, (TPImage) part);
            }
        }
    }

    public static void tpLineArchetype(Graphics g, TPGeometry geom, TPLine tpl) {
        Gfx.line(g, geom, Gfx.getStrokeForLineStrength(tpl),
                 tpl.getArchetype().start, tpl.getArchetype().end);
    }

}
