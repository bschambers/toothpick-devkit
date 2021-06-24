package info.bstancham.toothpick.dev.editor;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class HubPopup extends TPEditorPopup {

    public HubPopup() {
        super("editor");
        JPanel panel = makeVerticalPanel();
        setContentPane(panel);
        panel.add(new JLabel("open shape editor"));
    }

}
