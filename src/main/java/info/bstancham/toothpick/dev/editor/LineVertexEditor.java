package info.bstancham.toothpick.dev.editor;

import info.bstancham.toothpick.actor.TPLink;
import info.bstancham.toothpick.geom.Node;
// import info.bstancham.toothpick.geom.Line;
// import info.bstancham.toothpick.geom.Pt;

public class LineVertexEditor extends EditorHandle {

    public enum Vertex { START, END };

    private TPLink link;
    private Vertex vertex;

    public LineVertexEditor(TPLink link, Vertex vertex) {
        this.link = link;
        this.vertex = vertex;
        // Pt p = getVertexPoint();
        // setPosition((int) p.x, (int) p.y);
        Node n = getVertexNode();
        setPosition((int) n.getX(), (int) n.getY());
    }

    /**
     * <p>Updates the {@code TPLine} archetype to be centered on the current handle
     * position.</p>
     */
    @Override
    public void update() {

        Node n = getVertexNode();
        double xx = x - n.getXArchetype();
        double yy = y - n.getYArchetype();
        double nxx = xx + n.getXArchetype();
        double nyy = yy + n.getYArchetype();
        // Pt vv = v.add(xx, yy);
        // Line archetype = tpl.getArchetype();
        if (vertex == Vertex.START) {
            link.getStartNode().setArchetype(nxx, nyy);
            // tpl.setArchetype(new Line(vv, tpl.getArchetype().end));
        } else {
            link.getEndNode().setArchetype(nxx, nyy);
            // tpl.setArchetype(new Line(tpl.getArchetype().start, vv));
        }

        // Pt v = getVertexPoint();
        // double xx = x - v.x;
        // double yy = y - v.y;
        // Pt vv = v.add(xx, yy);
        // Line archetype = tpl.getArchetype();
        // if (vertex == Vertex.START)
        //     tpl.setArchetype(new Line(vv, tpl.getArchetype().end));
        // else
        //     tpl.setArchetype(new Line(tpl.getArchetype().start, vv));
    }

    // private Pt getVertexPoint() {
    //     if (vertex == Vertex.START)
    //         return tpl.getArchetype().start;
    //     else
    //         return tpl.getArchetype().end;
    // }

    private Node getVertexNode() {
        if (vertex == Vertex.START)
            return link.getStartNode();
        else
            return link.getEndNode();
    }

}
