package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import Project.Client.CardView;
import Project.Client.ICardControls;

public class ConnectionPanel extends JPanel {
    private String host;
    private int port;

    public ConnectionPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // add host info
        JLabel hostLabel = new JLabel("<HTML><U>Host:</U></HTML>");//am3485 4/15/24
        //hostLabel.setForeground(Color.blue); // am3485 4/15/24
        JTextField hostValue = new JTextField("127.0.0.1");
        //JTextField hostValue = new JTextField("<HTML><U>127.0.0.1</U></HTML>");
        JLabel hostError = new JLabel();
        //hostValue.setForeground(Color.yellow); // am3485 4/15/24
        content.add(hostLabel);
        content.add(hostValue);
        content.add(hostError);
        // add port info
        JLabel portLabel = new JLabel("<HTML><I>Port:</I></HTML>");//am3485 4/15/24
        //portLabel.setForeground(Color.red);// am3485 4/15/24
        JTextField portValue = new JTextField("3000");
        //JTextField portValue = new JTextField("<HTML><I>3000</I></HTML>");
        //portValue.setForeground(Color.green);// am3485 4/15/24
        JLabel portError = new JLabel();
        content.add(portLabel);
        content.add(portValue);
        content.add(portError);
        // add button
        JButton button = new JButton("Next");
        // add listener
        button.addActionListener((event) -> {
            boolean isValid = true;
            try {
                port = Integer.parseInt(portValue.getText());
                portError.setVisible(false);
                // if valid, next card

            } catch (NumberFormatException e) {
                portError.setText("Invalid port value, must be a number");
                portError.setVisible(true);
                isValid = false;
            }
            if (isValid) {
                host = hostValue.getText();
                controls.next();
            }
        });
        content.add(button);
        // filling the other slots for spacing
        this.add(new JPanel(), BorderLayout.WEST);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.SOUTH);
        // add the content to the center slot
        this.add(content, BorderLayout.CENTER);
        this.setName(CardView.CONNECT.name());
        controls.addPanel(CardView.CONNECT.name(), this);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}