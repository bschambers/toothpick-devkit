package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPProgram;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.ui.swing.Gfx;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class TPEditor extends TPSwingUI {

    private boolean editorMode = false;
    private List<ActorEditor> actEds = new ArrayList<>();
    private List<ActorEditor> toAdd = new ArrayList<>();
    private List<ActorEditor> toRemove = new ArrayList<>();

    public TPEditor() {
        super("Toothpick Editor");
    }

    public boolean isEditorMode() {
        return editorMode;
    }

    public void setEditorMode(boolean val) {
        if (val)
            startEditorSession();
        else
            endEditorSession();
    }

    public void startEditorSession() {
        if (!editorMode) {
            editorMode = true;
            // clear any existing ActorEditors
            for (ActorEditor ae : actEds)
                removeAE(ae);
            // create an ActorEditor wrapper for each TPActor
            for (int i = 0; i < getProgram().numActors(); i++)
                addAE(new ActorEditor(getProgram().getActor(i)));
        }
    }

    /**
     * Clean up and dispose of editor resources.
     */
    public void endEditorSession() {
        if (editorMode) {
            editorMode = false;
            for (ActorEditor ae : actEds)
                removeAE(ae);
        }
    }

    @Override
    public void updateUI() {
        updateEditor();
        super.updateUI();
    }

    @Override
    protected void paintOverlay(Graphics g) {
        super.paintOverlay(g);
        if (editorMode) {
            for (ActorEditor ae : actEds)
                EditorGfx.actorEditor(g, ae);
        }
    }

    @Override
    protected void paintInfo(Graphics g) {
        super.paintInfo(g);
        if (editorMode) {
            g.setColor(Color.WHITE);
            int midX = getWidth() / 2;
            int y = 30;
            g.drawString("EDITOR MODE", midX, y);
        }
    }

    private void updateEditor() {
        // prune dead actors
        for (ActorEditor ae : actEds)
            if (!getProgram().contains(ae.getActor()))
                removeAE(ae);
        // add new actors
        for (int i = 0; i < getProgram().numActors(); i++)
            if (!containsActor(getProgram().getActor(i)))
                addAE(new ActorEditor(getProgram().getActor(i)));
        // remove and add as required
        for (ActorEditor ae : toRemove)
            actEds.remove(ae);
        for (ActorEditor ae : toAdd)
            actEds.add(ae);
        toRemove.clear();
        toAdd.clear();
        // update ActorEditors
        for (ActorEditor ae : actEds)
            ae.update();
    }

    private void addAE(ActorEditor ae) {
        toAdd.add(ae);
    }

    private void removeAE(ActorEditor ae) {
        toRemove.add(ae);
    }

    private boolean containsActor(TPActor a) {
        for (ActorEditor ae : actEds)
            if (a == ae.getActor())
                return true;
        return false;
    }

}