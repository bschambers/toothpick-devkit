package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.TPGeometry;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.actor.TPForm;
import info.bschambers.toothpick.actor.TPLine;
import info.bschambers.toothpick.geom.Line;
import info.bschambers.toothpick.geom.Pt;
import info.bschambers.toothpick.ui.swing.Gfx;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FormEditorWindow extends JFrame
    implements KeyListener, MouseListener, MouseMotionListener, ComponentListener {

    private enum Mode { PARTS, VERTICES, DRAW, CENTER };
    private Mode mode = Mode.PARTS;

    private TPEditor editor;
    private ActorEditor actorEd;
    private FormEditorPanel panel = new FormEditorPanel();
    private TPGeometry geometry = new TPGeometry();
    private boolean snapToGrid = true;
    private int gridSize = 20;

    private boolean selectionActive = false;
    private boolean mouseLeftDown = false;
    private Point mark = new Point(0, 0);
    private Point point = new Point(0, 0);
    private Point cursor = new Point(0, 0);
    private List<EditorHandle> handles = new ArrayList<>();

    private Color gridColor = new Color(0, 0, 191);
    private Color pointColor = new Color(127, 127, 127);
    private Color cursorColor = new Color(191, 191, 191);

    public FormEditorWindow(TPEditor editor, ActorEditor actorEd) {
        super("Editing Form: " + actorEd.getActor().name);
        this.editor = editor;
        this.actorEd = actorEd;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        panel.setBackground(Color.BLACK);
        setContentPane(panel);
        addKeyListener(this);
        addComponentListener(this);
        // mouse listeners added to panel so that MouseEvent co-ordinates will be correct
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        switchToPartsMode();
    }

    private TPForm getForm() {
        return actorEd.getActor().getForm();
    }

    /**
     * <p>Repaints the panel without delay.</p>
     */
    public void updateView() {
        panel.paintImmediately(0, 0, getWidth(), getHeight());
    }

    private class FormEditorPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintGrid(g);
            paintForm(g);
            paintOverlay(g);
        }
    }

    private void paintGrid(Graphics g) {
        int xCenter = (int) geometry.xToScreen(geometry.getXCenter());
        int yCenter = (int) geometry.yToScreen(geometry.getYCenter());
        g.setColor(gridColor);
        // vertical lines
        int x = xCenter % gridSize;
        while (x < getWidth()) {
            g.drawLine(x, 0, x, getHeight());
            x += gridSize;
        }
        // horizontal lines
        int y = yCenter % gridSize;
        while (y < getHeight()) {
            g.drawLine(0, y, getWidth(), y);
            y += gridSize;
        }
        // center axes
        g.setColor(Color.BLUE);
        paintCenterCross(g, xCenter, yCenter);
    }

    private void paintCenterCross(Graphics g, int x, int y) {
        g.drawLine(0, y, getWidth(), y);
        g.drawLine(x, 0, x, getHeight());
    }

    private void paintForm(Graphics g) {
        g.setColor(Color.WHITE);
        EditorGfx.formArchetype(g, geometry, getForm());
    }

    /**
     * <p>Paints control-handles etc.</p>
     */
    private void paintOverlay(Graphics g) {
        // line-drawing in progress
        if (mode == Mode.DRAW) {
            if (mouseLeftDown) {
                g.setColor(Color.CYAN);
                Gfx.line(g, geometry, Gfx.STROKE_1, mark.x, mark.y, point.x, point.y);
            }
        }
        // center
        if (mode == Mode.CENTER) {
            if (mouseLeftDown) {
                g.setColor(Color.GRAY);
                paintCenterCross(g,
                                 (int) geometry.xToScreen(point.x),
                                 (int) geometry.yToScreen(point.y));
            }
        }
        // editor handles
        for (EditorHandle eh : handles) {
            if (eh.isSelected())
                g.setColor(Color.RED);
            else
                g.setColor(eh.getColor());
            Gfx.rectangle(g, geometry, eh.getHandle());
        }
        // cursor and point
        g.setColor(pointColor);
        Gfx.crosshairs(g, geometry, point.x, point.y, 20);
        g.setColor(cursorColor);
        Gfx.crosshairs(g, geometry, cursor.x, cursor.y, 20);
        // info text
        g.setColor(Color.WHITE);
        g.drawString(makeModeString(), 20, 30);
        g.drawString("screen-point: x=" + point.x + " y=" + point.y , 20, 45);
        g.drawString("geom-point: x=" + geometry.xFromScreen(point.x)
                     + " y=" + geometry.yFromScreen(point.y) , 20, 60);
    }

    private String makeModeString() {
        return "MODE: (1) " + (mode == Mode.PARTS ? "PARTS" : "parts")
                + " | (2) " + (mode == Mode.VERTICES ? "VERTICES" : "vertices")
                + " | (3) " + (mode == Mode.DRAW ? "DRAW" : "draw")
                + " | (4) " + (mode == Mode.CENTER ? "CENTER" : "center")
                + " --- (G) grid=" + snapToGrid;
    }

    public void recenterGrid() {
        geometry.setupAndCenter(panel.getWidth(), panel.getHeight());
        geometry.xOffset = getWidth() / 2;
        geometry.yOffset = getHeight() / 2;
    }

    private void switchToPartsMode() {
        mode = Mode.PARTS;
        selectionActive = false;
        handles.clear();
        for (int p = 0; p < getForm().numParts(); p++) {
            if (getForm().getPart(p) instanceof TPLine) {
                handles.add(new LineEditor((TPLine) getForm().getPart(p)));
            }
        }
        updateView();
    }

    private void switchToVerticesMode() {
        mode = Mode.VERTICES;
        selectionActive = false;
        handles.clear();
        for (int p = 0; p < getForm().numParts(); p++) {
            if (getForm().getPart(p) instanceof TPLine) {
                TPLine tpl = (TPLine) getForm().getPart(p);
                handles.add(new LineVertexEditor(tpl, LineVertexEditor.Vertex.START));
                handles.add(new LineVertexEditor(tpl, LineVertexEditor.Vertex.END));
            }
        }
        updateView();
    }

    private void switchToDrawMode() {
        mode = Mode.DRAW;
        selectionActive = false;
        handles.clear();
        updateView();
    }

    private void switchToCenterMode() {
        mode = Mode.CENTER;
        selectionActive = false;
        handles.clear();
        updateView();
    }

    /**
     * <p>Snapping to grid if the option is set.</p>
     */
    private Point convertPointPosition(Point p) {
        int x = (int) geometry.xFromScreen(p.x);
        int y = (int) geometry.yFromScreen(p.y);
        if (snapToGrid) {
            x = x - (x % gridSize);
            y = y - (y % gridSize);
        }
        return new Point(x, y);
    }

    /*------------------------- Keyboard Input -------------------------*/

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            System.out.println("exit editor window...");
            dispose();
            break;
        case KeyEvent.VK_1:
            if (mode != Mode.PARTS)
                switchToPartsMode();
            break;
        case KeyEvent.VK_2:
            if (mode != Mode.VERTICES)
                switchToVerticesMode();
            updateView();
            break;
        case KeyEvent.VK_3:
            if (mode != Mode.DRAW)
                switchToDrawMode();
            break;
        case KeyEvent.VK_4:
            if (mode != Mode.CENTER)
                switchToCenterMode();
            break;
        case KeyEvent.VK_G:
            snapToGrid = !snapToGrid;
            updateView();
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    /*-------------------------- Mouse Input ---------------------------*/

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        mouseLeftDown = true;
        mark = convertPointPosition(e.getPoint());
        point = convertPointPosition(e.getPoint());
        if (mode == Mode.PARTS || mode == Mode.VERTICES) {
            // select any handles under point
            for (EditorHandle eh : handles)
                eh.setSelected(eh.getHandle().contains(point));
        }
        updateView();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseLeftDown = false;
        if (mode == Mode.DRAW) {
            if (!point.equals(mark)) {
                // finalize line and add to form
                double x1 = mark.x;
                double y1 = mark.y;
                double x2 = point.x;
                double y2 = point.y;
                Line ln = new Line(x1, y1, x2, y2);
                System.out.println("new line: " + x1 + ", " + y1 + ", " + x2 + ", " + y2);
                TPLine tpl = new TPLine(ln);
                getForm().addPart(tpl);
                actorEd.getActor().updateForm();
                updateView();
            }
        } else if (mode == Mode.CENTER) {
            // set new center point for form
            double xx = point.x;
            double yy = point.y;
            geometry.xOffset += xx;
            geometry.yOffset += yy;
            for (int p = 0; p < getForm().numParts(); p++) {
                if (getForm().getPart(p) instanceof TPLine) {
                    TPLine tpl = (TPLine) getForm().getPart(p);
                    tpl.setArchetype(tpl.getArchetype().shift(-xx, -yy));
                }
            }
            actorEd.getActor().updateForm();
            updateView();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        cursor = convertPointPosition(e.getPoint());
        updateView();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        point = convertPointPosition(e.getPoint());
        cursor = convertPointPosition(e.getPoint());
        if (mode == Mode.PARTS || mode == Mode.VERTICES) {
            // move any selected handles
            boolean modified = false;
            for (EditorHandle eh : handles)
                if (eh.isSelected()) {
                    eh.setPosition(point.x, point.y);
                    eh.update();
                    modified = true;
                }
            if (modified)
                actorEd.getActor().updateForm();
        }
        updateView();
    }

    /*------------------------ window resizing -------------------------*/

    @Override
    public void componentResized(ComponentEvent e) {}

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

}
