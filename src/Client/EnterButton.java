package client;

import javax.swing.*;
import java.awt.*;

class EnterButton extends JButton {
    public EnterButton() {
        Dimension size = getPreferredSize();
        size.width = 34;
        size.height = 26;
        setPreferredSize(size);
        setContentAreaFilled(false);
    }

    protected void paintBorder(Graphics g) {
        Graphics g2d = (Graphics2D)g;
        g2d.drawLine(18, 0, 32, 0);
        g2d.drawLine(32, 0, 32, 24);
        g2d.drawLine(32, 24, 0, 24);
        g2d.drawLine(0, 24, 0, 13);
        g2d.drawLine(0, 13, 18, 13);
        g2d.drawLine(18, 13, 18, 0);
    }
}
