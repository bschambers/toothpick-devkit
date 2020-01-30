package info.bschambers.toothpick.dev.editor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class TPEditorPopup extends JFrame {

    public TPEditorPopup(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
    }

    /** Makes a JPanel with vertical oriented BoxLayout. */
    protected JPanel makeVerticalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * <p>Makes a JPanel with vertical oriented BoxLayout with TitledBorder.</p>
     */
    protected JPanel makeVerticalPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder(title));
        return panel;
    }

    protected JButton makeButton(String label, Runnable action) {
        return new JButton(new AbstractAction(label) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    action.run();
                }
            });
    }

}
