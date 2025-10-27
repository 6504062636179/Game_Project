import java.awt.*;
import java.io.*;

public class FontManager {
    public static Font loadFont(String path, float size) {
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, new File(path));
            return f.deriveFont(size);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int)size);
        }
    }
}
