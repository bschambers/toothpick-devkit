package info.bschambers.toothpick.dev.editor;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LabelledTextField extends JPanel {

    private JLabel label;
    private JTextField textField = new JTextField();

    public LabelledTextField(String labelText) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        label = new JLabel(labelText + ":");
        textField.setText("...");
        add(label);
        add(textField);
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }

}
