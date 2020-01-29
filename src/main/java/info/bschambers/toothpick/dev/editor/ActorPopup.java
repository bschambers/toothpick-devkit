package info.bschambers.toothpick.dev.editor;

import info.bschambers.toothpick.actor.TPActor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ActorPopup extends TPEditorPopup {

    private TPEditor editor;
    private JLabel infoLabel = new JLabel("0 actors selected");
    private LabelledTextField currentNameField = new LabelledTextField("name: ...");
    private LabelledTextField currentXField = new LabelledTextField("x: ...");
    private LabelledTextField currentYField = new LabelledTextField("y: ...");
    private LabelledTextField currentAngleField = new LabelledTextField("angle: ...");
    private LabelledTextField currentXInertiaField = new LabelledTextField("x-inertia: ...");
    private LabelledTextField currentYInertiaField = new LabelledTextField("y-inertia: ...");
    private LabelledTextField currentAngleInertiaField = new LabelledTextField("angle-inertia: ...");
    private JButton currentUpdateButton;

    public ActorPopup(TPEditor editor) {
        super("actors");
        this.editor = editor;

        JPanel panel = makeVerticalPanel();
        setContentPane(panel);

        JPanel infoPan = makeVerticalPanel("info");
        infoPan.add(infoLabel);
        panel.add(infoPan);

        JPanel currentPan = makeVerticalPanel("current");
        currentPan.add(currentNameField);
        currentPan.add(currentXField);
        currentPan.add(currentYField);
        currentPan.add(currentAngleField);
        currentPan.add(currentXInertiaField);
        currentPan.add(currentYInertiaField);
        currentPan.add(currentAngleInertiaField);
        currentUpdateButton = new JButton(new AbstractAction("update") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    updateCurrent();
                }
            });
        currentPan.add(currentUpdateButton);
        panel.add(currentPan);
    }

    public void update() {
        List<ActorEditor> actEds = new ArrayList<>();

        for (ActorEditor ae : editor.getActorEditors())
            if (ae.isSelected())
                actEds.add(ae);

        infoLabel.setText(actEds.size() + " actors selected");
        currentNameField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().name));
        currentXField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().x + ""));
        currentYField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().y + ""));
        currentAngleField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().angle + ""));
        currentXInertiaField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().xInertia + ""));
        currentYInertiaField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().yInertia + ""));
        currentAngleInertiaField.setText(collectiveStringVal(actEds, (ActorEditor ae) -> ae.getActor().angleInertia + ""));
    }

    private String collectiveStringVal(List<ActorEditor> actEds,
                                       Function<ActorEditor, String> func) {
        String val = "";
        for (ActorEditor ae : actEds) {
            String newVal = func.apply(ae);
            if (val.isEmpty()) {
                val = newVal;
            } else if (!val.equals(newVal)) {
                val = "...";
            }
        }
        return val;
    }

    /**
     * <p>Update selected actors from values in current-panel.</p>
     */
    private void updateCurrent() {
        System.out.println("UPDATE SELECTED ACTORS");

        String name = currentNameField.getText();
        if (name.equals("..."))
            name = null;
        Double x = parseDoubleOrNull(currentXField.getText());
        Double y = parseDoubleOrNull(currentYField.getText());
        Double angle = parseDoubleOrNull(currentAngleField.getText());
        Double xInertia = parseDoubleOrNull(currentXInertiaField.getText());
        Double yInertia = parseDoubleOrNull(currentYInertiaField.getText());
        Double angleInertia = parseDoubleOrNull(currentAngleInertiaField.getText());

        for (ActorEditor ae : editor.getActorEditors()) {
            if (ae.isSelected()) {
                TPActor actor = ae.getActor();
                if (name != null)
                    actor.name = name;
                if (x != null)
                    actor.x = x;
                if (y != null)
                    actor.y = y;
                if (angle != null)
                    actor.angle = angle;
                if (xInertia != null)
                    actor.xInertia = xInertia;
                if (yInertia != null)
                    actor.yInertia = yInertia;
                if (angleInertia != null)
                    actor.angleInertia = angleInertia;
            }
        }
    }

    /**
     * <p>Attempt to parse text as double, or return null in case of failure.</p>
     */
    private Double parseDoubleOrNull(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {}
        return null;
    }

}
