package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPProgram;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.ui.swing.Gfx;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import java.awt.Color;
import java.awt.Graphics;

public class TPEditor extends TPSwingUI {

    private boolean editorMode = false;

    public TPEditor() {
        super("Toothpick Editor");
    }

    public boolean isEditorMode() {
        return editorMode;
    }

    public void setEditorMode(boolean val) {
        editorMode = val;
        if (!editorMode)
            cleanupSession();
    }

    /**
     * Dispose of editor resources.
     */
    public void cleanupSession() {
    }

    @Override
    protected void paintOverlay(Graphics g) {
        super.paintOverlay(g);
        if (editorMode) {
            TPProgram prog = getProgram();
            for (int i = 0; i < prog.numActors(); i++) {
                TPActor actor = prog.getActor(i);
                g.setColor(Color.GREEN);
                Gfx.centeredSquare(g, (int) actor.x, (int) actor.y, 10);
            }
        }
    }

}
