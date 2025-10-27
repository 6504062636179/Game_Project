import java.awt.*;
import javax.swing.*;

public class Background {
    private Image image;

    public Background(String path) {
        image = new ImageIcon(path).getImage();
    }

    public void draw(Graphics g) {
        g.drawImage(image, 0, 0, 1280, 720, null);
    }

    public Image getImage() {
        return image;
    }
}
