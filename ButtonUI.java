import javax.swing.*;
import java.awt.*;

public class ButtonUI {
    public static void style(JButton btn, Font font, Color fg) {
        btn.setFont(font);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(fg);
    }
}
