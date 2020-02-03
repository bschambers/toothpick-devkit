package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.actor.TPLine;
import info.bschambers.toothpick.geom.Line;
import info.bschambers.toothpick.geom.Pt;

public class LineVertexEditor extends EditorHandle {

    public enum Vertex { START, END };

    private TPLine tpl;
    private Vertex vertex;

    public LineVertexEditor(TPLine tpl, Vertex vertex) {
        this.tpl = tpl;
        this.vertex = vertex;
        Pt p = getVertexPoint();
        setPosition((int) p.x, (int) p.y);
    }

    /**
     * <p>Updates the {@code TPLine} archetype to be centered on the current handle
     * position.</p>
     */
    @Override
    public void update() {
        Pt v = getVertexPoint();
        double xx = x - v.x;
        double yy = y - v.y;
        Pt vv = v.add(xx, yy);
        Line archetype = tpl.getArchetype();
        if (vertex == Vertex.START)
            tpl.setArchetype(new Line(vv, tpl.getArchetype().end));
        else
            tpl.setArchetype(new Line(tpl.getArchetype().start, vv));
    }

    private Pt getVertexPoint() {
        if (vertex == Vertex.START)
            return tpl.getArchetype().start;
        else
            return tpl.getArchetype().end;
    }

}
