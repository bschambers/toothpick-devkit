package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPGeometry;
import info.bschambers.toothpick.TPProgram;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.ui.swing.Gfx;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import static java.awt.event.KeyEvent.VK_SHIFT;

public class TPEditor extends TPSwingUI {

    private enum Mode { IDLE, SELECT_RECT, MOVE_ACTOR, MOVE_INERTIA, ROTATION }
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
    private boolean showCursor = true;
    // keyboard
    private boolean shiftDown = false;
    // popup windows
    private HubPopup hubWindow;
    private ProgramPopup progWindow;
    private ActorPopup actorWindow;

    public TPEditor() {
        super("Toothpick Editor");
        hubWindow = new HubPopup();
        progWindow = new ProgramPopup();
        actorWindow = new ActorPopup(this);
        hubWindow.setBounds(5, 5, 250, 200);
        progWindow.setBounds(5, 210, 250, 300);
        actorWindow.setBounds(5, 515, 250, 300);
    }

    private TPGeometry getGeom() {
        return getProgram().getGeometry();
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

    private void startEditorSession() {
        if (!editorMode) {
            editorMode = true;
            // clear any existing ActorEditors
            for (ActorEditor ae : actEds)
                removeAE(ae);
            // create an ActorEditor wrapper for each TPActor
            for (int i = 0; i < getProgram().numActors(); i++)
                addAE(new ActorEditor(getProgram().getActor(i)));
            // show popup editor windows
            hubWindow.setVisible(true);
            progWindow.setVisible(true);
            actorWindow.setVisible(true);
        }
    }

    /**
     * Clean up and dispose of editor resources.
     */
    private void endEditorSession() {
        if (editorMode) {
            editorMode = false;
            for (ActorEditor ae : actEds)
                removeAE(ae);
            // hide popup editor windows
            hubWindow.setVisible(false);
            progWindow.setVisible(false);
            actorWindow.setVisible(false);
        }
    }

    public List<ActorEditor> getActorEditors() {
        return actEds;
    }

    /**
     * <p>NOTE: may be {@code null}.</p>
     */
    public ActorEditor getCurrentActorEditor() {
        return selectedAE;
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
                EditorGfx.actorEditor(g, getGeom(), ae);
            // paint cursor
            if (showCursor) {
                g.setColor(Color.GRAY);
                Gfx.crosshairs(g, currentP.x, currentP.y, 50);
            }
        }
    }

    @Override
    protected void paintInfo(Graphics g) {
        super.paintInfo(g);
        if (editorMode) {
            g.setColor(Color.WHITE);
            int midX = getWidth() / 2;
            g.drawString("Editor Mode: " + mode, midX, 30);
            g.drawString("point : " + currentP.x + ", " + currentP.y, midX, 45);
            g.drawString("shift-left-click and drag to change angle-inertia", midX, 75);
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
            if (ae.getPositionHandle(getGeom()).contains(p)) {
                selectedAE = ae;
                break Choosing;
            } else if (ae.getInertiaHandle(getGeom()).contains(p)) {
                inertiaHandleSelected = true;
                selectedAE = ae;
                break Choosing;
            }
        }

        actorWindow.update();
    }

    private ActorEditor getAEAtPoint(Point p) {
        p = new Point(p.x, p.y - getInsets().top);
        for (ActorEditor ae : actEds) {
            if (ae.getPositionHandle(getGeom()).contains(p) ||
                ae.getInertiaHandle(getGeom()).contains(p)) {
                return ae;
            }
        }
        return null;
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
                if (selectRect.contains(getGeom().xToScreen(ae.getPosX()),
                                        getGeom().yToScreen(ae.getPosY()))) {
                    ae.setSelected(true);
                } else {
                    ae.setSelected(false);
                }
            }
        }

        actorWindow.update();
    }

    /*------------------------- Keyboard Input -------------------------*/

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (editorMode)
            if (e.getKeyCode() == VK_SHIFT)
                shiftDown = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        if (editorMode)
            if (e.getKeyCode() == VK_SHIFT)
                shiftDown = false;
    }

    /*-------------------------- Mouse Input ---------------------------*/

    private void setPoint(Point p, MouseEvent e) {
        Insets insets = getInsets();
        p.x = e.getPoint().x;
        p.y = e.getPoint().y - insets.top;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (editorMode && e.getButton() == MouseEvent.BUTTON3) {
            System.out.println("right-clicked");
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        // left button
        if (editorMode && e.getButton() == MouseEvent.BUTTON1) {
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
                    if (shiftDown) {
                        mode = Mode.ROTATION;
                    } else {
                        mode = Mode.MOVE_ACTOR;
                    }
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
        if (editorMode)
            mode = Mode.IDLE;
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
            TPGeometry geom = getGeom();

            if (mode == Mode.SELECT_RECT) {

                int sx = Math.min(startP.x, currentP.x);
                int sy = Math.min(startP.y, currentP.y);
                int sw = Math.abs(currentP.x - startP.x);
                int sh = Math.abs(currentP.y - startP.y);
                selectRect = new Rectangle(sx, sy, sw, sh);
                updateSelection();

            } else if (mode == Mode.MOVE_INERTIA) {
                if (selectedAE != null && geom.scale != 0) {
                    ActorEditor sae = selectedAE;
                    double ix = sae.getActor().xInertia
                        + ((x / (double) sae.getInertiaScale()) / geom.scale);
                    double iy = sae.getActor().yInertia
                        + ((y / (double) sae.getInertiaScale()) / geom.scale);
                    for (ActorEditor ae : actEds) {
                        if (ae.isSelected()) {
                            ae.getActor().xInertia = ix;
                            ae.getActor().yInertia = iy;
                            ae.getActor().updateForm();
                        }
                    }
                }

            } else if (mode == Mode.ROTATION) {
                if (selectedAE != null) {
                    ActorEditor sae = selectedAE;
                    double ai = sae.getActor().angleInertia + (y / (double) 1500);
                    for (ActorEditor ae : actEds) {
                        if (ae.isSelected()) {
                            ae.getActor().angleInertia = ai;
                            ae.getActor().updateForm();
                        }
                    }
                }

            } else if (mode == Mode.MOVE_ACTOR) {
                for (ActorEditor ae : actEds) {
                    if (ae.isSelected() && geom.scale != 0) {
                        ae.getActor().x += (x / geom.scale);
                        ae.getActor().y += (y / geom.scale);
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
