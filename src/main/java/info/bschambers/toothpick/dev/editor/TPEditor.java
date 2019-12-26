package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPProgram;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.ui.swing.Gfx;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TPEditor extends TPSwingUI {

    private enum Mode { IDLE, SELECT_RECT, MOVE_ACTOR, MOVE_INERTIA }
    private Mode mode = Mode.IDLE;

    private boolean editorMode = false;
    private List<ActorEditor> actEds = new ArrayList<>();
    private List<ActorEditor> toAdd = new ArrayList<>();
    private List<ActorEditor> toRemove = new ArrayList<>();
    // mouse and selection
    private Point startP = new Point(0, 0);
    private Point currentP = new Point(0, 0);
    private Point prevP = new Point(0, 0);
    private Rectangle selectRect = null;
    private ActorEditor selectedAE = null;
    private boolean inertiaHandleSelected = false;

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
            // paint selection
            if (selectRect != null) {
                g.setColor(Color.BLUE);
                Gfx.rectangle(g, selectRect);
            }
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
            g.drawString("Editor Mode: " + mode, midX, y);
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

    /**
     * <p>If an ActorEditor control handle is under point then set return the parent
     * ActorEditor as {@code selectedAE}, otherwise set it to {@code null}.</p>
     *
     * <p>If the point is on an inertia control handle then also set
     * {@code inertiaHandleSelected} to true.</p>
     */
    private void selectAE(Point p) {
        selectedAE = null;
        inertiaHandleSelected = false;
        Insets insets = getInsets();
        p = new Point(p.x, p.y - insets.top);
        Choosing:
        for (ActorEditor ae : actEds) {
            if (ae.getPositionHandle().contains(p)) {
                selectedAE = ae;
                break Choosing;
            } else if (ae.getInertiaHandle().contains(p)) {
                inertiaHandleSelected = true;
                selectedAE = ae;
                break Choosing;
            }
        }
    }

    private void clearSelection() {
        selectedAE = null;
        selectRect = null;
        for (ActorEditor ae : actEds)
            ae.setSelected(false);
    }

    /**
     * Selects all actors inside the rectangular selection area.
     */
    private void updateSelection() {
        if (selectRect != null) {
            for (ActorEditor ae : actEds) {
                if (selectRect.contains(ae.getPosX(), ae.getPosY())) {
                    ae.setSelected(true);
                } else {
                    ae.setSelected(false);
                }
            }
        }
    }

    /*-------------------------- Mouse Input ---------------------------*/

    private void setPoint(Point p, MouseEvent e) {
        p.x = e.getPoint().x;
        p.y = e.getPoint().y;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (editorMode) {
            setPoint(startP, e);
            setPoint(currentP, e);
            setPoint(prevP, e);
            selectAE(e.getPoint());
            ActorEditor ae = selectedAE;

            if (ae == null) {
                clearSelection();
                mode = Mode.SELECT_RECT;

            } else {
                if (inertiaHandleSelected) {
                    mode = Mode.MOVE_INERTIA;
                } else {
                    mode = Mode.MOVE_ACTOR;
                }
                if (!ae.isSelected()) {
                    clearSelection();
                    ae.setSelected(true);
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        if (editorMode) {
            mode = Mode.IDLE;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        if (editorMode) {
            setPoint(currentP, e);
            int x = currentP.x - prevP.x;
            int y = currentP.y - prevP.y;
            prevP.x = currentP.x;
            prevP.y = currentP.y;

            if (mode == Mode.SELECT_RECT) {

                int sx = Math.min(startP.x, currentP.x);
                int sy = Math.min(startP.y, currentP.y);
                int sw = Math.abs(currentP.x - startP.x);
                int sh = Math.abs(currentP.y - startP.y);
                selectRect = new Rectangle(sx, sy, sw, sh);
                updateSelection();

            } else if (mode == Mode.MOVE_INERTIA) {

                if (selectedAE != null) {

                    ActorEditor sae = selectedAE;
                    double ix = sae.getActor().xInertia + (x / (double) sae.getInertiaScale());
                    double iy = sae.getActor().yInertia + (y / (double) sae.getInertiaScale());

                    for (ActorEditor ae : actEds) {
                        if (ae.isSelected()) {
                            ae.getActor().xInertia = ix;
                            ae.getActor().yInertia = iy;
                            ae.getActor().updateForm();
                        }
                    }
                }

            } else if (mode == Mode.MOVE_ACTOR) {
                for (ActorEditor ae : actEds) {
                    if (ae.isSelected()) {
                        ae.getActor().x += x;
                        ae.getActor().y += y;
                        ae.getActor().updateForm();
                    }
                }

                if (selectRect != null) {
                    int sx = selectRect.x;
                    int sy = selectRect.y;
                    selectRect.setLocation(sx + x, sy + y);
                }
            }
	}
    }

}
