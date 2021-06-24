package info.bstancham.toothpick.dev.editor;

import info.bstancham.toothpick.actor.TPLink;
import info.bstancham.toothpick.geom.Line;
import info.bstancham.toothpick.geom.Pt;
import info.bstancham.toothpick.geom.Geom;

public class LineEditor extends EditorHandle {

    private TPLink link;

    public LineEditor(TPLink link) {
        this.link = link;
        Pt center = getArchetypeCenter(link);
        setPosition((int) center.x, (int) center.y);
    }

    /**
     * <p>Updates the {@code TPLine} archetype to be centered on the current handle
     * position.</p>
     */
    @Override
    public void update() {
        Pt center = getArchetypeCenter(link);
        double xx = x - center.x;
        double yy = y - center.y;
        // Line archetype = tpl.getArchetype();
        // tpl.setArchetype(archetype.shift(xx, yy));
        link.getStartNode().setArchetype(link.getStartNode().getX() + xx,
                                         link.getStartNode().getY() + yy);
        link.getEndNode().setArchetype(link.getEndNode().getX() + xx,
                                       link.getEndNode().getY() + yy);
    }

    private Pt getArchetypeCenter(TPLink ln) {
	return new Pt(Geom.midVal(ln.getStartNode().getXArchetype(), ln.getEndNode().getXArchetype()),
                      Geom.midVal(ln.getStartNode().getYArchetype(), ln.getEndNode().getYArchetype()));
    }

}
