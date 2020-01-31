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
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FormEditorWindow extends JFrame
    implements KeyListener, MouseListener, MouseMotionListener, ComponentListener {

    private enum Mode { DRAW, POINTS, LINES };
    private Mode mode = Mode.DRAW;

    private TPEditor editor;
    private TPActor actor;
    private FormEditorPanel panel = new FormEditorPanel();
    private TPGeometry geometry = new TPGeometry();
    private boolean snapToGrid = true;

    private Point mark = new Point(0, 0);
    private Point point = new Point(0, 0);
    private boolean mouseLeftDown = false;

    public FormEditorWindow(TPEditor editor, TPActor actor) {
        super("Editing Form: " + actor.name);
        this.editor = editor;
        this.actor = actor;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        panel.setBackground(Color.BLACK);
        setContentPane(panel);

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
    }

    private TPForm getForm() {
        return actor.getForm();
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
        g.setColor(Color.BLUE);
        int x = (int) geometry.xOffset;
        int y = (int) geometry.yOffset;
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

        if ( mode == Mode.DRAW) {
            if (mouseLeftDown) {
                g.setColor(Color.CYAN);
                g.drawLine(mark.x, mark.y, point.x, point.y);
            }
        } else if (mode == Mode.POINTS) {
            g.setColor(Color.MAGENTA);
            paintPoints(g);
        } else if (mode == Mode.LINES) {
            g.setColor(Color.YELLOW);
            paintLines(g);
        }

        g.setColor(Color.WHITE);
        g.drawString("MODE: " + mode, 20, 30);
    }

    private void paintPoints(Graphics g) {
        for (int p = 0; p < getForm().numParts(); p++) {
            if (getForm().getPart(p) instanceof TPLine) {
                Line ln = ((TPLine) getForm().getPart(p)).getArchetype();
                Pt a = ln.start;
                Pt b = ln.end;
                Gfx.centeredSquare(g, (int) geometry.xToScreen(a.x), (int) geometry.yToScreen(a.y), 10);
                Gfx.centeredSquare(g, (int) geometry.xToScreen(b.x), (int) geometry.yToScreen(b.y), 10);
            }
        }
    }

    private void paintLines(Graphics g) {
        for (int p = 0; p < getForm().numParts(); p++) {
            if (getForm().getPart(p) instanceof TPLine) {
                Line ln = ((TPLine) getForm().getPart(p)).getArchetype();
                Pt c = ln.center();
                Gfx.centeredSquare(g, (int) geometry.xToScreen(c.x), (int) geometry.yToScreen(c.y), 10);
            }
        }
    }

    public void recenterGrid() {
        geometry.xOffset = getWidth() / 2;
        geometry.yOffset = getHeight() / 2;
    }

    /*------------------------- Keyboard Input -------------------------*/

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            System.out.println("exit editor window...");
            dispose();
            break;
        case KeyEvent.VK_D:
            mode = Mode.DRAW;
            updateView();
            break;
        case KeyEvent.VK_L:
            mode = Mode.LINES;
            updateView();
            break;
        case KeyEvent.VK_P:
            mode = Mode.POINTS;
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
        mark = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseLeftDown = false;

        if (mode == Mode.DRAW) {
            if (!point.equals(mark)) {
                // finalize line and add to form
                double x1 = geometry.xFromScreen(mark.x);
                double y1 = geometry.yFromScreen(mark.y);
                double x2 = geometry.xFromScreen(point.x);
                double y2 = geometry.yFromScreen(point.y);
                Line ln = new Line(x1, y1, x2, y2);
                System.out.println("new line: " + x1 + ", " + y1 + ", " + x2 + ", " + y2);
                TPLine tpl = new TPLine(ln);
                getForm().addPart(tpl);
                actor.updateForm();
                updateView();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        point = e.getPoint();
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
