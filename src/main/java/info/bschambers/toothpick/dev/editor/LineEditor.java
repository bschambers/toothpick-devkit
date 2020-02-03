package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.actor.TPLine;
import info.bschambers.toothpick.geom.Line;
import info.bschambers.toothpick.geom.Pt;

public class LineEditor extends EditorHandle {

    private TPLine tpl;

    public LineEditor(TPLine tpl) {
        this.tpl = tpl;
        Pt center = tpl.getArchetype().center();
        setPosition((int) center.x, (int) center.y);
    }

    /**
     * <p>Updates the {@code TPLine} archetype to be centered on the current handle
     * position.</p>
     */
    @Override
    public void update() {
        Pt center = tpl.getArchetype().center();
        double xx = x - center.x;
        double yy = y - center.y;
        Line archetype = tpl.getArchetype();
        tpl.setArchetype(archetype.shift(xx, yy));
    }

}
